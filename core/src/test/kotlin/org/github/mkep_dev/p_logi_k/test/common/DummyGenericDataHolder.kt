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

package org.github.mkep_dev.p_logi_k.test.common

import org.github.mkep_dev.p_logi_k.Identifier
import org.github.mkep_dev.p_logi_k.io.GenericDataHolder
import org.github.mkep_dev.p_logi_k.io.GenericValue
import kotlin.reflect.KClass

class DummyGenericDataHolder<V:Any>(override val identifier: Identifier, val initialValue:GenericValue<V>) : GenericDataHolder<V> {
    override fun getValue(): GenericValue<V> = initialValue

    override fun equals(other: Any?): Boolean {
        return if (other is GenericDataHolder<*>){
            identifier == other.identifier &&
                    this.initialValue == other.getValue()
        }else{
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + initialValue.hashCode()
        return result
    }

    override val valueClass: KClass<V> = initialValue.valueClass
}