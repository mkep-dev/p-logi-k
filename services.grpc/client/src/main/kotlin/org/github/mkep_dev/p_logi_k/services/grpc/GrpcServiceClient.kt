
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

import io.grpc.Channel
import org.github.mkep_dev.p_logi_k.services.api.IOService
import org.github.mkep_dev.p_logi_k.services.api.PLCExecutionService
import org.github.mkep_dev.p_logi_k.services.api.SimulationClockService
import org.github.mkep_dev.p_logi_k.services.grpc.stub.IOServiceGrpc
import org.github.mkep_dev.p_logi_k.services.grpc.stub.PLCExecutionServiceGrpcKt
import org.github.mkep_dev.p_logi_k.services.grpc.stub.SimulationClockServiceGrpcKt

/**
 * Client to use the services via grpc
 *
 * @constructor
 *
 * @param channel the grpc channel that will be used
 */
class GrpcServiceClient(channel: Channel) {

    /**
     * The IO service
     */
    val ioService: IOService = GrpcIOServiceClient(IOServiceGrpc.newBlockingStub(channel))

    /**
     * The simulation clock service
     */
    val simulationClockService: SimulationClockService =
        GrpcSimulationClockServiceClient(SimulationClockServiceGrpcKt.SimulationClockServiceCoroutineStub(channel))

    /**
     * The plc execution control service
     */
    val plcExecutionService: PLCExecutionService =
        GrpcPLCExecutionServiceClient(PLCExecutionServiceGrpcKt.PLCExecutionServiceCoroutineStub(channel))

}