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

package org.github.mkep_dev.p_logi_k.services.api

import org.github.mkep_dev.p_logi_k.model.PLCExecutionState
import org.github.mkep_dev.p_logi_k.program.api.PLCProgramMeta

/**
 * Service to control the execution of the PLC programs.
 */
interface PLCExecutionService {

    /**
     * Resets the whole PLC and start over again
     */
    fun reset()

    /**
     * Stops the PLC and forces staying in the current moment
     * The currently active cycle will be finished and the paused
     * Does nothing if program is paused
     */
    fun pause()

    /**
     * Fire a reset and pause before executing the program again
     */
    fun resetAndPause()

    /**
     * Continue with the execution of the program.
     * Does nothing if program is already running
     */
    fun goOn()

    /**
     * Return the currently active execution state
     */
    fun getState() : PLCExecutionState

    /**
     * Load the program with the given name.
     *
     * This triggers a pause event at first then loads the program and afterwards the PLC is reset.
     * So if the load fails the PLC is in state PAUSED
     *
     * @param name The name of the program
     *
     * @return whether to program could be loaded. If false the program does not exist.
     */
    fun loadProgram(name: String) : Boolean

    /**
     * Returns the currently active program.
     * @return the currently active program. Null if no program is active
     */
    fun getCurrentProgramName() : String?

    /**
     * List all programs that are available
     */
    fun listProgramNames() : List<String>

    /**
     * Returns info for the given program.
     * @param name The name of the program
     * @return metadata of the program. Null if no program with the given name is available
     */
    fun getProgramMeta(name: String) : PLCProgramMeta?
}