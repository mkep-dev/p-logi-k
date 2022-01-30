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

package org.github.mkep_dev.p_logi_k.simulation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.github.mkep_dev.p_logi_k.services.api.IOService
import org.github.mkep_dev.p_logi_k.services.api.PLCExecutionService
import org.github.mkep_dev.p_logi_k.services.api.SimulationClockService
import org.github.mkep_dev.p_logi_k.time.SimulationClock

/**
 * Base class to create a simulation environment that can represent and mimic the behaviour of the real world.
 *
 * The environment deactivates the automatic ticking of the PLC clock and uses the clock of the environment to ensure correct timed behaviour.
 * If you activate the automatic ticking again this will result in many errors.
 *
 * The envirnoment will reset the plc when started and set the correct inputs as specified.
 *
 * The lifecycle of a simulation enviroment is always:
 *
 * 1. [prepare()][prepare]: prepare everything and initialize variable and set the start input values
 * 2. [run()][run]: this is the main part the simulation will run as long the run() method is active
 * 3. [destroy()][destroy]: after the run is completed [destroy] is used for cleanup
 *
 * @property accelerationFactor the acceleration factor for the environment (1.0 is as close as possible to real time)
 * @property ioService the io service
 * @property simulationClockService the simulation clock service
 * @property plcExecutionService the plc execution control service
 */
// TODO doc rules
abstract class SimulationEnvironment(
    protected val accelerationFactor: Double,
    protected val ioService: IOService,
    private val simulationClockService: SimulationClockService,
    private val plcExecutionService: PLCExecutionService
) {

    private val environmentClock = SimulationClock(10)

    // Use Unconfined Dispatcher to ensure collect is called in "background"
    @OptIn(ObsoleteCoroutinesApi::class)
    private val executionScope = CoroutineScope(Dispatchers.Unconfined)

    protected val currentMillis: Long
        get() = environmentClock.millis

    init {
        require(accelerationFactor > 0) { "The acceleration factor must be greater than 0." }
        environmentClock.setAutoTicking(false)
        environmentClock.setSimAcceleration(accelerationFactor)
        environmentClock.tickFlow.filter { it % simulationClockService.tickDelta == 0L }.onEach {
            simulationClockService.doTick()
        }.launchIn(executionScope)

    }

    protected suspend fun sleep(millis: Long) {
        val start = environmentClock.millis
        environmentClock.tickFlow.takeWhile { (it - start) < millis }.collect()
    }

    fun start(programName: String) = runBlocking(Dispatchers.Default) {
        environmentClock.reset()
        simulationClockService.setAutoTicking(false)
        environmentClock.setAutoTicking(true)
        val programLoaded = plcExecutionService.loadProgram(programName)
        if (!programLoaded) {
            throw NoSuchElementException("The given program '$programName' can't be loaded.")
        }
        plcExecutionService.resetAndPause()

        prepare()

        plcExecutionService.goOn()

        run()

        destroy()
    }

    /**
     * Prepare everything and initialize variable and set the start input values
     *
     */
    protected abstract suspend fun prepare()

    /**
     *  this is the main part the simulation will run as long the run() method is active
     *
     */
    protected abstract suspend fun run()

    /**
     * after the run is completed, this method is used for cleanup
     *
     */
    protected open fun destroy() {}

}