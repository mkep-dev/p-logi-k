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

import org.github.mkep_dev.p_logi_k.model.io.IOElement
import org.github.mkep_dev.p_logi_k.program.fsm.formula.TimerVariable
import org.github.mkep_dev.p_logi_k.program.fsm.formula.Variable

data class EFSM(
    val states: List<State>,
    val transitions: List<Transition>,
    val initialState: State,
    val initialOutputValues: List<IOElement<Any>>,
    val variablesInitialValues: List<VariableAssignment<out Any>>
) {

    init {
        require(states.contains(this.initialState)) { "Initial state must be part of state list" }
        require(states.containsAll(transitions.flatMap {
            listOf(
                it.start,
                it.end
            )
        })) { "Every state in transition must be part of state list." }
        require(states.groupBy { it.id }.none { entry -> entry.value.size > 1 }) { "Duplicate state identifiers" }
        require(initialOutputValues.all { it.direction.isOutput() }) { "Initial value for input was set." }
        require(initialOutputValues.map { it.identifier }
            .containsAll(transitions.flatMap { transition -> transition.outputs.map { it.output } })) {
            "Missing initial value definition for ${
                transitions.flatMap { transition -> transition.outputs }
                    .filter { initialOutputValues.none { initDef -> initDef.identifier == it.output } }.joinToString()
            }."
        }

        // constant assignments
        require(variablesInitialValues.all { it.value.isConstant })

        require(variablesInitialValues.map { it.ref }.containsAll(transitions.flatMap { transition ->
            transition.varUpdates.map { it.ref } + transition.guard.getOperandsOfType(
                Variable::class
            )
        }.filter { it !is TimerVariable })) {
            "Missing initial value definition for ${
                transitions.flatMap { transition ->
                    transition.varUpdates.map { it.ref } + transition.guard.getOperandsOfType(
                        Variable::class
                    )
                }.filter { it !is TimerVariable && variablesInitialValues.none { initDef -> initDef.ref == it } }.distinct().joinToString()
            }."
        }
    }


}