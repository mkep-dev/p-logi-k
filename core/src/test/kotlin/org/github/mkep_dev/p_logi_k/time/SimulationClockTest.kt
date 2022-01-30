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

package org.github.mkep_dev.p_logi_k.time

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.github.mkep_dev.p_logi_k.services.api.SimulationClockService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.atomic.AtomicInteger

class SimulationClockTest {

    private lateinit var clock: SimulationClock

    private val tickDelta = 50

    @BeforeEach
    fun testInit() {
        clock = SimulationClock(tickDelta)
    }

    @Test
    fun manualTicking() = runBlocking {
        clock.setAutoTicking(false)

        val numberOfTicks = 50

        var tickCounter = 0
        val counterMutex = Mutex()

        launch(Dispatchers.Unconfined) {
            clock.tickFlow.filter { it != 0L }.collect {
                counterMutex.withLock {
                    // Increase tick counter
                    tickCounter++

                    // ensure time value is correct
                    assertEquals(
                        (tickCounter * tickDelta).toLong(),
                        it,
                        "Time message mismatches tick count"
                    )
                }
            }
        }


        for (i in 1..numberOfTicks) {
            assertDoesNotThrow {
                clock.doTick()
            }
            assertEquals((i * tickDelta).toLong(), clock.millis, "Time after singe tick incorrect")
        }

        assertEquals((numberOfTicks * tickDelta).toLong(), clock.millis, "Time mismatch after ticks")

        assertEquals(numberOfTicks, tickCounter, "Counter mismatch after ticks")

        coroutineContext.cancelChildren()
    }

    @Test
    fun autoTicking() = runBlocking {
        runWithTimeAcceleration(1.0)
    }

    @Test
    fun failManualWithActiveAutoTick() {
        clock.setAutoTicking(true)

        assertThrows<SimulationClockService.ManualTickForbiddenException> {
            clock.doTick()
        }
    }

    @Test
    fun timeUnchangedAfterClockAutoOff() = runBlocking {
        clock.setAutoTicking(true)

        // Let the clock tick
        delay((10 * tickDelta).toLong())

        // Stop the auto tick
        clock.setAutoTicking(false)

        val timeAfterOff = clock.millis

        // just wait to ensure clock won't change
        delay((2 * tickDelta).toLong())

        assertEquals(timeAfterOff, clock.millis, "Time has changed but auto ticking was off")

    }

    @Test
    fun testTimeAcceleration() {
        val timeAccelerations = arrayOf(0.5, 2.0, 4.0, 8.0)

        for (accel in timeAccelerations) {
            testInit()
            runWithTimeAcceleration(accel)
        }
    }

    private fun runWithTimeAcceleration(acceleration: Double = 1.0) = runBlocking {
        val numberOfTicks = 50

        clock.setSimAcceleration(acceleration)

        var tickCounter = 0

        launch(Dispatchers.Unconfined) {
            clock.tickFlow.collect {
                    // reset tick counter to prevent reset is counted as tick
                    if (it == 0L) {
                        tickCounter = 0
                        return@collect
                    }

                    // Increase tick counter
                    tickCounter++

                    assertEquals(
                        (tickCounter * tickDelta).toLong(),
                        it,
                        "Time message mismatches tick count"
                    )

            }
        }

        clock.setAutoTicking(true)

        // let the clock tick
        delay((numberOfTicks * tickDelta * 1 / acceleration).toLong())

        // Stop clock
        clock.setAutoTicking(false)

        // Wait 2 * acceleration ticks to ensure nothing changed
        delay((2 * tickDelta).toLong())


        assertEquals(
            numberOfTicks.toDouble(),
            (clock.millis / tickDelta).toDouble(),
            acceleration,
            "Time mismatch after ticks with accel $acceleration"
        )

        assertEquals(numberOfTicks.toDouble(), tickCounter.toDouble(), acceleration,  "Counter mismatch after ticks")

        coroutineContext.cancelChildren()
    }

    @Test
    fun resetClock() {

        val numberOfTicks = 50

        clock.setAutoTicking(false)

        // Tick x times
        for (i in 1..numberOfTicks) {
            clock.doTick()
        }
        // Check if clock ticked
        assertEquals((numberOfTicks * tickDelta).toLong(), clock.millis, "Time mismatch after ticks")

        // Reset clock
        clock.reset()
        assertEquals(0, clock.millis, "Clock reset failed")

    }

    @Test
    fun multipleFlowSubscribers() = runBlocking {
        clock.setAutoTicking(false)

        val numberOfTicks = 50

        val tickCounter = mutableMapOf<Int,Int>()
        val counterMutex = Mutex()

        // Create multiple thread with multiple subscribers
        val subCount = 5
        repeat(subCount) { subId ->
            tickCounter[subId] = 0
            launch(Dispatchers.Unconfined) {
                clock.tickFlow.filter { it != 0L }.collect {
                    counterMutex.withLock {
                        // Increase tick counter
                        tickCounter.computeIfPresent(subId){_,v -> v + 1}

                        assertEquals(
                            (tickCounter[subId]!! * tickDelta).toLong(),
                            it,
                            "Subscriber #$subId:Time message mismatches tick count"
                        )
                    }
                }
            }
        }


        for (i in 1..numberOfTicks) {
            clock.doTick()
            assertEquals((i * tickDelta).toLong(), clock.millis, "Time after singe tick incorrect")
        }

        assertEquals((numberOfTicks * tickDelta).toLong(), clock.millis, "Time mismatch after ticks")

        assertEquals(numberOfTicks * subCount, tickCounter.values.sum(),  "Counter mismatch after ticks")

        coroutineContext.cancelChildren()
    }

    @Test
    fun scheduleSteps() = runBlocking{
        clock.setAutoTicking(false)
        clock.reset()

        val int = AtomicInteger(0)
        val stepCount = 5

        val steps = mutableListOf<() -> Unit>()
        repeat(stepCount){
            steps.add{ int.incrementAndGet()}
        }
        val stepExecutionJob = launch(Dispatchers.Unconfined,start = CoroutineStart.LAZY) {
            clock.scheduleSteps(steps)
        }

        assertTrue(stepExecutionJob.start())


        // More ticks than necessary to ensure nothing is called to often
        repeat(stepCount+2){
            clock.doTick()
        }

        assertEquals(stepCount,int.get())

        assertTrue(stepExecutionJob.isCompleted, "Step execution job did not complete.")

        coroutineContext.cancelChildren()
    }
}