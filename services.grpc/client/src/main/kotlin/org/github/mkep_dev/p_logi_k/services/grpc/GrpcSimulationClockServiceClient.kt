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

import com.google.protobuf.BoolValue
import com.google.protobuf.DoubleValue
import com.google.protobuf.Empty
import io.grpc.StatusException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.github.mkep_dev.p_logi_k.services.api.SimulationClockService
import org.github.mkep_dev.p_logi_k.services.grpc.stub.SimulationClockServiceGrpcKt

internal class GrpcSimulationClockServiceClient(private val stub: SimulationClockServiceGrpcKt.SimulationClockServiceCoroutineStub) :
    SimulationClockService {

    override val tickDelta: Int
        get() = runBlocking(Dispatchers.IO) {
            try {
                stub.getTickDelta(Empty.getDefaultInstance()).value
            } catch (statusEx: StatusException) {
                -1
            }
        }


    override fun setSimAcceleration(speedFactor: Double) = runBlocking<Unit>(Dispatchers.IO) {
        stub.setSimAcceleration(DoubleValue.of(speedFactor))
    }

    override val simAccelerationFactor: Double
        get() = runBlocking {
            stub.getSimAcceleration(Empty.getDefaultInstance()).value
        }

    override fun setAutoTicking(doAutoTick: Boolean) = runBlocking<Unit>(Dispatchers.IO) {
        stub.setAutoTicking(BoolValue.of(doAutoTick))
    }

    override fun doTick() = runBlocking(Dispatchers.IO) {
        val tickWasGood = stub.doTick(Empty.getDefaultInstance()).value
        if (!tickWasGood) {
            throw SimulationClockService.ManualTickForbiddenException()
        }
    }

    override val millis: Long
        get() = runBlocking(Dispatchers.IO) {
            stub.getMillis(Empty.getDefaultInstance()).value
        }
}