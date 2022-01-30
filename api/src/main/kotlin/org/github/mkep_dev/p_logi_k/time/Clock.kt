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

import kotlinx.coroutines.flow.StateFlow

/**
 * The clock inside the PLC
 */
interface Clock {

    /**
     * Returns the channel that fires the ticks with the new system time in millis
     */
    val tickFlow: StateFlow<Long>


    /**
     * Current time in millis
     * @return the current simulation time in milliseconds
     */
    val millis: Long

    /**
     * Schedule the given steps for execution. One step per tick.
     *
     * @param steps the given steps
     * @throws IllegalStateException is thrown when the clock is reset while there are still steps to be executed.
     */
    @Throws(IllegalStateException::class)
    suspend fun scheduleSteps(steps: List<(() -> Unit)>)

    /**
     * Schedule the given steps for execution. One step per tick.
     *
     * @param step the given step
     * @throws IllegalStateException is thrown when the clock is reset while there are still steps to be executed.
     */
    @Throws(IllegalStateException::class)
    suspend fun scheduleSteps(vararg step: (() -> Unit)) {
        this.scheduleSteps(step.asList())
    }

}