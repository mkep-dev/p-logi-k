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
 * A provider interface that extends the PLC by the functionality to offer the services over another interface.
 *
 */
interface ServicesInterfaceProvider {

    /**
     * Starts the service provider
     *
     * @param ioService the io service that can be interfaced
     * @param plcExecutionService the plc execution service that can be interfaced
     * @param simulationClockService the simulation clock service that can be interfaced
     * @return
     */
    fun start(ioService: IOService, plcExecutionService: PLCExecutionService, simulationClockService: SimulationClockService) : Boolean

    /**
     * Stops the service interface provider
     *
     */
    fun stop()
}