package org.github.mkep_dev.p_logi_k.program.fsm.formula

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

import kotlin.reflect.KClass

/**
 * Representation of an operand that is itself a combination of a number of operands and an operator
 *
 * @param T The input/operand type of the operator
 * @param V The output/result type of the operator
 *
 * @param type the type class reference
 *
 * @property inputType the input type class
 */
sealed class Operator<T : Any, V : Any>(val inputType: KClass<@UnsafeVariance T>, type: KClass<@UnsafeVariance V>) :
    Operand<V>(type) {
    /**
     * The list of direct successors/operands inside this operator
     */
    abstract val successors: List<Operand<out T>>

    /**
     * The string representation of the operator
     */
    abstract val operationString: String

    final override val isConstant: Boolean
        get() = successors.all { it.isConstant }

    override fun getAllDirectValueProvidersMatching(selector: (DirectValueProvider<out Any>) -> Boolean): List<DirectValueProvider<out Any>> {
        return successors.flatMap { it.getAllDirectValueProvidersMatching(selector) }
    }

    private infix fun <E : Any> List<E>.concatWith(new: E?): List<E> {
        if (new == null) {
            return this
        } else {
            return this + new
        }
    }

    override fun <T : Operand<*>> getOperandsOfType(searchedType: KClass<T>): List<T> {
        return successors.flatMap { it.getOperandsOfType(searchedType) } concatWith this.takeIf {
            searchedType.isInstance(
                this
            )
        }?.let {
            @Suppress("UNCHECKED_CAST") // checked by tyke if isInstance
            it as T
        }
    }

    override fun toString(): String = "$operationString(${successors.joinToString(",") { it.toString() }})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (!super.equals(other)) return false

        other as Operator<*, *>

        if (successors != other.successors) return false
        if (operationString != other.operationString) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + successors.hashCode()
        result = 31 * result + operationString.hashCode()
        return result
    }


}