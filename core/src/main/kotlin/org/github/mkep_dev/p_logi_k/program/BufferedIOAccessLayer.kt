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

package org.github.mkep_dev.p_logi_k.program

import org.github.mkep_dev.p_logi_k.Identifier
import org.github.mkep_dev.p_logi_k.io.*
import org.github.mkep_dev.p_logi_k.model.io.DataDirection
import org.github.mkep_dev.p_logi_k.model.io.IOElement
import org.github.mkep_dev.p_logi_k.model.io.IORepository
import org.github.mkep_dev.p_logi_k.model.io.IoEdgePolarity
import org.github.mkep_dev.p_logi_k.program.api.IOAccessLayer
import kotlin.reflect.KClass

/**
 * Implementation that buffers the changes and reads updates from the repository when requested.
 *
 * @property ioRepository the io repository that is used to store/access the applied changes
 */
class BufferedIOAccessLayer(private val ioRepository: IORepository) : IOAccessLayer {

    private val inputs: MutableMap<Identifier, BufferedGenericInput<Any>> = mutableMapOf()
    private val outputs: MutableMap<Identifier, BufferedGenericOutput<Any>> = mutableMapOf()

   override fun <T:Any> getInput(id: Identifier,type:KClass<T>): GenericInput<T>? =
       inputs[id]?.takeIf { it.valueClass == type }?.let {
       @Suppress("UNCHECKED_CAST") // checked by take if
       it as GenericInput<T>
   }

    override fun <T:Any> getOutput(id: Identifier,type:KClass<T>): GenericOutput<T>? =
        outputs[id]?.takeIf { it.valueClass == type }?.let {
        @Suppress("UNCHECKED_CAST") // checked by take if
        it as GenericOutput<T>
    }

    /**
     * Apply the default values to the repository and set new allowed IOs.
     *
     * @param newInputs the new inputs
     * @param newOutputs the new outputs
     */
    fun initIOS(newInputs:Iterable<IOElement<Any>>, newOutputs: Iterable<IOElement<Any>>){
        // prepare IOs
        inputs.clear()
        inputs.putAll(
            newInputs
                .map { it.identifier to BufferedGenericInput(it.identifier, it.value) })
        outputs.clear()
        outputs.putAll(
            newOutputs
                .map { it.identifier to BufferedGenericOutput(it.identifier, it.value) })

        // apply new Output values to repository
        applyOutputChanges()
    }

    /**
     * Write the input changes to the repository
     *
     */
    fun applyOutputChanges(){
        // Update outputs
        outputs.values.forEach(BufferedGenericOutput<Any>::takeNewValue)
        // Write changes to repository
        ioRepository.changeIOs { outputs[it.identifier]?.getValue() }
    }

    /**
     * Read the new values from repository
     *
     */
    fun applyIOChangesFromRepository(){
        ioRepository.getAll().forEach { io ->
            when (io.direction) {
                DataDirection.IN -> inputs[io.identifier]?.updateValue(io.value)
                DataDirection.OUT -> outputs[io.identifier]?.updateValue(io.value)
            }
        }
    }

    /**
     * Class that wraps around an output to model its cyclic behaviour
     */
    private class BufferedGenericOutput<V:Any>(name: String, defaultValue: GenericValue<V>,
    ) : GenericOutput<V>, BufferedGenericInput<V>(name, defaultValue) {

        private var nextValue: GenericValue<V>? = null

        override fun setValue(value: GenericValue<V>) {
            if (value.valueClass != currentValue.valueClass) {
                throw TypeMismatchException(this, value.valueClass)
            }
            nextValue = value
        }

        fun takeNewValue() {
            // Take the new value if null
            nextValue?.let {
                currentValue = it
            }
        }
    }

    /**
     * Class that wraps around an input to model the cyclic behaviour of the input
     */
    private open class BufferedGenericInput<V:Any>(name: String, defaultValue: GenericValue<V>
    ) : GenericInput<V> {

        protected var currentValue: GenericValue<V> = defaultValue
        protected var _edgePolarity: IoEdgePolarity?

        override val identifier: Identifier = name

        override fun getValue(): GenericValue<V> = currentValue

        override val valueClass: KClass<V> = defaultValue.valueClass

        override val edgePolarity: IoEdgePolarity?
            get() = _edgePolarity

        init {
            _edgePolarity = if (valueClass == Boolean::class) {
                IoEdgePolarity.FLAT
            } else {
                null
            }
        }

        override fun equals(other: Any?): Boolean {
            return if (other is GenericDataHolder<*>) {
                identifier == other.identifier && currentValue == other.getValue()
            } else {
                super.equals(other)
            }
        }

        @Throws(TypeMismatchException::class)
        fun updateValue(newValue: GenericValue<V>) {
            if (newValue.valueClass != currentValue.valueClass) {
                throw TypeMismatchException(this, newValue.valueClass)
            }
            @Suppress("UNCHECKED_CAST") // if guarantees the cast
            if (_edgePolarity != null) {
                newValue as GenericValue<Boolean>
                val cur = currentValue as GenericValue<Boolean>
                _edgePolarity = when {
                    !cur.value && newValue.value -> IoEdgePolarity.RISING
                    cur.value && !newValue.value -> IoEdgePolarity.FALLING
                    else -> IoEdgePolarity.FLAT
                }
            }
            currentValue = newValue
        }

        override fun hashCode(): Int {
            var result = currentValue.hashCode()
            result = 31 * result + identifier.hashCode()
            return result
        }

    }

}