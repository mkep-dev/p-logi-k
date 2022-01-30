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



package org.github.mkep_dev.p_logi_k.starter

import mu.KLogging
import org.github.mkep_dev.p_logi_k.model.SimulatedPLC
import org.github.mkep_dev.p_logi_k.model.io.IOModule
import org.github.mkep_dev.p_logi_k.program.InMemoryPLCProgramMemory
import org.github.mkep_dev.p_logi_k.program.api.PLCProgramMemory
import org.github.mkep_dev.p_logi_k.services.IOServiceImpl
import org.github.mkep_dev.p_logi_k.services.PLCExecutionFacade
import org.github.mkep_dev.p_logi_k.services.api.IOService
import org.github.mkep_dev.p_logi_k.services.api.PLCExecutionService
import org.github.mkep_dev.p_logi_k.services.api.ServicesInterfaceProvider
import org.github.mkep_dev.p_logi_k.services.api.SimulationClockService

/**
 * "The master of the simulation".
 * This class handles the services and create all object that are necessary to run the simulation.
 * It encapsulates the services that can be used to control the simulation and access the IOs.
 *
 * @constructor
 * The constructor
 *
 * @param ioModules the list of available IOModules. It is impossible to add modules later.
 * @param cycleTime the cycle time of the PLC. The time period a new execution cycle is started.
 */
class PLCMaster(ioModules: List<IOModule>, cycleTime: Int) {

    constructor(cycleTime: Int, vararg ioModule: IOModule) : this(ioModule.toList(), cycleTime)

    private companion object : KLogging()

    private val plc = SimulatedPLC(ioModules, cycleTime)

    /**
     * The IO service
     */
    val ioService: IOService = IOServiceImpl(plc.ioRepository)

    /**
     * The simulation clock service
     */
    val simulationClockService: SimulationClockService = plc.simulationClock

    /**
     * The program memory
     */
    val plcProgramMemory: PLCProgramMemory = InMemoryPLCProgramMemory(plc.ioRepository.getAll().toList())

    /**
     * The plc execution control service
     */
    val plcExecutionService: PLCExecutionService = PLCExecutionFacade(plc, plcProgramMemory)

    /**
     * List of existing interface providers
     */
    private val interfaceProviders = mutableListOf<ServicesInterfaceProvider>()


    /**
     * start new set of interface providers they extend the functionality of the existing services to e.g. make the services accessible vio RPCs.
     *
     * @param interfaceProviders the list of new interface providers
     */
    fun startExtraInterfaces(interfaceProviders: List<ServicesInterfaceProvider>) {

        this.interfaceProviders.addAll(interfaceProviders)

        interfaceProviders.distinct().forEach { it.start(ioService, plcExecutionService, simulationClockService) }

    }

    /**
     * Shutdown the simulation and all interface providers
     *
     */
    fun stop() {
        interfaceProviders.forEach { it.stop() }
    }

}