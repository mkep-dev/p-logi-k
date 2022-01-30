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


@file:JvmName("StartKt")

package org.github.mkep_dev.p_logi_k.examples.garageDoor

import kotlinx.coroutines.runBlocking
import org.github.mkep_dev.p_logi_k.model.io.BasicIOCard
import org.github.mkep_dev.p_logi_k.starter.PLCMaster


fun main(args: Array<String>) = runBlocking {
    val master = PLCMaster(listOf(BasicIOCard(0)), 50)
    val program = GarageDoorProgram()
    master.plcProgramMemory.addProgram(program)
    val sim =
        GarageDoorSimulation( 1.0, master.ioService, master.simulationClockService, master.plcExecutionService)
    sim.start(program.name)
}