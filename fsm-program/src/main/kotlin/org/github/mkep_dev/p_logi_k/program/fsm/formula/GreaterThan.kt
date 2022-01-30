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
 * The representation of a *greater than* compare. It takes two numbers and returns a boolean.
 *
 * This equals "operand1 > operand2".
 *
 * @param lhsOperand the first operand
 * @param rhsOperand the second operand
 */
class GreaterThan<T : Any>(lhsOperand: Operand<T>, rhsOperand: Operand<T>) :
    ComparingOperator<T>(lhsOperand, rhsOperand) {
    override val operationString: String = ">"

    override fun computeValue(valueProvider: (NamedDirectValueProvider<out Any>) -> Any): Boolean {
        return lhsOperand.computeValue(valueProvider).compareTo(rhsOperand.computeValue(valueProvider)) { it > 0 }
    }

    companion object {
        infix fun <T : Any> Operand<T>.gt(other: Operand<T>) = GreaterThan(this, other)
    }

}