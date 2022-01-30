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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KLogging
import org.github.mkep_dev.p_logi_k.model.PLCExecutionState.*
import org.github.mkep_dev.p_logi_k.model.io.DataDirection
import org.github.mkep_dev.p_logi_k.model.io.IOElement
import org.github.mkep_dev.p_logi_k.model.io.IOModule
import org.github.mkep_dev.p_logi_k.model.io.IORepository
import org.github.mkep_dev.p_logi_k.program.BaseCyclicProgramExecutor
import org.github.mkep_dev.p_logi_k.program.api.CyclicPLCProgram
import org.github.mkep_dev.p_logi_k.time.Clock
import org.github.mkep_dev.p_logi_k.time.SimulationClock

class SimulatedPLC @Throws(IllegalArgumentException::class) constructor(
    ioModules: List<IOModule>,
    tickDelta: Int = 50
) : PLC {

    /**
     * State flow to keep trake of the PLC state
     */
    private var _stateFlow: MutableStateFlow<PLCExecutionState> = MutableStateFlow(INIT)

    // Backing field for state flow
    private var _state: PLCExecutionState
        get() = _stateFlow.value
        set(value) {
            _stateFlow.value = value
        }

    /**
     * Mutex to protect the state against concurrent changes
     */
    private val stateMutex: Mutex = Mutex()

    /**
     * The simulation clock
     */
    val simulationClock: SimulationClock

    /**
     * The used io repository
     */
    val ioRepository: IORepository

    /**
     * The plc program executor
     */
    private val executor: BaseCyclicProgramExecutor

    /**
     * Subscriptions for repository changes -> keep them for cleanup
     */
    private val repoSubs: MutableList<IORepository.OnChangeSubscription> = mutableListOf()

    /**
     * Subscriptions for io module changes -> keep them for cleanup
     */
    private val moduleSubs: MutableList<IOModule.OnIOChangeSubscription> = mutableListOf()


    init {
        this.simulationClock = SimulationClock(tickDelta)
        val modules = ioModules.distinctBy { it.identifier }.toSet()
        val allIOs = modules.flatMap { it.providedIOs }
        ioRepository = IORepository(allIOs)
        require(allIOs.groupingBy { it.identifier }.eachCount().all { it.value == 1 }) {
            // If there is a duplicate io find it and print it
            allIOs.groupingBy { it.identifier }.eachCount().filter { it.value != 1 }.keys.map { id ->
                // find modules
                val clashingIOModules =
                    modules.filter { module -> module.providedIOs.any { el -> el.identifier == id } }
                        .joinToString(",") { it.identifier }

                "The modules '$clashingIOModules' defined the same IO '$id'"
            }
        }
        modules.forEach { module ->
            // connect both IO providers with subscriptions
            moduleSubs += module.addOnValueChangeListener { id, _, new, direction ->
                if (direction.isInput()) {
                    // FIXme loop on change
                    ioRepository.setIO(id, new)
                }
            }
            repoSubs += ioRepository.addOnValueChangeListener(filter = { _, direction -> direction.isOutput() }) { id, _, new, _ ->
                // FIXme loop on change
                if (module.providedIOs.map { it.identifier }.contains(id)) {
                    module.writeOutputValue(id, new)
                }
            }
        }
        executor = BaseCyclicProgramExecutor(clock.tickFlow, ioRepository, this::onStepException)
        // Define executor behaviour with respect to the current PLC state and register logger for PLC state:
        _stateFlow.onEach {
            executor.active = when (it) {
                RUNNING -> true
                INIT, RESETTING, PAUSED, ERROR -> false
            }
            logger.debug { "PLC state changed to: '$it'." }
        }.launchIn(CoroutineScope(Dispatchers.Unconfined))
        _state = PAUSED
    }


    override val clock: Clock
        get() = simulationClock
    override val state: PLCExecutionState
        get() = _state
    override val stateFlow: StateFlow<PLCExecutionState>
        get() = _stateFlow.asStateFlow()

    private fun onStepException(exception: Exception) {
        logger.error { "While executing program: '${getCurrentProgram()}' an exception was fired. Go to error state. See: \n ${exception.stackTraceToString()}" }
        _state = ERROR
    }

    override suspend fun reset() = stateMutex.withLock {
        logger.info { "Resetting PLC..." }
        simulationClock.reset()
        clock.scheduleSteps(
            listOf(
                // Step 1
                {
                    _state = RESETTING
                    executor.reset()
                },
                // Step  2
                { _state = INIT },
                // Step  3
                { _state = RUNNING })
        )

    }

    override suspend fun pause() = stateMutex.withLock {
        logger.info { "Pausing PLC..." }
        _state = PAUSED
    }

    override suspend fun resetAndPause() = stateMutex.withLock {
        logger.info { "Resetting and Pausing PLC..." }
        simulationClock.reset()
        clock.scheduleSteps(
            listOf(
                // Step 1
                {
                    _state = RESETTING
                    executor.reset()
                },
                // Step  2
                { _state = INIT })
        )
    }

    override suspend fun goOn() = stateMutex.withLock {
        logger.info { "Going on with execution" }
        _state = RUNNING
    }

    override suspend fun loadProgram(plcProgram: CyclicPLCProgram): Boolean = stateMutex.withLock {
        logger.info { "Loading new program '${plcProgram.name}' to PLC..." }

        val steps = mutableListOf<() -> Unit>()

        // Check only available IOs are used
        val onlyExistingIOsUsed =
            plcProgram.getOutputs().all { output ->
                ioRepository.values.filter { it.direction.isOutput() }
                    .any { repoIO -> repoIO.identifier == output.identifier && repoIO.value.valueClass == output.value.valueClass }
            }
                    &&
                    plcProgram.getUsedInputs().all { input ->
                        ioRepository.values.filter { it.direction.isInput() }
                            .any { repoIO -> repoIO.identifier == input.identifier && repoIO.value.valueClass == input.value.valueClass }
                    }
        val oldState = _state
        steps.add {
            logger.debug { "Loading new program: Pause PLC." }
            _state = PAUSED
        }


        if (onlyExistingIOsUsed) {
            steps.add {
                logger.debug { "Loading new program: IO Module succeeded -> Hand program to executor" }
                // Be synchronous with clock
                _state = INIT
                try {
                    executor.setProgram(plcProgram)
                } catch (ex: Exception) {
                    logger.error { "Setting the program failed while initializing the program because of $ex ." }
                    throw ex
                }
            }
            steps.add {
                logger.debug { "Loading new program: Set state back to old value '$oldState'" }
                _state = oldState
            }

        } else {
            // Find missing ios
            val missingIOs =
                // Outputs
                plcProgram.getOutputs().filter { output ->
                    ioRepository.values.filter { it.direction.isOutput() }
                        .any { repoIO -> repoIO.identifier == output.identifier && repoIO.value.valueClass == output.valueClass }
                }.map { IOElement(it.identifier, DataDirection.OUT, it.value) } +
                        // inputs
                        plcProgram.getUsedInputs().filterNot { input ->
                            ioRepository.values.filter { it.direction.isInput() }
                                .any { repoIO -> repoIO.identifier == input.identifier && repoIO.value.value == input.valueClass }
                        }.map { IOElement(it.identifier, DataDirection.IN, it.value) }
            logger.warn {
                "The PLC program ${plcProgram.name} could not be loaded because the following IOs are missing:" +
                        "'$missingIOs'."
            }
        }
        try {
            clock.scheduleSteps(steps)
        } catch (ex: Exception) {
            logger.error { "Exception while setting new program: See ${ex.stackTraceToString()}" }
            _state = ERROR
            return false
        }
        return onlyExistingIOsUsed
    }

    override fun getCurrentProgram(): CyclicPLCProgram? = executor.program


    companion object : KLogging()

}