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

package org.github.mkep_dev.p_logi_k.services.grpc

import org.github.mkep_dev.p_logi_k.io.GenericValue
import org.github.mkep_dev.p_logi_k.model.io.DataDirection
import org.github.mkep_dev.p_logi_k.services.grpc.stub.Direction

@Throws(IllegalStateException::class, IllegalArgumentException::class)
internal fun mapMsgToIoElement(ioElement: org.github.mkep_dev.p_logi_k.services.grpc.stub.IOElement): org.github.mkep_dev.p_logi_k.model.io.IOElement<Any> {
    val direction = when (ioElement.direction) {
        Direction.IN -> DataDirection.IN
        Direction.OUT -> DataDirection.OUT
        else -> throw IllegalArgumentException("Direction can't be mapped")
    }
    val value = GenericValueHelper.mapMsgToGenericValue(ioElement.value)
        ?: throw IllegalArgumentException("Value can't be mapped to generic value because the data type is unknown.")
    @Suppress("UNCHECKED_CAST") // Casts are checked by when statement
    return when (value.valueClass) {
        Boolean::class -> org.github.mkep_dev.p_logi_k.model.io.IOElement(
            ioElement.id,
            direction,
            value as GenericValue<Boolean>
        )
        Int::class -> org.github.mkep_dev.p_logi_k.model.io.IOElement(
            ioElement.id,
            direction,
            value as GenericValue<Int>
        )
        Double::class -> org.github.mkep_dev.p_logi_k.model.io.IOElement(
            ioElement.id,
            direction,
            value as GenericValue<Double>
        )
        else -> throw IllegalStateException("The data type of generic value is unknown.")
    }
}