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

/**
 * A data holder that can store values and can be overwritten.
 *
 * @param V the dataype of the values
 */
interface GenericWritableDataHolder<V:Any> : GenericDataHolder<V>  {

    /**
     * Set a new value.
     *
     * @param value the new value
     * @throws TypeMismatchException if the given type doesn't match with datatype of the data holder
     */
    @Throws(TypeMismatchException::class)
    fun setValue(value: GenericValue<V>)

    /**
     * Update the value using a transform function.
     *
     * @param transform the transform function
     * @throws TypeMismatchException if the given type doesn't match with datatype of the data holder
     */
    @Throws(TypeMismatchException::class)
    fun updateValue(transform: (value: GenericValue<V>) -> GenericValue<V>){
        setValue(transform(getValue()))
    }
}