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

package org.github.mkep_dev.p_logi_k.examples.garageDoor

import org.github.mkep_dev.p_logi_k.io.BooleanValue
import org.github.mkep_dev.p_logi_k.model.io.DataDirection
import org.github.mkep_dev.p_logi_k.model.io.IOElement
import org.github.mkep_dev.p_logi_k.model.io.IoEdgePolarity
import org.github.mkep_dev.p_logi_k.program.fsm.EFSM
import org.github.mkep_dev.p_logi_k.program.fsm.OutputAssignment
import org.github.mkep_dev.p_logi_k.program.fsm.State
import org.github.mkep_dev.p_logi_k.program.fsm.Transition
import org.github.mkep_dev.p_logi_k.program.fsm.VariableAssignment.Companion.becomes
import org.github.mkep_dev.p_logi_k.program.fsm.formula.*
import org.github.mkep_dev.p_logi_k.program.fsm.formula.And.Companion.and
import org.github.mkep_dev.p_logi_k.program.fsm.formula.GreaterEquals.Companion.ge
import org.github.mkep_dev.p_logi_k.program.fsm.formula.Not.Companion.not
import kotlin.time.Duration.Companion.seconds

object GarageDoorEfsm {

    private val falseValue = BooleanValue(false)
    private val trueValue = BooleanValue(true)

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

}