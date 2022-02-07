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

package org.github.mkep_dev.p_logi_k.program

import mu.KLogging
import org.github.mkep_dev.p_logi_k.model.io.IOElement
import org.github.mkep_dev.p_logi_k.program.api.CyclicPLCProgram
import org.github.mkep_dev.p_logi_k.program.api.PLCProgramMemory
import org.github.mkep_dev.p_logi_k.program.api.PLCProgramMeta

/**
 * Basic in memory (heap -> non-persistent) program memory
 *
 * @property availableIOS the available ios (used to check whether the given program could be executed)
 */
class InMemoryPLCProgramMemory(private val availableIOS: List<IOElement<Any>>) : PLCProgramMemory {

    private val programs = mutableMapOf<String, CyclicPLCProgram>()

    override fun addProgram(program: CyclicPLCProgram): Boolean {
        if (!program.getUsedInputs()
                .all { expected -> availableIOS.any { it.direction.isInput() && it.identifier == expected.identifier && it.value.valueClass == expected.valueClass } }
        ) {
            logger.error {
                "There are unknown inputs that are required for the program:\n" +
                program.getUsedInputs()
                    .filter { expected -> !availableIOS.any { it.direction.isInput() && it.identifier == expected.identifier && it.value.valueClass == expected.valueClass } }
                    .joinToString()
            }
            return false
        }
        if (!program.getOutputs()
                .all { expected -> availableIOS.any { it.direction.isOutput() && it.identifier == expected.identifier && it.value.valueClass == expected.valueClass } }
        ) {
            logger.error {
                "There are unknown outputs that are required for the program:\n" + program.getOutputs()
                    .filter { expected -> !availableIOS.any { it.direction.isOutput() && it.identifier == expected.identifier && it.value.valueClass == expected.valueClass } }
                    .joinToString()
            }
            return false
        }

        if (programs[program.name] != null) {
            return false
        }
        programs[program.name] = program
        return true
    }

    override fun getAvailablePrograms(): Collection<PLCProgramMeta> {
        return programs.values
    }

    override fun getProgram(name: String): CyclicPLCProgram? {
        return programs[name]
    }

    private companion object : KLogging()

}