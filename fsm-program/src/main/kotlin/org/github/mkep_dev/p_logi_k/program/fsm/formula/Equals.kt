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

import kotlin.math.absoluteValue

/**
 * The representation of a *equals by value* compare. It takes two operands and returns a boolean.
 *
 * This equals "operand1 == operand2".
 *
 * @param lhsOperand the first operand
 * @param rhsOperand the second operand
 * @param delta
 */
class Equals<T : Any>(lhsOperand: Operand<T>, rhsOperand: Operand<T>, val delta: T? = null) :
    ComparingOperator<T>(lhsOperand, rhsOperand) {

    override val operationString: String = "=="

    override fun computeValue(valueProvider: (NamedDirectValueProvider<out Any>) -> Any): Boolean {
        return if (inputType == Double::class) {
            if (delta == null) {
                // without delta it is impossible to comapre to doubles
                return false
            }
            @Suppress("UNCHECKED_CAST") // checked by if
            ((lhsOperand as Operand<Double>).computeValue(valueProvider) - (rhsOperand as Operand<Double>).computeValue(
                valueProvider
            )).absoluteValue <= (delta as Double)
        } else {
            lhsOperand.computeValue(valueProvider) == rhsOperand.computeValue(valueProvider)
        }
    }

    companion object {
        infix fun <T : Any> Operand<T>.eq(other: Operand<T>) = Equals(this, other)
        infix fun Operand<Long>.eq(other: Long) = Equals(this, Constant.of(other))
    }
}