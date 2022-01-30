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

package org.github.mkep_dev.p_logi_k.io

import kotlin.reflect.KClass

/**
 * Exception when the data holder type and the value type differ
 *
 * @property identifier the identifier of the data holder
 * @property expectedValueClass the expected type
 * @property wrongType the given (wrong) type
 */
class TypeMismatchException(val identifier: String, val expectedValueClass: KClass<*>, val wrongType: KClass<*>) :
    Exception(
        "Attempt to overwrite the value of `${identifier}` of type `${expectedValueClass}`" +
                " with a new value of type `${wrongType.simpleName}`"
    ) {

    constructor(dataHolder: GenericDataHolder<*>, wrongType: KClass<*>) : this(
        dataHolder.identifier,
        dataHolder.valueClass,
        wrongType
    )
}