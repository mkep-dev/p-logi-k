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

import org.github.mkep_dev.p_logi_k.Identifier
import org.github.mkep_dev.p_logi_k.io.GenericInput
import org.github.mkep_dev.p_logi_k.io.GenericOutput
import kotlin.reflect.KClass

/**
 * Allows to access the IO from the program
 *
 * The GenericDataHolder references to IOs from this access layer can be reused and will always contain the latest value
 *
 * Limitations:
 * - inputs are read only
 */
interface IOAccessLayer {

    /**
     * Returns the output with the given name and type
     *
     * @param T the type of the output
     * @param id the id of the output
     * @param type the [KClass] representation of the datatype
     * @return the output reference. Null if no corresponding output could be found.
     */
    fun <T : Any> getOutput(id:Identifier, type: KClass<T>):GenericOutput<T>?

    /**
     * Returns the input with the given name and type
     *
     * @param T the type of the input
     * @param id the id of the input
     * @param type the [KClass] representation of the datatype
     * @return the input reference. Null if no corresponding input could be found.
     */
    fun <T : Any> getInput(id: Identifier, type: KClass<T>): GenericInput<T>?
}