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
 * A constant of type [T] that value will never change
 */
sealed class Constant<T : Any>(val value: T, type: KClass<@UnsafeVariance T>) : DirectValueProvider<T>(type) {

    // named value provider are constants that can't change their value
    final override val isConstant: Boolean = true

    override fun toString(): String =
        "$value"

    override fun computeValue(valueProvider: (NamedDirectValueProvider<out Any>) -> Any): T = value

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (!super.equals(other)) return false

        other as Constant<*>

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + isConstant.hashCode()
        return result
    }


    companion object {
        fun of(b: Boolean) = BooleanConstant(b)
        fun of(i: Int) = LongConstant(i.toLong())
        fun of(l: Long) = LongConstant(l)
        fun of(d: Double) = DoubleConstant(d)
    }
}

class BooleanConstant(value: Boolean) : Constant<Boolean>(value, Boolean::class) {
    companion object {
        val TRUE = BooleanConstant(true)
        val FALSE = BooleanConstant(false)
    }
}

class LongConstant(value: Long) : Constant<Long>(value, Long::class)

class DoubleConstant(value: Double) : Constant<Double>(value, Double::class)

