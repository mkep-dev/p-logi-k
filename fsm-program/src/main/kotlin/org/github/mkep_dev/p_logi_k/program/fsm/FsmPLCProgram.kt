/*
 * P-logi-K. A Kotlin(TM)-based PLC simulation.
 * Copyright (C)  2022.  Markus Keppeler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package org.github.mkep_dev.p_logi_k.program.fsm

import mu.KLogging
import org.github.mkep_dev.p_logi_k.Identifier
import org.github.mkep_dev.p_logi_k.io.BooleanValue
import org.github.mkep_dev.p_logi_k.io.GenericInput
import org.github.mkep_dev.p_logi_k.io.GenericOutput
import org.github.mkep_dev.p_logi_k.io.IntegerValue
import org.github.mkep_dev.p_logi_k.model.io.DataDirection
import org.github.mkep_dev.p_logi_k.model.io.IOElement
import org.github.mkep_dev.p_logi_k.program.api.CyclicPLCProgram
import org.github.mkep_dev.p_logi_k.program.api.IOAccessLayer
import org.github.mkep_dev.p_logi_k.program.fsm.formula.*

open class FsmPLCProgram(override val name: String, private val efsm: EFSM, private val ioAliases: Map<String, String>) :
    CyclicPLCProgram {

    private companion object : KLogging()

    private val _usedInputs: List<IOElement<Any>>
    private val _usedOutputs: List<IOElement<Any>>
    private lateinit var _inputAccessMap: Map<Identifier, GenericInput<out Any>>
    private lateinit var _outputAccessMap: Map<Identifier, GenericOutput<Any>>
    private var currentState: State = efsm.initialState

    private val _variableValues: MutableMap<Variable<out Any>, Constant<out Any>> = mutableMapOf()
    private val timerResetMillis: MutableMap<TimerVariable, Long> = mutableMapOf()

    init {
        _usedInputs = efsm.transitions.flatMap { transition ->
            transition.guard.getOperandsOfType(Input::class)
        }.map {
            val value = when (it.type) {
                Boolean::class -> BooleanValue(false)
                Int::class -> IntegerValue(0)
                else -> throw IllegalArgumentException("FSM uses an input of an unknown type '${it.type}'")
            }
            IOElement(it.name, DataDirection.IN, value)
        }

        _usedOutputs = efsm.initialOutputValues
    }

    private fun replaceAlias(alias: String): String = ioAliases[alias] ?: alias

    override fun initialize(ioAccess: IOAccessLayer) {
        currentState = efsm.initialState

        logger.debug { "Init fsm program '$name'." }
        _inputAccessMap = _usedInputs.associate {
            it.identifier to
                    (ioAccess.getInput(replaceAlias(it.identifier), it.valueClass)
                        ?: throw NoSuchElementException("The efsm referenced the input '$it' that is doesn't exist at the PLC."))
        }
        _outputAccessMap = _usedOutputs.associate {
            it.identifier to
                    (ioAccess.getOutput(replaceAlias(it.identifier), it.valueClass)
                        ?: throw NoSuchElementException("The efsm referenced the output '$it' that is doesn't exist at the PLC."))
        }

        // put all timers
        _variableValues.clear()
        _variableValues.putAll(efsm.transitions.flatMap { transition -> transition.guard.getOperandsOfType(TimerVariable::class) }
            .associateWith { Constant.of(0) })
        // remaining vars
        _variableValues.putAll(efsm.variablesInitialValues.associate { it.ref to (it.value as Constant) })
        // clear timer resets
        timerResetMillis.clear()
    }

    private fun updateTimers(currentMillis: Long) {
        _variableValues.replaceAll { variable, value ->
            if (variable is TimerVariable) {
                Constant.of(currentMillis - (timerResetMillis[variable] ?: 0))
            } else {
                value
            }
        }
    }

    private fun provideValues(namedProvider: NamedDirectValueProvider<out Any>): Any {
        return when (namedProvider) {
            is BooleanEdgeInput -> _inputAccessMap[namedProvider.name]!!.edgePolarity == namedProvider.polarity
            is Input -> _inputAccessMap[namedProvider.name]!!.getValue().value
            is Variable -> _variableValues[namedProvider]!!.value
        }
    }

    override fun step(millis: Long) {
        updateTimers(millis)
        // Find the transition that will be followed
        val transition = efsm.transitions.filter { it.start == currentState }.firstOrNull {
            it.guard.computeValue(::provideValues)
        }

        if (transition != null) {// do the assignments etc. if there is a matching transition
            logger.info { "Take transition '$transition' @${millis}ms." }
            currentState = transition.end
            logger.info { "New state is '$currentState' @${millis}ms." }
            transition.varUpdates.forEach {

                if (it.ref is TimerVariable) {
                    timerResetMillis[it.ref] =
                        millis - (it.value.computeValue(::provideValues) as Long).also { newValue ->
                            logger.debug { "${millis}ms: Reset timer '${it.ref}' to $newValue." }
                        }
                } else {

                    val newValue = it.value.computeValue(::provideValues).let { value ->
                        when (value) {
                            is Long -> Constant.of(value)
                            is Boolean -> Constant.of(value)
                            is Double -> Constant.of(value)
                            else -> throw IllegalStateException("Unknown type when computing value for '${it.value}'")
                        }
                    }
                    _variableValues[it.ref] = newValue
                }
            }
            transition.outputs.forEach { outputAssignment ->
                _outputAccessMap[outputAssignment.output]?.setValue(outputAssignment.value)
            }
        }
    }

    override fun getUsedInputs(): Set<IOElement<Any>> =
        _usedInputs.map { it.copy(identifier = replaceAlias(it.identifier)) }.toSet()

    override fun getOutputs(): Set<IOElement<Any>> =
        _usedOutputs.map { it.copy(identifier = replaceAlias(it.identifier)) }.toSet()
}