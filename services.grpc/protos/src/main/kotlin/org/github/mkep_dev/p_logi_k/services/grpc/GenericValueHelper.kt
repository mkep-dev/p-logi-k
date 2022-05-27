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

import org.github.mkep_dev.p_logi_k.io.BooleanValue
import org.github.mkep_dev.p_logi_k.io.DoubleValue
import org.github.mkep_dev.p_logi_k.io.GenericValue
import org.github.mkep_dev.p_logi_k.io.LongValue
import org.github.mkep_dev.p_logi_k.services.grpc.stub.Type
import org.github.mkep_dev.p_logi_k.services.grpc.stub.genericValue

object GenericValueHelper {

    fun mapGenericValueToMsg(ioValue: GenericValue<Any>) = genericValue {
        when (ioValue) {
            is BooleanValue -> {
                value = when (ioValue.value) {
                    true -> 1.0
                    false -> -1.0
                }
                type = Type.BOOLEAN
            }
            is LongValue -> {
                value = ioValue.value.toDouble()
                type = Type.INTEGER
            }
            is DoubleValue -> {
                value = ioValue.value
                type = Type.DOUBLE
            }
            else -> {
                type = Type.UNKNOWN
            }
        }
    }

    fun mapMsgToGenericValue(msg: org.github.mkep_dev.p_logi_k.services.grpc.stub.GenericValue): GenericValue<Any>? {
        return when (msg.type) {
            Type.BOOLEAN -> BooleanValue(msg.value > 0)
            Type.INTEGER -> LongValue(msg.value.toLong())
            Type.DOUBLE -> throw NotImplementedError("Double values are yet not supported. But prepared in msgs.")
            Type.UNKNOWN, Type.UNRECOGNIZED, null -> null
        }
    }
}