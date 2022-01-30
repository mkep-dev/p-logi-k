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

import org.github.mkep_dev.p_logi_k.model.io.IOElement
import org.github.mkep_dev.p_logi_k.program.api.CyclicPLCProgram
import org.github.mkep_dev.p_logi_k.program.api.IOAccessLayer

/**
 * Class with dummy implementations that can be overwritten
 * Used for tests
 */
open class DummyCyclicPLCProgram(override val name: String = "DummyProgram") : CyclicPLCProgram {
    lateinit var ioAccessLayer: IOAccessLayer

    override fun initialize(ioAccess: IOAccessLayer) {
        ioAccessLayer = ioAccess
    }

    override fun step(millis: Long) {

    }

    override fun getUsedInputs(): Set<IOElement<Any>> = emptySet()

    override fun getOutputs(): Set<IOElement<Any>> = emptySet()
}