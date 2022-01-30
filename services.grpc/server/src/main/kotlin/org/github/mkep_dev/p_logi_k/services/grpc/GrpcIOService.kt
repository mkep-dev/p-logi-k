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

import com.google.protobuf.Empty
import com.google.protobuf.StringValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import mu.KLogger
import org.github.mkep_dev.p_logi_k.io.TypeMismatchException
import org.github.mkep_dev.p_logi_k.model.io.DataDirection
import org.github.mkep_dev.p_logi_k.services.api.IOService
import org.github.mkep_dev.p_logi_k.services.grpc.stub.*

class GrpcIOService(private val ioService: IOService, private val logger: KLogger) :
    IOServiceGrpcKt.IOServiceCoroutineImplBase() {

    private val inputSubs = mutableMapOf<SubscriptionId, IOService.ValueChangeSubscriptionHandle>()
    private val outputSubs = mutableMapOf<SubscriptionId, IOService.ValueChangeSubscriptionHandle>()

    private data class SubscriptionId(val cliendId: Long, val regex: String)

    override suspend fun getIOValue(request: StringValue) = valueInfo {
        val ioValue = ioService.getIOValue(request.value)
        if (ioValue == null) {
            success = false
        } else {
            value = GenericValueHelper.mapGenericValueToMsg(ioValue)
            success = value.type != Type.UNKNOWN
        }
    }

    override suspend fun setInput(request: SetInputRequest): SetInputFeedback {
        logger.trace { "Got request to update input '${request.name}' to '${request.newValue}' " }
        val feedbackBuilder = SetInputFeedback.newBuilder()
        val newValue = GenericValueHelper.mapMsgToGenericValue(request.newValue)
        if (newValue != null) {
            try {
                ioService.setInput(request.name, newValue)
                feedbackBuilder.success = true
            } catch (typeMismatchException: TypeMismatchException) {
                feedbackBuilder.typeMismatch = true
            } catch (noSuchElementException: NoSuchElementException) {
                feedbackBuilder.notFound = true
            }
        } else {
            feedbackBuilder.typeMismatch = true
        }

        return feedbackBuilder.build()
    }

    override fun findIOsMatchingRegex(request: StringValue): Flow<IOElement> {
        return ioService.getIOs(request.value.toRegex()).asFlow().map {
            iOElement {
                id = it.identifier
                direction = when (it.direction) {
                    DataDirection.IN -> Direction.IN
                    DataDirection.OUT -> Direction.OUT
                }
                value = GenericValueHelper.mapGenericValueToMsg(it.value)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun registerForInputChanges(request: SubscriptionRequest): Flow<IOChange> {
        logger.debug { "New subscription input request '$request'." }

        val channel = Channel<IOChange>(Channel.Factory.BUFFERED)

        val changeListener = object : IOService.ValueChangeListener {
            override fun onChange(
                name: String,
                oldValue: org.github.mkep_dev.p_logi_k.io.GenericValue<Any>,
                newValue: org.github.mkep_dev.p_logi_k.io.GenericValue<Any>
            ) {
                runBlocking {
                    channel.send(iOChange {
                        this.name = name
                        old = GenericValueHelper.mapGenericValueToMsg(oldValue)
                        new = GenericValueHelper.mapGenericValueToMsg(newValue)
                    })
                }
            }

        }
        val handle = ioService.addOnInputChangeListener(request.regex.toRegex(), changeListener)

        channel.invokeOnClose {
            handle?.stop()
        }

        val subId = SubscriptionId(request.clientId, request.regex)
        if (handle == null || inputSubs.contains(subId)) {
            channel.close()
        } else {
            inputSubs[subId] = handle
        }


        return channel.consumeAsFlow()
    }

    override suspend fun unregisterForInputChanges(request: UnsubscriptionRequest): Empty {
        inputSubs.remove(SubscriptionId(request.clientId, request.regex))?.stop()

        return Empty.getDefaultInstance()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun registerForOutputChanges(request: SubscriptionRequest): Flow<IOChange> {
        logger.debug { "New subscription output request '$request'." }

        val channel = Channel<IOChange>(Channel.Factory.BUFFERED)

        val changeListener = object : IOService.ValueChangeListener {
            override fun onChange(
                name: String,
                oldValue: org.github.mkep_dev.p_logi_k.io.GenericValue<Any>,
                newValue: org.github.mkep_dev.p_logi_k.io.GenericValue<Any>
            ) {
                runBlocking {
                    channel.send(iOChange {
                        this.name = name
                        this.old = GenericValueHelper.mapGenericValueToMsg(oldValue)
                        this.new = GenericValueHelper.mapGenericValueToMsg(newValue)
                    }.also { logger.trace { "Send update: $it" } })
                }
            }

        }
        val handle = ioService.addOnOutputChangeListener(request.regex.toRegex(), changeListener)

        channel.invokeOnClose {
            handle?.stop()
        }

        val subId = SubscriptionId(request.clientId, request.regex)
        if (handle == null || outputSubs.contains(subId)) {
            channel.close()
        } else {
            outputSubs[subId] = handle
        }


        return channel.consumeAsFlow()
    }

    override suspend fun unregisterForOutputChanges(request: UnsubscriptionRequest): Empty {
        outputSubs.remove(SubscriptionId(request.clientId, request.regex))?.stop()

        return Empty.getDefaultInstance()

    }

    override fun getIOMap(request: Empty): Flow<IOReference> {
        return ioService.getIOMap().map {
            val typeClass = when(it.valueClass){
                Boolean::class -> Type.BOOLEAN
                Int::class -> Type.INTEGER
                Double::class -> Type.DOUBLE
                else -> Type.UNRECOGNIZED
            }
            val dir = when(it.direction){
                DataDirection.IN -> Direction.IN
                DataDirection.OUT -> Direction.OUT
            }
            iOReference {
                id = it.identifier
                type = typeClass
                direction = dir
            }
        }.asFlow()
    }
}