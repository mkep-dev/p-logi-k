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

package org.github.mkep_dev.p_logi_k.services.grpc

import io.grpc.Server
import io.grpc.ServerBuilder
import mu.KLogging
import org.github.mkep_dev.p_logi_k.services.api.IOService
import org.github.mkep_dev.p_logi_k.services.api.PLCExecutionService
import org.github.mkep_dev.p_logi_k.services.api.ServicesInterfaceProvider
import org.github.mkep_dev.p_logi_k.services.api.SimulationClockService

/**
 * Interface provider to offer the services via grpc
 * The underlying uses plain text and no authentication
 * **No protection against attackers**
 *
 * @property port the port that will be used for the grpc services.
 */
class GRPCServiceInterfaceProvider(private val port: Int) : ServicesInterfaceProvider {

    private var server: Server? = null

    override fun start(
        ioService: IOService,
        plcExecutionService: PLCExecutionService,
        simulationClockService: SimulationClockService
    ): Boolean {
        server = ServerBuilder
            .forPort(port)
            .addService(GrpcPLCExecutionService(plcExecutionService))
            .addService(GrpcIOService(ioService, logger))
            .addService(GrpcSimulationClockService(simulationClockService))
            .build()

        return kotlin.runCatching { server?.start() }.isSuccess
    }

    override fun stop() {
        server?.shutdown()
    }

    companion object : KLogging()

}