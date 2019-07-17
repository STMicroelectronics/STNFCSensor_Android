/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
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

package com.st.smartaglib.model

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.experimental.and
import kotlin.experimental.or

/**
 * common class to store a data sample it can be a sensor data or an event data
 */
sealed class DataSample:Serializable

/**
 * class containing a sensor sampling recorded on [date].
 * [temperature],[pressure],[humidity],[acceleration]  are null if the sensor is not enabled
 */
data class SensorDataSample(val date: Date,
                            val temperature: Float?,
                            val pressure: Float?,
                            val humidity: Float?,
                            val acceleration: Float?) : DataSample() {
}

/**
 * accelerometer event
 */
enum class AccelerationEvent(private val mask:Byte){
    WAKE_UP(0X01),
    ORIENTATION(0X02),
    SINGLE_TAP(0X04),
    DOUBLE_TAP(0X08),
    FREE_FALL(0X10),
    TILT(0X20);

    companion object {

        /**
         * since each bit is a separate event, this function extract all selected events
         */
        fun extractEvent(rawEvent:Byte):Array<AccelerationEvent>{
            val events = ArrayList<AccelerationEvent>()
            values().forEach {
                if( rawEvent and it.mask == it.mask)
                    events.add(it)
            }
            return events.toArray(arrayOfNulls(events.size))
        }

        /**
         * map all the events to a byte
         */
        fun packEvents(events:Array<AccelerationEvent>):Byte{
            var value:Byte=0
            events.forEach { value = value or it.mask }
            return value
        }
    }
}

/**
 * possible board orientation
 */
enum class Orientation(val raw:Byte){

    UNKNOWN(0x00),
    UP_RIGHT(0X01),
    TOP(0x02),
    DOWN_LEFT(0x03),
    BOTTOM(0x04),
    UP_LEFT(0x05),
    DOWN_RIGHT(0x06);


    companion object {
        /**
         * create an orientation or return [Orientation.UNKNOWN] if it is not a valid value
         */
        fun valueOf(raw:Byte):Orientation{
            return values().find { it.raw == raw } ?: UNKNOWN
        }
    }
}

/**
 * event Sample, this sample is generated when an accelerometer event is detected
 * [date] when the event happen
 * [acceleration] the accelerometer detected by the sensor (it is not available for each event)
 * [events] events detected by the accelerometer
 * [currentOrientation] current board orientation (it is mandatory when the [events] contains [AccelerationEvent.ORIENTATION])
 */
data class EventDataSample (
        val date:Date,
        val acceleration:Int?,
        val events:Array<AccelerationEvent>,
        val currentOrientation: Orientation
):DataSample(){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventDataSample

        if (date != other.date) return false
        if (acceleration != other.acceleration) return false
        if (!Arrays.equals(events, other.events)) return false
        if (currentOrientation != other.currentOrientation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = date.hashCode()
        acceleration?.let {
            result = 31 * result + it.hashCode()
        }
        result = 31 * result + Arrays.hashCode(events)
        result = 31 * result + currentOrientation.hashCode()
        return result
    }

}

fun List<DataSample>.getSensorDataSample() : List<SensorDataSample> = filterIsInstance<SensorDataSample>()
fun List<DataSample>.getEventDataSample() : List<EventDataSample> = filterIsInstance<EventDataSample>()