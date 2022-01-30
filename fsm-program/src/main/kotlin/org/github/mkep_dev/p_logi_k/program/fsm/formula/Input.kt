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

import org.github.mkep_dev.p_logi_k.model.io.IoEdgePolarity
import kotlin.reflect.KClass


/**
 * Extension point for inputs
 * @param V the generic Type of the Input
 * @param type type of the Input
 */
sealed class Input<V : Any>(name: String, type: KClass<@UnsafeVariance V>) : NamedDirectValueProvider<V>(name, type) {
    override fun toString(): String = "input(${name}:${type.simpleName})"
}

class BooleanInput(name: String) : Input<Boolean>(name, Boolean::class) {

}

class BooleanEdgeInput(name: String, val polarity: IoEdgePolarity) : Input<Boolean>(name, Boolean::class) {

}

class DoubleInput(name: String) : Input<Double>(name, Double::class) {

}

class LongInput(name: String) : Input<Long>(name, Long::class) {

}