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

package org.github.mkep_dev.p_logi_k.model.io

import org.github.mkep_dev.p_logi_k.Identifiable
import org.github.mkep_dev.p_logi_k.Identifier
import org.github.mkep_dev.p_logi_k.io.GenericValue
import kotlin.reflect.KClass

/**
 * Wrapper for an input / output
 *
 * @param V the data type
 * @property identifier the identifier
 * @property direction the direction
 * @property value the current/default value
 */
data class IOElement<out V : Any>(
    override val identifier: Identifier, val direction: DataDirection, val value: GenericValue<V>,
) : Identifiable {
    val valueClass: KClass<@UnsafeVariance V>
        get() = value.valueClass

    val reference : IOReference<V>
        get() = IOReference(identifier, direction, valueClass)
}