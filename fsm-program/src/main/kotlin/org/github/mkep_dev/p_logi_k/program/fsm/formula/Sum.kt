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

/**
 * *Sum* Operator with multiple arguments.
 * Representation of an operand that is itself a combination of two operands and an operator.
 *
 * This equals to "element_0 **+** element_1 **+** ... **+** element_n".
 *
 * @param operands the operands that are part of this operator. The list must contain at least one operand.
 */
class Sum<T : Number>(operands: List<Operand<T>>) :
    MultiArgumentOperator<@UnsafeVariance T, T>(operands, operands.first().type) {

    constructor(vararg operands: Operand<T>) : this(operands.toList())

    override val operationString: String = "sum"

    override fun computeValue(valueProvider: (NamedDirectValueProvider<out Any>) -> Any): T {
        @Suppress("UNCHECKED_CAST") // We know it is the correct type because of our when check
        return when (operands.first().type) {
            Long::class -> {
                operands.sumOf { (it as Operand<Long>).computeValue(valueProvider) }
            }
            Double::class -> {
                operands.sumOf { (it as Operand<Double>).computeValue(valueProvider) }
            }
            else -> {
                assert(false) { "Impossible" }
                0
            }
        } as T
    }

    companion object {
        operator fun <T : Number> Operand<T>.plus(other: Operand<T>) = Sum(this, other)
        operator fun Operand<Long>.plus(other: Long) = Sum(this, Constant.of(other))
        operator fun Long.plus(other: Operand<Long>) = Sum(Constant.of(this), other)
    }
}