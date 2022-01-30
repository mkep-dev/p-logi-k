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
import kotlinx.coroutines.flow.*
import org.github.mkep_dev.p_logi_k.services.api.SimulationClockService
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.Delegates

/**
 * Simulated PLC clock
 *
 * the clock thread is always active
 */
class SimulationClock(override val tickDelta: Int) : Clock, SimulationClockService {

    private var autoTicking = false

    private var simulationAcceleration by Delegates.vetoable(1.0) { _, _, newValue ->
        0 < newValue
    }

    private val simulationTime = MutableStateFlow(0L)

    @OptIn(ObsoleteCoroutinesApi::class)
    private val context = newSingleThreadContext("ClockContext")

    private val scope = CoroutineScope(context)

    private val job: Job

    init {
        job = scope.launch(context) {
            while (isActive) {
                if (autoTicking) {
                    launch {
                        incrementTime()
                    }
                }
                delay((tickDelta / simulationAcceleration).toLong())
            }
        }
    }

    override val simAccelerationFactor: Double
        get() = simulationAcceleration

    override fun setSimAcceleration(speedFactor: Double) {
        if (speedFactor <= 0) {
            throw IllegalArgumentException("Simulation acceleration is not allowed to be negative or zero. Given value was '$speedFactor'")
        }
        simulationAcceleration = speedFactor
    }

    override fun setAutoTicking(doAutoTick: Boolean) {
        autoTicking = doAutoTick
    }

    override fun doTick() {
        if (autoTicking) {
            throw SimulationClockService.ManualTickForbiddenException()
        }
        incrementTime()
    }

    override val millis: Long
        get() = simulationTime.value

    override val tickFlow: StateFlow<Long>
        get() = simulationTime.asStateFlow()

    fun reset() {
        simulationTime.update { 0 }
    }

    private fun incrementTime() {
        simulationTime.update { it + tickDelta }
    }

    @Throws(IllegalStateException::class, Exception::class)
    override suspend fun scheduleSteps(steps: List<(() -> Unit)>) {
        val stepCounter = AtomicInteger(0)
        try {
            tickFlow.takeWhile { stepCounter.get() < steps.size && !(it == 0L && stepCounter.get() > 0) }
                .collectLatest {
                    if (stepCounter.get() >= 0) {
                        steps[stepCounter.get()].invoke()
                    }
                    stepCounter.incrementAndGet()
                }
        } catch (ex: Exception) {
            // step invocation threw an error: rethrow and stop further steps
            throw ex
        }
        if (stepCounter.get() != steps.size) {
            throw IllegalStateException("Execution of steps was cancelled because a reset of the clock occurred")
        }

    }

}