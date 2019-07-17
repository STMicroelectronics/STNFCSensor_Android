/*
 * Copyright (c) 2018  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *    and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *    STMicroelectronics company nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *    in a directory whose title begins with st_images may only be used for internal purposes and
 *    shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *    icons, pictures, logos and other images that are provided with the source code in a directory
 *    whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package com.st.smarTag.cloud.provider.json

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.st.smartaglib.model.*
import java.io.IOException
import java.lang.reflect.Type


internal open class CustomizedTypeAdapterFactory<C>(private val customizedClass: Class<C> ) : TypeAdapterFactory {

    override// we use a runtime check to guarantee that 'C' and 'T' are equal
    fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        return if (type.rawType == customizedClass)
            customizeMyClassAdapter(gson, type as TypeToken<C>) as TypeAdapter<T>
        else
            null
    }

    private fun customizeMyClassAdapter(gson: Gson, type: TypeToken<C>): TypeAdapter<C> {
        val delegate = gson.getDelegateAdapter(this, type)
        val elementAdapter = gson.getAdapter(JsonElement::class.java)
        return object : TypeAdapter<C>() {
            @Throws(IOException::class)
            override fun write(out: JsonWriter, value: C) {
                val tree = delegate.toJsonTree(value)
                val newTree = beforeWrite(value, tree)
                elementAdapter.write(out, newTree)
            }

            @Throws(IOException::class)
            override fun read(`in`: JsonReader): C {
                val tree = elementAdapter.read(`in`)
                val newTree = afterRead(tree)
                return delegate.fromJsonTree(newTree)
            }
        }
    }

    /**
     * Override this to muck with `toSerialize` before it is written to
     * the outgoing JSON stream.
     */
    open fun beforeWrite(source: C, toSerialize: JsonElement) : JsonElement = toSerialize

    /**
     * Override this to muck with `deserialized` before it parsed into
     * the application type.
     */
    open fun afterRead(deserialized: JsonElement) :JsonElement = deserialized
}

internal class SensorDataSampleJsonFactory : CustomizedTypeAdapterFactory<SensorDataSample>(SensorDataSample::class.java) {
    override fun beforeWrite(source: SensorDataSample, toSerialize: JsonElement) : JsonElement{
        val newRoot = JsonObject()
        newRoot.add("SensorData",toSerialize)
        return newRoot
    }
}

internal class EventDataSampleJsonFactory : CustomizedTypeAdapterFactory<EventDataSample>(EventDataSample::class.java) {
    override fun beforeWrite(source: EventDataSample, toSerialize: JsonElement) : JsonElement{
        val newRoot = JsonObject()
        newRoot.add("EventData",toSerialize)
        return newRoot
    }
}

internal class ExtremeDataJsonFactory : CustomizedTypeAdapterFactory<TagExtreme>(TagExtreme::class.java) {
    override fun beforeWrite(source: TagExtreme, toSerialize: JsonElement) : JsonElement{
        val newRoot = JsonObject()
        newRoot.add("Extremes",toSerialize)
        return newRoot
    }
}

private const val DATE = "date"
private const val ACCELERATION = "acc"
private const val PRESSURE = "pres"
private const val TEMPERATURE = "temp"
private const val HUMIDITY = "hum"
private const val EVENTS = "evn"
private const val ORIENTATION = "ori"
private const val START_ACQUISITION = "started"

private const val MINDATE = "minDate"
private const val MIN = "min"
private const val MAXDATE = "maxDate"
private const val MAX = "max"

internal class SensorDataSampleJsonAdapter : JsonSerializer<SensorDataSample> {
    override fun serialize(src: SensorDataSample, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {

        val obj = JsonObject()

        obj.add(DATE, context.serialize(src.date))

        src.acceleration?.let{acc ->
            obj.addProperty(ACCELERATION,acc)
        }

        src.pressure?.let {pres ->
            obj.addProperty(PRESSURE,pres)
        }

        src.temperature?.let { temp ->
            obj.addProperty(TEMPERATURE,temp)
        }

        src.humidity?.let { hum ->
            obj.addProperty(HUMIDITY,hum)
        }

        return obj
    }
}

internal class EventDataSampleJsonAdapter : JsonSerializer<EventDataSample> {
    override fun serialize(src: EventDataSample, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {

        val obj = JsonObject()

        obj.add(DATE, context.serialize(src.date))
        obj.add(EVENTS,context.serialize(src.events))

        src.acceleration?.let{acc ->
            obj.addProperty(ACCELERATION,acc)
        }

        if(src.currentOrientation!=Orientation.UNKNOWN){
            obj.add(ORIENTATION,context.serialize(src.currentOrientation))
        }

        return obj
    }
}

internal class DataExtremesJsonAdapter : JsonSerializer<DataExtreme> {

    override fun serialize(src: DataExtreme, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {

        val obj = JsonObject()

        if(!src.minValue.isNaN()){
            obj.add(MINDATE,context.serialize(src.minDate))
            obj.addProperty(MIN,src.minValue)
        }

        if(!src.maxValue.isNaN()){
            obj.add(MAXDATE,context.serialize(src.maxDate))
            obj.addProperty(MAX,src.maxValue)
        }

        return obj

    }

}

internal class TagExtremesJsonAdapter : JsonSerializer<TagExtreme> {
    override fun serialize(src: TagExtreme, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val obj = JsonObject()

        obj.add(START_ACQUISITION, context.serialize(src.acquisitionStart))

        src.humidity?.let {
            obj.add(HUMIDITY, context.serialize(it))
        }

        src.temperature?.let {
            obj.add(TEMPERATURE, context.serialize(it))
        }

        src.pressure?.let {
            obj.add(PRESSURE, context.serialize(it))
        }

        src.vibration?.let {
            obj.add(ACCELERATION, context.serialize(it))
        }

        return obj
    }
}
