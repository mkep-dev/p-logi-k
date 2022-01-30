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
 *
 * Representation of a variable to use it inside a formula
 * @param V the generic Type of the variable
 * @param type the name of the variable
 * @param name the name of the variable
 */
sealed class Variable<V : Any>(name: String, type: KClass<@UnsafeVariance V>) : NamedDirectValueProvider<V>(
    name, type
) {
    override fun toString(): String = "var(${name}:${typeHint()})"

    /**
     * Determine the type suffix
     *
     * @return the suffix "timer" if this is a timer otherwise the dataype of the variable
     */
    private fun typeHint(): String {
        return if (this is TimerVariable) {
            "timer"
        } else {
            type.simpleName ?: ""
        }
    }
}

class BooleanVariable(name: String) : Variable<Boolean>(name, Boolean::class) {

}

/**
 * This Variable reference represents a timer that will automatically count upwards.
 * It is not allowed to assign a value different zero.
 *
 * @constructor Simple constructor
 *
 * @param name name of the timer
 */
class TimerVariable(name: String) : Variable<Long>(name, Long::class) {

}

class LongVariable(name: String) : Variable<Long>(name, Long::class) {

}