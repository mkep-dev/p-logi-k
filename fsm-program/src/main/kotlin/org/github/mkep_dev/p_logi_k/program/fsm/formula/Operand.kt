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

package org.github.mkep_dev.p_logi_k.program.fsm.formula

import kotlin.reflect.KClass

/**
 * Representation of any kind of Operand that holds values of type [V]
 *
 * @param V
 * @property type
 */
sealed class Operand<V : Any>(val type: KClass<@UnsafeVariance V>) {

    abstract val isConstant: Boolean

    abstract override fun toString(): String

    /**
     * Computes the value using the given value provider to replace the variables and inputs.
     *
     * @param valueProvider
     * @return the computed value
     * @throws ArithmeticException when the computation fails because of a prohibited computation operation e.g. division by zero
     */
    @Throws(ArithmeticException::class, OperandCastException::class)
    abstract fun computeValue(valueProvider: (NamedDirectValueProvider<out Any>) -> Any): V


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Operand<*>

        if (type != other.type) return false
        if (isConstant != other.isConstant) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + isConstant.hashCode()
        return result
    }

    abstract fun getAllDirectValueProvidersMatching(selector: (DirectValueProvider<out Any>) -> Boolean): List<DirectValueProvider<out Any>>

    abstract fun <T : Operand<*>> getOperandsOfType(searchedType: KClass<T>): List<T>

}

