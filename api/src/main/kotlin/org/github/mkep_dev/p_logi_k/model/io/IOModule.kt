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

package org.github.mkep_dev.p_logi_k.model.io

import org.github.mkep_dev.p_logi_k.Identifiable
import org.github.mkep_dev.p_logi_k.Identifier
import org.github.mkep_dev.p_logi_k.io.GenericValue
/**
 * Representation of an io module that offers multiple inputs and outputs to the PLC
 * Those ios can "real" ios or e.g. opc ua variables
 */
interface IOModule: Identifiable {

    fun addOnValueChangeListener(onChange: ((id: Identifier, old: GenericValue<*>, new: GenericValue<*>, direction: DataDirection) -> Unit)): OnIOChangeSubscription

    /**
     * Handle for the value changes
     */
    interface OnIOChangeSubscription {
        /**
         * Stop the subscription
         */
        fun stop()
    }

    /**
     * Write the new value to the given output
     */
    fun writeOutputValue(id:Identifier, newValue: GenericValue<*>)

    /**
     * Returns a list of all provided IOs.
     *
     * The implementation must always return the same result.
     */
    val providedIOs : Collection<IOElement<Any>>
        get() = listOf()

}