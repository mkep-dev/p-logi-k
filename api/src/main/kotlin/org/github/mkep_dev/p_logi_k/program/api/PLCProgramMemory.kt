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

package org.github.mkep_dev.p_logi_k.program.api


/**
 * The program storage of the plc
 *
 */
interface PLCProgramMemory {

    /**
     * Add the given program to the memory of available programs
     *
     * @param program
     * @return false if the program can't be executed
     */
    fun addProgram(program: CyclicPLCProgram): Boolean

    /**
     * List all available programs inside the memory
     *
     * @return all available programs inside the memory
     */
    fun getAvailablePrograms(): Collection<PLCProgramMeta>

    /**
     * Returns the program with the given name
     *
     * @param name the name of the program
     * @return the program with the given name. Null if no program with the given name exists.
     */
    fun getProgram(name: String): CyclicPLCProgram?

}