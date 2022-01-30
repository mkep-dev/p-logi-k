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

package org.github.mkep_dev.p_logi_k.program.fsm

import org.github.mkep_dev.p_logi_k.program.fsm.formula.*

data class VariableAssignment<T : Any>(val ref: Variable<T>, val value: Operand<T>) {
    override fun toString(): String {
        return "${ref.name}=$value"
    }

    companion object {

        infix fun <T : Any> Variable<T>.becomes(value: Operand<@UnsafeVariance T>): VariableAssignment<T> =
            VariableAssignment(this, value)

        infix fun BooleanVariable.becomes(boolean: Boolean) =
            VariableAssignment(this, Constant.of(boolean))

        infix fun LongVariable.becomes(int: Long) =
            VariableAssignment(this, Constant.of(int))

        fun TimerVariable.reset() =
            VariableAssignment(this, Constant.of(0))
    }
}