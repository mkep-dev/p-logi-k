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
 * *And* Operator with multiple arguments.
 * Representation of an operand that is itself a combination of two operands and an operator.
 *
 * This equals to "element_0 **and** element_1 **and** ... **and** element_n".
 *
 *
 * @param operands the operands that are part of this operator. The list must contain at least one operand.
 */
class And(operands: List<Operand<Boolean>>) : MultiArgumentOperator<Boolean, Boolean>(operands, Boolean::class) {

    constructor(vararg operands: Operand<Boolean>) : this(operands.toList())

    override val operationString: String = "and"

    override fun computeValue(valueProvider: (NamedDirectValueProvider<out Any>) -> Any): Boolean {
        return operands.all { it.computeValue(valueProvider) }
    }

    companion object {
        infix fun Operand<Boolean>.and(other: Operand<Boolean>) = And(this, other)
    }


}