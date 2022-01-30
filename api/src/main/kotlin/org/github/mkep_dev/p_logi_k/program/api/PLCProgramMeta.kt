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

import org.github.mkep_dev.p_logi_k.model.io.IOElement

/**
 * The meta information interface for a PLC program
 *
 */
interface PLCProgramMeta {

    /**
     * The name of the program (must be unique)
     */
    val name: String

    /**
     * The used inputs of the program.
     * If the program uses inputs that are not specified, the result behaviour is unknown but errors are more than possible.
     *
     * @return the set of used inputs
     */
    fun getUsedInputs(): Set<IOElement<Any>>

    /**
     * The used outputs inside the program and their default value. If no value is assigned during the program execution the default value is applied.
     * If the program uses outputs that are not specified, the result behaviour is unknown but errors are more than possible.
     *
     * @return the set of used outputs
     */
    fun getOutputs(): Set<IOElement<Any>>

    /**
     * Returns all used inputs / outputs.
     *
     * @return all used inputs / outputs.
     */
    fun getIOs(): Set<IOElement<Any>> = getOutputs() + getUsedInputs()

}