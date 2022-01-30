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

package org.github.mkep_dev.p_logi_k.services

import kotlinx.coroutines.runBlocking
import org.github.mkep_dev.p_logi_k.model.PLC
import org.github.mkep_dev.p_logi_k.model.PLCExecutionState
import org.github.mkep_dev.p_logi_k.program.api.PLCProgramMemory
import org.github.mkep_dev.p_logi_k.program.api.PLCProgramMeta
import org.github.mkep_dev.p_logi_k.services.api.PLCExecutionService

/**
 * Implementation of a [PLCExecutionService] that uses the PLC and the program memory to offer the defined methods.
 *
 * @property plc the PLC
 * @property programMemory the program memroy
 */
class PLCExecutionFacade(private val plc: PLC, private val programMemory: PLCProgramMemory) : PLCExecutionService {

    override fun reset() = runBlocking {
        plc.reset()
    }


    override fun pause() = runBlocking {
        plc.pause()
    }

    override fun resetAndPause() = runBlocking {
        plc.resetAndPause()
    }

    override fun goOn() = runBlocking {
        plc.goOn()
    }

    override fun getState(): PLCExecutionState {
        return plc.state
    }

    override fun loadProgram(name: String): Boolean {
        programMemory.getProgram(name)?.let {
            return runBlocking {
                plc.loadProgram(it)
            }
        } ?: return false
    }

    override fun getCurrentProgramName(): String? {
        return plc.getCurrentProgram()?.name
    }

    override fun listProgramNames(): List<String> {
        return programMemory.getAvailablePrograms().map { it.name }
    }

    override fun getProgramMeta(name: String): PLCProgramMeta? {
        return programMemory.getProgram(name)
    }
}