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
 * Representation of the modulo operation.
 *
 * This equals the expression "operand1 **%** operand2".
 *
 * @param lhsOperand the first operand. It can be an Integer or a real value.
 * @param rhsOperand the second operand. It has to be an Integer.
 */
class Modulo<T : Number>(lhsOperand: Operand<T>, rhsOperand: Constant<Long>) :
    BiArgumentOperator<T, T>(
        lhsOperand, @Suppress("UNCHECKED_CAST") (if (lhsOperand.type == Double::class) {
            Constant.of(rhsOperand.value.toDouble())
        } else {
            rhsOperand
        } as Operand<T>), lhsOperand.type
    ) {
    override val operationString: String = "%"

    override fun computeValue(valueProvider: (NamedDirectValueProvider<out Any>) -> Any): T {
        @Suppress("UNCHECKED_CAST") // We know it the type for casting because of our when check
        return lhsOperand.computeValue(valueProvider).let {
            when (it) {
                is Long -> it.toLong() % (rhsOperand as Constant<Long>).value
                is Double -> it.toDouble() % (rhsOperand as Constant<Double>).value
                else -> {
                    assert(false) { "Impossible" }
                    0
                }
            }
        } as T
    }

    companion object {
        operator fun <T : Number> Operand<T>.rem(other: Constant<Long>) = Modulo(this, other)
        operator fun <T : Number> Operand<T>.rem(other: Long) = Modulo(this, Constant.of(other))
    }
}