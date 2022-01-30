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

import com.google.protobuf.*
import org.github.mkep_dev.p_logi_k.services.api.SimulationClockService
import org.github.mkep_dev.p_logi_k.services.grpc.stub.SimulationClockServiceGrpcKt

class GrpcSimulationClockService(private val simulationClockService: SimulationClockService) :
    SimulationClockServiceGrpcKt.SimulationClockServiceCoroutineImplBase() {

    override suspend fun getTickDelta(request: Empty): Int32Value = int32Value {
        value = simulationClockService.tickDelta
    }

    override suspend fun getSimAcceleration(request: Empty): DoubleValue = doubleValue{
        value = simulationClockService.simAccelerationFactor
    }

    override suspend fun setSimAcceleration(request: DoubleValue) = boolValue {
        value = kotlin.runCatching { simulationClockService.setSimAcceleration(request.value) }.isSuccess
    }

    override suspend fun setAutoTicking(request: BoolValue) = empty {
        simulationClockService.setAutoTicking(request.value)
    }

    override suspend fun doTick(request: Empty) = boolValue {
        value = kotlin.runCatching { simulationClockService.doTick() }.isSuccess
    }

    override suspend fun getMillis(request: Empty) = int64Value {
        value = simulationClockService.millis
    }
}