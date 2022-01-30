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

package org.github.mkep_dev.p_logi_k.model

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.takeWhile
import mu.KLogging
import org.awaitility.kotlin.await
import org.github.mkep_dev.p_logi_k.Identifier
import org.github.mkep_dev.p_logi_k.io.GenericValue
import org.github.mkep_dev.p_logi_k.model.PLCExecutionState.*
import org.github.mkep_dev.p_logi_k.model.io.BasicIOCard
import org.github.mkep_dev.p_logi_k.model.io.DataDirection
import org.github.mkep_dev.p_logi_k.model.io.IOElement
import org.github.mkep_dev.p_logi_k.model.io.IOModule
import org.github.mkep_dev.p_logi_k.test.common.DummyCyclicPLCProgram
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.TimeUnit

internal class SimulatedPLCTest {

    companion object : KLogging()

    private lateinit var plc: SimulatedPLC

    private val module1 = BasicIOCard(1)

    @BeforeEach
    fun initTests() {
        plc = SimulatedPLC(listOf(module1))
        plc.simulationClock.setAutoTicking(true)
    }


    @Test
    fun duplicateIOsDetected(){

        val evilModule1 = DummyModule("1", IOElement("1",DataDirection.IN, GenericValue.of(1)))
        val evilModule2 = DummyModule("2", IOElement("1",DataDirection.IN, GenericValue.of(1)))

        Assertions.assertThrows(IllegalArgumentException::class.java){
            SimulatedPLC(listOf(evilModule1,evilModule2))
        }
    }

    @Test
    fun testInitState() {
        Assertions.assertEquals(PAUSED,plc.state)
    }

    @Test
    fun reset() = runBlocking{
        goOn()

        val expectedTransition = mutableListOf(RUNNING, RESETTING, INIT, RUNNING)

        launch(Dispatchers.Unconfined) {
            val flow = plc.stateFlow as Flow<PLCExecutionState>
            flow.collect {
                Assertions.assertEquals(expectedTransition.first(),it)
                expectedTransition.removeFirst()
            }
        }

        runBlocking { plc.reset() }

        Assertions.assertTrue(expectedTransition.isEmpty(),"Not all necessary states were visited." +
                "Remaining states $expectedTransition")

        coroutineContext.cancelChildren()


    }

    @Test
    fun pause() = runBlocking{
        var stepCalled = false
        // set a program
        plc.loadProgram(object : DummyCyclicPLCProgram() {
            override fun step(millis: Long) {
                stepCalled = true
            }

        })

        plc.goOn()
        suspend { await.pollDelay(10,TimeUnit.MILLISECONDS).alias("Step in program wasn't executed but plc should be running.")
            .atMost(Duration.ofMillis(plc.simulationClock.tickDelta*2L)).until { stepCalled } }
        plc.pause()
        stepCalled = false
        delay(plc.simulationClock.tickDelta*2L)
        Assertions.assertFalse(stepCalled,"Program was called although PLC was paused")
    }

    @Test
    fun resetAndPause() = runBlocking{
        var stepCalled = false
        // set a program
        plc.loadProgram(object : DummyCyclicPLCProgram() {
            override fun step(millis: Long) {
                stepCalled = true
            }

        })

        plc.goOn()
        suspend {
            await.pollDelay(10, TimeUnit.MILLISECONDS)
                .alias("Step in program wasn't executed but plc should be running.")
                .atMost(Duration.ofMillis(plc.simulationClock.tickDelta * 2L)).until { stepCalled }
        }
        plc.resetAndPause()
        stepCalled = false
        delay(plc.simulationClock.tickDelta*2L)
        Assertions.assertFalse(stepCalled,"Program was called although PLC was paused")
    }

    @Test
    fun goOn() = runBlocking {

        var executionAllowed = false
        var stepCalled = false

        // set a program and it should not be executed
        plc.loadProgram(object : DummyCyclicPLCProgram() {

            override fun step(millis: Long) {
                if (!executionAllowed) {
                    Assertions.fail<String>("Program executed before goOn() call")
                }
                stepCalled = true
            }

        })

        delay(500)
        Assertions.assertEquals(PAUSED,plc.state)

        executionAllowed = true
        plc.goOn()
        Assertions.assertEquals(RUNNING,plc.state)
        delay(500)
        Assertions.assertTrue(stepCalled,"Step in program wasn't executed but plc should be running.")
    }

    @Test
    fun loadProgram() = runBlocking {
        var stepCalled = false
        plc.simulationClock.setAutoTicking(false)
        plc.goOn()
        Assertions.assertEquals(RUNNING, plc.state,"PLC must be running after goOn().")

        // Check PLC state updates
        val expectedTransition = mutableListOf(RUNNING,PAUSED, INIT, RUNNING)
        launch(Dispatchers.Unconfined) {
            val flow = plc.stateFlow as Flow<PLCExecutionState>
            flow.takeWhile { expectedTransition.isNotEmpty() }.collect {
                Assertions.assertEquals(expectedTransition.first(),it,"Order of state transitions is broken. " +
                        "Expected remaining states: $expectedTransition")
                expectedTransition.removeFirst()
            }
        }
        launch(Dispatchers.Unconfined) {
            // set a program and it should not be executed
            plc.loadProgram(object : DummyCyclicPLCProgram() {
                override fun step(millis: Long) {
                    logger.info { "program step called" }
                    stepCalled = true
                }

            })
        }
        // 3 Tick for loading a program -> spec
        repeat(2){
            plc.simulationClock.doTick()
        }

        Assertions.assertTrue(expectedTransition.isEmpty(),"Not all necessary states were visited." +
                "Remaining states $expectedTransition")

        Assertions.assertEquals(RUNNING,plc.state, "PLC must be running after the program was loaded.")

        Assertions.assertFalse(stepCalled, "PLC should not execute program after it was loaded without a new tick.")

        logger.info { "Now test if program is executed with next tick." }

        plc.simulationClock.doTick()

        Assertions.assertTrue(stepCalled, "PLC must execute the program after the program was loaded.")

        coroutineContext.cancelChildren()
    }

    @Test
    fun getCurrentProgramName() = runBlocking{
        val dummyProgram = DummyCyclicPLCProgram("DumDum")
        plc.loadProgram(dummyProgram)
        Assertions.assertEquals(dummyProgram, plc.getCurrentProgram())
    }

    private class DummyModule(val id: Identifier /* = kotlin.String */, val ios:List<IOElement<Any>>):IOModule{
        override val identifier: Identifier
            get() = id

        constructor(id:Identifier, vararg ios:IOElement<Any>):this(id,ios.toList())

        override fun addOnValueChangeListener(onChange: (id: Identifier, old: GenericValue<Any>, new: GenericValue<Any>, direction: DataDirection) -> Unit): IOModule.OnIOChangeSubscription {
            // DO nothing
            return object : IOModule.OnIOChangeSubscription{
                override fun stop() {
                    // do nothing
                }
            }
        }

        override fun writeOutputValue(id: Identifier, newValue: GenericValue<Any>) {
            // do nothing
        }

        override val providedIOs: Collection<IOElement<Any>>
            get() = ios
    }

}