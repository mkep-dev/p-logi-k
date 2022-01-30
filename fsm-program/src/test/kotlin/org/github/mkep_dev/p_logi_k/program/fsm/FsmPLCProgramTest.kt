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

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.github.mkep_dev.p_logi_k.io.BooleanValue
import org.github.mkep_dev.p_logi_k.io.GenericValue
import org.github.mkep_dev.p_logi_k.model.io.BasicIOCard
import org.github.mkep_dev.p_logi_k.model.io.DataDirection
import org.github.mkep_dev.p_logi_k.model.io.IOElement
import org.github.mkep_dev.p_logi_k.model.io.IoEdgePolarity
import org.github.mkep_dev.p_logi_k.program.fsm.VariableAssignment.Companion.becomes
import org.github.mkep_dev.p_logi_k.program.fsm.formula.*
import org.github.mkep_dev.p_logi_k.program.fsm.formula.And.Companion.and
import org.github.mkep_dev.p_logi_k.program.fsm.formula.GreaterEquals.Companion.ge
import org.github.mkep_dev.p_logi_k.program.fsm.formula.Not.Companion.not
import org.github.mkep_dev.p_logi_k.services.api.IOService
import org.github.mkep_dev.p_logi_k.starter.PLCMaster
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

internal class FsmPLCProgramTest {


    private val falseValue = BooleanValue(false)
    private val trueValue = BooleanValue(true)

    @Test
    fun test() = runBlocking {
        val stop = State("STOP")
        val down = State("DOWN")
        val up = State("UP")

        val timer = TimerVariable("timer1")

        val btn_up = BooleanEdgeInput("btn_up", IoEdgePolarity.RISING)
        val btn_down = BooleanEdgeInput("btn_down", IoEdgePolarity.RISING)
        val hit_bar = BooleanInput("hit_bar")
        val end_down = BooleanInput("end_down")
        val end_up = BooleanInput("end_up")

        val motor_down = IOElement("motor_down", DataDirection.OUT, falseValue)
        val motor_up = IOElement("motor_up", DataDirection.OUT, falseValue)


        val transitions = listOf<Transition>(
            Transition(
                stop,
                down,
                And(!btn_up, btn_down, !hit_bar, !end_down, timer ge Constant.of(1.seconds.inWholeMilliseconds)),
                listOf(OutputAssignment(motor_down.identifier, trueValue))
            ),
            Transition(
                stop,
                up,
                And(btn_up, !btn_down, !hit_bar, !end_up, timer ge Constant.of(1.seconds.inWholeMilliseconds)),
                listOf(OutputAssignment(motor_up.identifier, trueValue))
            ),
            Transition(
                down,
                up,
                hit_bar,
                listOf(
                    OutputAssignment(motor_down.identifier, falseValue),
                    OutputAssignment(motor_up.identifier, trueValue)
                )
            ),
            Transition(
                down,
                stop,
                Or(btn_up and !btn_down, end_down),
                listOf(
                    OutputAssignment(motor_down.identifier, falseValue),
                    OutputAssignment(motor_up.identifier, falseValue)
                ),
                listOf(timer.becomes(Constant.of(0)))
            ),
            Transition(
                up,
                stop,
                Or(!btn_up and btn_down, end_up),
                listOf(
                    OutputAssignment(motor_down.identifier, falseValue),
                    OutputAssignment(motor_up.identifier, falseValue)
                ),
                listOf(timer.becomes(Constant.of(0)))
            ),
        )

        val efsm = EFSM(listOf(stop, up, down), transitions, stop, listOf(motor_down, motor_up), listOf())

        val aliases = mapOf(
            btn_up.name to "bIO0/in/1",
            btn_down.name to "bIO0/in/2",
            hit_bar.name to "bIO0/in/3",
            end_up.name to "bIO0/in/4",
            end_down.name to "bIO0/in/5",
            motor_up.identifier to "bIO0/out/1",
            motor_down.identifier to "bIO0/out/2",
        )

        val program = FsmPLCProgram("garageDoor", efsm, aliases)

        val master = PLCMaster(listOf(BasicIOCard(0)), 50)

        master.simulationClockService.setAutoTicking(true)
        master.plcProgramMemory.addProgram(program) shouldBe true
        master.plcExecutionService.loadProgram(program.name) shouldBe true

        master.ioService.addOnOutputChangeListener(".*".toRegex(), object : IOService.ValueChangeListener {
            override fun onChange(name: String, oldValue: GenericValue<Any>, newValue: GenericValue<Any>) {
                println("Value of '$name' changed from '${oldValue.value}' to '${newValue.value}'.")
            }
        })

        // Start
        master.plcExecutionService.goOn()

        val ioService = master.ioService

        // wait until stop can be left
        delay(1000)

        // send door upwards
        ioService.setInput(aliases[btn_up.name]!!, trueValue)
        ioService.setInput(aliases[btn_down.name]!!, falseValue)

        // wait to move the door
        delay(100)
        ioService.getIOValue(aliases[motor_down.identifier]!!)?.value shouldBe false
        ioService.getIOValue(aliases[motor_up.identifier]!!)?.value shouldBe true

        ioService.setInput(aliases[end_up.name]!!, trueValue)

        delay(500)

        ioService.getIOValue(aliases[motor_down.identifier]!!)?.value shouldBe false
        ioService.getIOValue(aliases[motor_up.identifier]!!)?.value shouldBe false
        // reset end
        ioService.setInput(aliases[end_up.name]!!, falseValue)

        // we have to wait at least 1 second
        delay(1000)

        // move door downwards
        ioService.setInput(aliases[btn_up.name]!!, falseValue)
        ioService.setInput(aliases[btn_down.name]!!, trueValue)

        delay(100)

        ioService.getIOValue(aliases[motor_down.identifier]!!)?.value shouldBe true
        ioService.getIOValue(aliases[motor_up.identifier]!!)?.value shouldBe false

        delay(500)

        ioService.setInput(aliases[hit_bar.name]!!, trueValue)
        delay(100)
        ioService.getIOValue(aliases[motor_down.identifier]!!)?.value shouldBe false
        ioService.getIOValue(aliases[motor_up.identifier]!!)?.value shouldBe true


    }

}