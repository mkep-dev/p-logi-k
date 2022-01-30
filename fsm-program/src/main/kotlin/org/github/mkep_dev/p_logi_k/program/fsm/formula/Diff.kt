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
 * The representation of a *difference* operation. It takes two number and returns a number of the same domain.
 *
 * This equals "operand1 - operand2".
 *
 * @param lhsOperand the first operand.
 * @param rhsOperand the second operand.
 */
class Diff<T : Number>(lhsOperand: Operand<T>, rhsOperand: Operand<T>) :
    BiArgumentOperator<T, T>(lhsOperand, rhsOperand, lhsOperand.type) {
    override val operationString: String = "-"

    override fun computeValue(valueProvider: (NamedDirectValueProvider<out Any>) -> Any): T {
        @Suppress("UNCHECKED_CAST") // We know it the type for casting because of our when check
        return lhsOperand.computeValue(valueProvider).let {
            when (it) {
                is Long -> it.toLong() - (rhsOperand as Operand<Long>).computeValue(valueProvider)
                is Double -> it.toDouble() - (rhsOperand as Operand<Double>).computeValue(valueProvider)
                else -> assert(false) { "Impossible" }
            }
        } as T
    }

    companion object {
        operator fun <T : Number> Operand<T>.minus(other: Operand<T>) = Diff(this, other)
        operator fun Operand<Long>.minus(other: Long) = Diff(this, Constant.of(other))
        operator fun Long.minus(other: Operand<Long>) = Diff(Constant.of(this), other)
    }
}