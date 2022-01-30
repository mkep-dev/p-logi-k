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

package org.github.mkep_dev.p_logi_k.services

import org.github.mkep_dev.p_logi_k.Identifier
import org.github.mkep_dev.p_logi_k.io.GenericValue
import org.github.mkep_dev.p_logi_k.io.TypeMismatchException
import org.github.mkep_dev.p_logi_k.model.io.DataDirection
import org.github.mkep_dev.p_logi_k.model.io.IOElement
import org.github.mkep_dev.p_logi_k.model.io.IOReference
import org.github.mkep_dev.p_logi_k.model.io.IORepository
import org.github.mkep_dev.p_logi_k.services.api.IOService

/**
 * Implementation for an IO service that uses the underlying  [IORepository] to access the IOs.
 *
 * @property ioRepository the underlying repository
 */
class IOServiceImpl(private val ioRepository: IORepository) : IOService {

    override fun getIOMap(): List<IOReference<Any>> {
       return ioRepository.getAll().map { it.reference }
    }

    override fun getIOs(regex: Regex): Collection<IOElement<Any>> {
        return ioRepository.filter { regex.matches(it.key) }.values
    }

    override fun getIOValue(name: String): GenericValue<Any>? {
        return ioRepository.getIO(name)?.value
    }

    override fun setInput(name: String, value: GenericValue<Any>) {
        if (ioRepository.getInput(name) == null) {
            throw NoSuchElementException("An input with name '$name' is unknown.")
        }
        if (!ioRepository.setIO(name, value)) {
            throw TypeMismatchException(name, ioRepository[name]!!.value.valueClass, value.valueClass)
        }
    }

    override fun addOnInputChangeListener(
        filter: Regex,
        listener: IOService.ValueChangeListener
    ): IOService.ValueChangeSubscriptionHandle? {

        // if there is no matching input return null
        if (ioRepository.filter { it.value.direction == DataDirection.IN }.keys.none { filter.matches(it) }) {
            return null
        }

        val filterFun: ((id: Identifier, direction: DataDirection) -> Boolean) =
            { id: Identifier, direction: DataDirection ->
                direction == DataDirection.IN && filter.matches(id)
            }

        return object : IOService.ValueChangeSubscriptionHandle {
            val subscription = ioRepository.addOnValueChangeListener(filter = filterFun, onlyDistinct = true) { id, old, new, _ ->
                listener.onChange(id, old, new)
            }

            override fun stop() {
                this.subscription.stop()
            }

        }
    }

    override fun addOnOutputChangeListener(
        filter: Regex,
        listener: IOService.ValueChangeListener
    ): IOService.ValueChangeSubscriptionHandle? {

        // if there is no matching output return null
        if (ioRepository.filter { it.value.direction == DataDirection.OUT }.keys.none { filter.matches(it) }) {
            return null
        }

        val filterFun: ((id: Identifier, direction: DataDirection) -> Boolean) =
            { id: Identifier, direction: DataDirection ->
                direction == DataDirection.OUT && filter.matches(id)
            }

        return object : IOService.ValueChangeSubscriptionHandle {
            val subscription = ioRepository.addOnValueChangeListener(filter = filterFun, onlyDistinct = true) { id, old, new, _ ->
                listener.onChange(id, old, new)
            }

            override fun stop() {
                this.subscription.stop()
            }

        }
    }
}