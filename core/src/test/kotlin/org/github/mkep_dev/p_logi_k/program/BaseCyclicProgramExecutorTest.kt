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

import org.github.mkep_dev.p_logi_k.io.GenericValue
import org.github.mkep_dev.p_logi_k.model.io.DataDirection
import org.github.mkep_dev.p_logi_k.model.io.IOElement
import org.github.mkep_dev.p_logi_k.model.io.IORepository
import org.github.mkep_dev.p_logi_k.test.common.DummyCyclicPLCProgram
import org.github.mkep_dev.p_logi_k.time.SimulationClock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BaseCyclicProgramExecutorTest {

    private lateinit var executor: BaseCyclicProgramExecutor

    private val clock = SimulationClock(50)

    private val dummyOutput = IOElement("d01", DataDirection.OUT, GenericValue.of(42))

    private lateinit var repository: IORepository


    @BeforeEach
    fun initTests() {
        repository = IORepository(dummyOutput)
        clock.reset()
        executor = BaseCyclicProgramExecutor(clock.tickFlow, repository)
    }


    @Test
    fun active() {
        var stepCalled = false
        val dummyProgram = object : DummyCyclicPLCProgram() {
            override fun step(millis: Long) {
                stepCalled = true
            }
        }

        executor.setProgram(dummyProgram)

        clock.doTick()

        Assertions.assertFalse(stepCalled, "Program step was executed but executor was inactive")

        executor.active = true

        // One tick and the program must be executed
        clock.doTick()

        Assertions.assertTrue(stepCalled, "Program step was not executed")

        // From now on the executor should be inactive again and the step should not be called
        executor.active = false
        stepCalled = false

        clock.doTick()

        Assertions.assertFalse(stepCalled, "Program step was executed but executor was set inactive again")

    }

    @Test
    fun setProgram() {

        val newDummyValue = GenericValue.of(25)
        val dummyProgram = object : DummyCyclicPLCProgram() {
            override fun step(millis: Long) {
                ioAccessLayer.getOutput(dummyOutput.identifier, dummyOutput.valueClass)!!.setValue(newDummyValue)
            }

            override fun getOutputs(): Set<IOElement<Any>> = setOf(dummyOutput)
        }

        Assertions.assertNull(executor.program)

        // executor was active before setProgram but must be inactive after set
        executor.active = true

        executor.setProgram(dummyProgram)


        Assertions.assertEquals(dummyProgram, executor.program)
        Assertions.assertFalse(executor.active, "Executor must be inactive after setting a new program")

        // TODO check IO resetting and writing etc
        // check io setting
        executor.active = true
        // before tick, it must have the initial value
        Assertions.assertEquals(
            dummyOutput.value,
            repository.getOutput(dummyOutput.identifier)!!.value,
            "Wrong initial value"
        )

        clock.doTick()

        // after the tick it must have the new vale true
        Assertions.assertEquals(
            newDummyValue,
            repository.getOutput(dummyOutput.identifier)!!.value,
            "IO was not changed by the program"
        )

        // When changing the program the new default value must be set
        val dummyValue2 = GenericValue.of(3)
        val dummyProgram2 = object : DummyCyclicPLCProgram() {
            override fun getOutputs(): Set<IOElement<Any>> = setOf(dummyOutput.copy(value = dummyValue2))
        }

        executor.setProgram(dummyProgram2)
        // before tick, it must have the new initial value
        Assertions.assertEquals(
            dummyValue2,
            repository.getOutput(dummyOutput.identifier)!!.value,
            "Wrong initial value"
        )
    }

    @Test
    fun reset() {
        //TODO
    }
}