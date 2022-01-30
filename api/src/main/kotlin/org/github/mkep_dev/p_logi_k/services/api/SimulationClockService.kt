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

package org.github.mkep_dev.p_logi_k.services.api

/**
 * Simulation time service interface
 */
interface SimulationClockService {

    /**
     * Return the millis increment by a single tick
     * @return delta between to ticks
     */
    val tickDelta:Int

    /**
     * Speed up factor for simulated time:
     * - x == 1.0: realtime
     * - 0.0 < x < 1.0: slower than reality
     * - 1.0 < x: faster than reality
     *
     * The real factor is limited by the computation speed;
     */
    val simAccelerationFactor:Double

    /**
     * Speed up factor for simulated time:
     * - x == 1.0: realtime
     * - 0.0 < x < 1.0: slower than reality
     * - 1.0 < x: faster than reality
     *
     * The real factor is limited by the computation speed;
     *
     * @throws IllegalArgumentException if value is smaller smaller or equal zero
     */
    @Throws(IllegalArgumentException::class)
    fun setSimAcceleration(speedFactor: Double)

    /**
     * Whether the clock should tick on its own (=automatically) or wait for external trigger
     * @param doAutoTick: whether to do an auto tick
     */
    fun setAutoTicking(doAutoTick: Boolean)

    /**
     * Manually trigger the tick event. Only allowed when doAutoTicking was set to false
     * @throws ManualTickForbiddenException
     */
    @Throws(ManualTickForbiddenException::class)
    fun doTick()

    /**
     * Current time in millis
     * @return the current simulation time in milliseconds
     */
    val millis:Long

    /**
     * Exception when somebody tries to fire a manual tick while automatic ticking is active.
     *
     */
    class ManualTickForbiddenException : Exception("Tried to fire a manual tick event but auto ticking is active")
}