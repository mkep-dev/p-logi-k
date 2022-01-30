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

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KLogging
import org.github.mkep_dev.p_logi_k.model.io.IORepository
import org.github.mkep_dev.p_logi_k.program.api.CyclicPLCProgram

/**
 * The executor for cyclic PLC programs
 */
class BaseCyclicProgramExecutor(private val tickChannel: Flow<Long>, ioRepository: IORepository, val stepExceptionReceiver:((ex:Exception) -> Unit)? = null) {

    private companion object : KLogging()

    /**
     * Buffer layer to access the IOs in a cyclic view.
     */
    private val ioAccessLayer = BufferedIOAccessLayer(ioRepository)

    /**
     * The currently active program
     *
     * Null means no program is active
     */
    private var _program: CyclicPLCProgram? = null

    val program get() = _program

    /**
     * The current cycle count
     */
    private var cycleCount: Int = 0

    /**
     * Mutex to ensure concurrency while changing
     */
    private val mutex = Mutex()

    /**
     * Whether the executor should be active and execute the program every step
     */
    var active = false

    // Use Unconfined Dispatcher to ensure collect is called in "background"
    @OptIn(ObsoleteCoroutinesApi::class)
    private val executionScope =  CoroutineScope( Dispatchers.Unconfined)

    init {
        tickChannel.conflate().onEach {
                if (active) {
                    // TODO ensure time limit and detect slow/blocking program
                    step(it)
                    cycleCount++
                }
            }.launchIn(executionScope)
    }

    /**
     * The Step function that is periodically called to execute the program and apply the resulting output changes
     */
    private suspend fun step(millis:Long) = mutex.withLock {
        _program?.let {
            // retrieve the values
            ioAccessLayer.applyIOChangesFromRepository()

            try {
                // run program
                it.step(millis)
            }catch (e:Exception){
                active = false
                stepExceptionReceiver?.invoke(e)
            }

            ioAccessLayer.applyOutputChanges()
        }
    }

    /**
     * Change the program to the given one
     * the current execution will be stopped
     *
     * The execution has to be started again by setting the [active] variable
     *
     * @param newProgram the new program
     */
    fun setProgram(newProgram: CyclicPLCProgram?): Unit = runBlocking {
        logger.debug { "Set program '${newProgram?.name}'" }
        mutex.withLock {
            // deactivate execution
            active = false
            // setProgram
            _program = newProgram
            // prepare IOs
            _program?.let {
                logger.debug { "Init IOs." }
                ioAccessLayer.initIOS(it.getUsedInputs(), it.getOutputs())
                // Run init
                logger.debug { "Call program initialization step." }
                it.initialize(ioAccessLayer)
            }
        }
    }

    /**
     * Reset the execution
     */
    fun reset() {
        setProgram(_program)
    }


}