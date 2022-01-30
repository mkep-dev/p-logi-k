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
 * Basic interface for a PLC program that runs cyclic.
 * The inputs are scanned at the beginning and the output changes are applied at the end of the program.
 * If you assign values sequentially to an output the last one is applied. No changes are applied during the step.
 */
interface CyclicPLCProgram : PLCProgramMeta {

    /**
     * This function is called when the program is loaded.
     * Changes of the output will be ignored.
     * @param ioAccess The interface to access the IOs
     */
    fun initialize(ioAccess: IOAccessLayer)

    /**
     * Periodically called function.
     * The changes of the IOs won't be registered or applied during the execution of the method
     * @param millis current system time
     */
    fun step(millis: Long)

}