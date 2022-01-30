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

@file:Suppress("PropertyName", "MemberVisibilityCanBePrivate")

package org.github.mkep_dev.p_logi_k.model.io

import org.github.mkep_dev.p_logi_k.Identifier
import org.github.mkep_dev.p_logi_k.io.GenericValue


/**
 * A simple io module with 6 simulated boolean inputs and 6 simulated boolean outputs.
 *
 * @property id the identifier
 */
class BasicIOCard(private val id: Int) : IOModule {

    val IN_1: Identifier = "bIO$id/in/1"
    val IN_2: Identifier = "bIO$id/in/2"
    val IN_3: Identifier = "bIO$id/in/3"
    val IN_4: Identifier = "bIO$id/in/4"
    val IN_5: Identifier = "bIO$id/in/5"
    val IN_6: Identifier = "bIO$id/in/6"
    val inputIDs = listOf(IN_1, IN_2, IN_3, IN_4, IN_5, IN_6)

    val OUT_1: Identifier = "bIO$id/out/1"
    val OUT_2: Identifier = "bIO$id/out/2"
    val OUT_3: Identifier = "bIO$id/out/3"
    val OUT_4: Identifier = "bIO$id/out/4"
    val OUT_5: Identifier = "bIO$id/out/5"
    val OUT_6: Identifier = "bIO$id/out/6"
    val outputIDs = listOf(OUT_1, OUT_2, OUT_3, OUT_4, OUT_5, OUT_6)

    private val ioRepository: IORepository

    override val identifier: Identifier
        get() = "$id"

    init {
        val list = mutableListOf<IOElement<Boolean>>()
        list.addAll(inputIDs.map { IOElement(it, DataDirection.IN, GenericValue.of(false)) })
        list.addAll(outputIDs.map { IOElement(it, DataDirection.OUT, GenericValue.of(false)) })
        ioRepository = IORepository(list as List<IOElement<Any>>)
    }

    override fun addOnValueChangeListener(onChange: (id: Identifier, old: GenericValue<*>, new: GenericValue<*>, direction: DataDirection) -> Unit): IOModule.OnIOChangeSubscription {

        val sub = ioRepository.addOnValueChangeListener(onChange = onChange)

        return object : IOModule.OnIOChangeSubscription {
            override fun stop() = sub.stop()
        }

    }

    override fun writeOutputValue(id: Identifier, newValue: GenericValue<*>) {
        ioRepository.setIO(id, newValue)
    }

    override val providedIOs: Collection<IOElement<Any>>
        get() = ioRepository.getAll()
}