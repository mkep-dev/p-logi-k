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

/**
 * PLC States
 *
 * Possible state transitions
 *
 * - INIT
 *     - RUNNING
 *     - PAUSED
 * - RUNNING
 *     - RESETTING
 *     - PAUSED
 *     - ERROR
 * - PAUSED
 *     - RESETTING
 *     - RUNNING
 * - RESETTING
 *     - INIT
 * - ERROR
 *     - RESETTING
 *
 */
enum class PLCExecutionState {

    /**
     * Start state before any program is executed. The PLC is preparing the execution: load program, init variables and IOs
     * This state also indicates the case that no program is currently loaded and the PLC does nothing
     */
    INIT,

    /**
     * A program is active and running
     */
    RUNNING,

    /**
     * The reset routine was fired. The PLC is handling this request and prepares the reset
     * Then go to init ->
     */
    RESETTING,

    /**
     * PLC is paused
     */
    PAUSED,

    /**
     * An error occurred while executing the program
     *
     */
    ERROR
}