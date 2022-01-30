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

package org.github.mkep_dev.p_logi_k.model

import kotlinx.coroutines.flow.StateFlow
import org.github.mkep_dev.p_logi_k.program.api.CyclicPLCProgram
import org.github.mkep_dev.p_logi_k.time.Clock

/**
 * The basic interface any plc
 *
 */
interface PLC {

    /**
     * The PLC clock
     */
    val clock: Clock

    /**
     * The currently active execution state
     */
    val state: PLCExecutionState

    /**
     * Update flow for execution state
     */
    val stateFlow: StateFlow<PLCExecutionState>

    /**
     * Resets the whole PLC and start over again
     */
    suspend fun reset()

    /**
     * Stops the PLC and forces staying in the current moment
     * The currently active cycle will be finished and the paused
     * Does nothing if program is paused
     */
    suspend fun pause()

    /**
     * Fire a reset and pause before executing the program again
     */
    suspend fun resetAndPause()

    /**
     * Continue with the execution of the program.
     * Does nothing if program is already running
     */
    suspend fun goOn()

    /**
     * Load the given program.
     *
     * This triggers a pause event at first then loads the program and afterwards the PLC is reset.
     * So if the load fails the PLC is in state PAUSED
     *
     * @param plcProgram the plc program
     * @return true if program loaded successfully. Might be false if program uses ios that doesn't exist
     *
     */
    suspend fun loadProgram(plcProgram: CyclicPLCProgram): Boolean

    /**
     * Returns the currently active program.
     * @return the currently active program. Null if no program is active
     */
    fun getCurrentProgram(): CyclicPLCProgram?
}