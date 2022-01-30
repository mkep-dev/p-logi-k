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
 * Representation of an operand that is itself a combination of a single operand and an operator.
 *
 * @param T The input/operand type of the operator
 * @param V The output/result type of the operator
 *
 * @param inputType the input type class reference
 * @param type the type class reference
 */
sealed class UnaryOperator<T : Any, V : Any>(inputType: KClass<@UnsafeVariance T>, type: KClass<@UnsafeVariance V>) :
    Operator<T, V>(inputType, type) {

    /**
     * The included operand of the operator.
     */
    abstract val operand: Operand<T>

    override val successors: List<Operand<T>>
        get() = listOf(operand)

    override fun toString(): String = "$operationString$operand"

}