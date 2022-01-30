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
 * Operator with two arguments
 * Representation of an operand that is itself a combination of two operands and an operator
 *
 * @param T The input/operand type of the operator
 * @param V The output/result type of the operator
 *
 * @param type the type class reference
 *
 * @property lhsOperand left-hand side operand
 * @property rhsOperand right-hand side operand
 *
 */
sealed class BiArgumentOperator<T : Any, V : Any>(
    val lhsOperand: Operand<T>,
    val rhsOperand: Operand<T>,
    type: KClass<@UnsafeVariance V>
) : Operator<T, V>(lhsOperand.type, type) {

    override val successors: List<Operand<out T>>
        get() = listOf(lhsOperand, rhsOperand)

    override fun toString(): String = "($lhsOperand $operationString $rhsOperand)"


}