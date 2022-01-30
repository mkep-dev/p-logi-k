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
 * An operand that directly provides the value without any computation e.g. a variable or a constant
 */
sealed class NamedDirectValueProvider<V : Any>(val name: String, type: KClass<@UnsafeVariance V>) :
    DirectValueProvider<V>(type) {

    // named value provider are vars or inputs that can change their value
    final override val isConstant: Boolean = false

    override fun computeValue(valueProvider: (NamedDirectValueProvider<out Any>) -> Any): V {
        val value = valueProvider.invoke(this)
        if (value::class != type) {
            throw OperandCastException("Tried to cast the '$value' returned value by value provider '$valueProvider' to type '$type'")
        }
        @Suppress("UNCHECKED_CAST") // See check above
        return value as V
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (!super.equals(other)) return false

        other as NamedDirectValueProvider<*>

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + isConstant.hashCode()
        return result
    }


}