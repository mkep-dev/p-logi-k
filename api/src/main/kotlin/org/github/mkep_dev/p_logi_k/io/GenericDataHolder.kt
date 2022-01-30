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

package org.github.mkep_dev.p_logi_k.io

import org.github.mkep_dev.p_logi_k.Identifiable
import kotlin.reflect.KClass

/**
 * Representation of a data holder that can store values and has a unique identifier
 */
interface GenericDataHolder<out V:Any> : Identifiable {

    /**
     * Returns the current value of the data holder
     *
     * @return the current value
     */
    fun getValue():GenericValue<V>

    /**
     * The value class of the object
     */
    val valueClass:KClass<@UnsafeVariance V>

    override fun equals(other:Any?):Boolean

}