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

import kotlin.reflect.KClass

/**
 * Abstract parent for all allowed value holder
 *
 * @param V the internal datatype
 * @property valueClass the [KClass] reference for the value type
 * @property value the value
 */
sealed class GenericValue<out V : Any>(val valueClass: KClass<@UnsafeVariance V>, val value: V) {


    override fun equals(other: Any?): Boolean {
        return if (other is GenericValue<*>) {
            this.valueClass == other.valueClass && this.value == other.value
        } else {
            super.equals(other)
        }
    }

    override fun toString(): String {
        return "Value(value=$value, type=$valueClass)"
    }

    override fun hashCode(): Int {
        var result = valueClass.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    /**
     * Casts the value to an integer value
     *
     * @return the integer value
     */
    @Throws(IllegalStateException::class)
    fun toInteger() =
        IntegerValue(
            when (valueClass) {
                Int::class -> {
                    value as Int
                }
                Boolean::class -> when (value as Boolean) {
                    false -> 0
                    else -> 1
                }
                else -> throw IllegalStateException() // Logical impossible
            }
        )


    /**
     * Casts the value to a boolean value
     * @return the boolean value
     */
    @Throws(IllegalStateException::class)
    fun toBoolean() =
        BooleanValue(
            when (valueClass) {
                Int::class -> {
                    (value as Int) != 0
                }
                Boolean::class -> value as Boolean
                else -> throw IllegalStateException() // Logical impossible
            }
        )

    companion object {
        /**
         * Create a boolean value using the given boolean
         *
         * @param value the boolean
         * @return the [GenericValue] representation
         */
        fun of(value: Boolean) = BooleanValue(value)
        /**
         * Create a boolean value using the given boolean
         *
         * @param value the integer
         * @return the [GenericValue] representation
         */
        fun of(value: Int) = IntegerValue(value)
    }

}

/**
 * A boolean value
 *
 * @constructor
 * create a value using the given boolean
 *
 * @param value the boolean
 */
class BooleanValue(value: Boolean) : GenericValue<Boolean>(Boolean::class, value)

/**
 * A integer value
 *
 * @constructor
 * create a value using the given int
 *
 * @param value the int
 */
class IntegerValue(value: Int) : GenericValue<Int>(Int::class, value)

// Functions
operator fun GenericValue<Boolean>.not(): GenericValue<Boolean> = BooleanValue(!value)

/**
 * And operator
 *
 * @param other the other boolean
 * @return the resulting boolean value
 */
infix fun GenericValue<Boolean>.and(other: GenericValue<Boolean>): GenericValue<Boolean> =
    BooleanValue(value && other.value)

/**
 * or operator
 *
 * @param other the other boolean
 * @return the resulting boolean value
 */
infix fun GenericValue<Boolean>.or(other: GenericValue<Boolean>): GenericValue<Boolean> =
    BooleanValue(value || other.value)


