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
 * The representation of a *division* operation. It takes two number and returns a double.
 *
 * This equals "operand1 / operand2".
 *
 * @property lhsOperand the first operand.
 * @property rhsOperand the second operand.
 */
class Divide<T : Number>(lhsOperand: Operand<T>, rhsOperand: Operand<T>) :
    BiArgumentOperator<T, Double>(lhsOperand, rhsOperand, Double::class) {
    override val operationString: String = "/"

    override fun computeValue(valueProvider: (NamedDirectValueProvider<out Any>) -> Any): Double {
        return lhsOperand.computeValue(valueProvider).let {
            @Suppress("UNCHECKED_CAST") // We know it the type for casting because of our when check
            val rhsValue = when (it) {
                is Long -> (rhsOperand as Operand<Long>).computeValue(valueProvider).toDouble()
                is Double -> (rhsOperand as Operand<Double>).computeValue(valueProvider)
                else -> {
                    assert(false) { "Impossible" }
                    0.0
                }
            }
            if (rhsValue == 0.0) {
                throw ArithmeticException("Division by zero in operation '$this' resulting in '$it/$rhsValue")
            }
            it.toDouble() / rhsValue
        }
    }

    companion object {
        operator fun <T : Number> Operand<T>.div(other: Operand<T>) = Divide(this, other)
        operator fun Operand<Long>.div(other: Long) = Divide(this, Constant.of(other))
        operator fun Long.div(other: Operand<Long>) = Divide(Constant.of(this), other)
    }
}