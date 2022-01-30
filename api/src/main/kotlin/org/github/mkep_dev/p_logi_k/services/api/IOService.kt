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

import org.github.mkep_dev.p_logi_k.io.GenericValue
import org.github.mkep_dev.p_logi_k.io.TypeMismatchException
import org.github.mkep_dev.p_logi_k.model.io.IOElement
import org.github.mkep_dev.p_logi_k.model.io.IOReference

/**
 * Service for getting info about the
 */
interface IOService {

    /**
     * Returns the list of available IOs that the PLC can control / access.
     *
     * @return the list of available IOs that the PLC can control / access.
     */
    fun getIOMap(): List<IOReference<Any>>

    /**
     * Returns all IOs that match with the given regular expression.
     *
     * @param regex the regular expression
     * @return all IOs that match with the given regular expression.
     */
    fun getIOs(regex: Regex): Collection<IOElement<Any>>

    /**
     * Returns the value of the IO with the given name.
     *
     * @param name the name of the IO
     * @return the value of the IO with the given name. Null if no IO with the given name exists.
     */
    fun getIOValue(name: String): GenericValue<Any>?

    /**
     * Assign the given value to the input with the corresponding name
     *
     * @param name the name of the input
     * @param value the new value
     *
     * @throws NoSuchElementException if there is no input with the given name
     * @throws TypeMismatchException if the new value's datatype don't match with the specified input
     */
    @Throws(TypeMismatchException::class, NoSuchElementException::class)
    fun setInput(name: String, value: GenericValue<Any>)

    /**
     * Add subscription for input change events for inputs with a matching name for the given filter.
     *
     * @param filter the name filter
     * @param listener the listener for changes
     *
     * @return the ValueChangeSubscriptionHandle to manage the subscription. Null if there is no input with a matching name
     */
    fun addOnInputChangeListener(filter: Regex, listener: ValueChangeListener): ValueChangeSubscriptionHandle?

    /**
     * Add subscription for input change events for outputs with a matching name for the given filter.
     *
     * @param filter the name filter
     * @param listener the listener for changes
     *
     * @return the ValueChangeSubscriptionHandle to manage the subscription. Null if there is no output with a matching name
     */
    fun addOnOutputChangeListener(filter: Regex, listener: ValueChangeListener): ValueChangeSubscriptionHandle?

    /**
     * Interface for change listener
     */
    interface ValueChangeListener {
        /**
         * The change listener
         *
         * @param name the name of the IO
         * @param oldValue the value before the change
         * @param newValue the value after that change
         */
        fun onChange(name: String, oldValue: GenericValue<Any>, newValue: GenericValue<Any>)
    }

    /**
     * Handle for the value changes
     */
    interface ValueChangeSubscriptionHandle {

        /**
         * Stop the subscription
         */
        fun stop()
    }
}