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


sealed class ComparingOperator<T : Any>(
    lhsOperand: Operand<T>,
    rhsOperand: Operand<T>,
) : BiArgumentOperator<T, Boolean>(lhsOperand, rhsOperand, Boolean::class) {

    protected fun <V : Any> V.compareTo(other: V, matcher: ((Int) -> Boolean)): Boolean {
        val compareResult = when (this) {
            is Comparable<*> -> {
                @Suppress("UNCHECKED_CAST")
                this as Comparable<V>
                this.compareTo(other)
            }
            else -> {
                assert(false) { "Impossible" }
                0
            }
        }
        return matcher(compareResult)
    }
}