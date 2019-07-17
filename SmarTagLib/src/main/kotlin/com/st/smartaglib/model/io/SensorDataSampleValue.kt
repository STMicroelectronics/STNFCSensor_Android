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

package com.st.smartaglib.model.io


import com.st.smartaglib.SmarTag
import com.st.smartaglib.util.leUInt

private const val INVALID_TEMPERATURE = 0X007F
private const val INVALID_PRESSURE = 0x0FFF
private const val INVALID_ACC = 0x003F
private const val INVALID_HUMIDITY = 0X007F

/**
 * Struct containing the sensor sample, the value can be null when the sensor data is disabled or
 * not available
 */
internal data class SensorDataSampleValue(
        val temperature:Float?,
        val pressure:Float?,
        val humidity:Float?,
        val acceleration:Float?)

/**
 * extract the sensor data from a nfc memory cell.
 */
internal fun unpackSensorDataSampleValue(rawData:ByteArray): SensorDataSampleValue {
    val intDataValue = rawData.leUInt.toInt()

    var intValue = intDataValue.ushr(20) and 0x0FFF
    val pressure = if(intValue != INVALID_PRESSURE)
        (intValue/10.0f + SmarTag.PRESSURE_RANGE_MBAR.start) // 12 bits
    else null

    intValue = intDataValue.ushr(13) and 0x007F
    val temperature = if(intValue != INVALID_TEMPERATURE)
        (intValue) + SmarTag.TEMPERATURE_RANGE_C.start // 7 bits
        else null

    intValue = (intDataValue.ushr(6) and 0x007F)
    val humidity = if(intValue != INVALID_HUMIDITY)
        intValue + SmarTag.HUMIDITY_RANGE.start
        else null

    intValue = (intDataValue and 0x003F)
    val acceleration = if(intValue != INVALID_ACC)
        (intValue shl 8) +SmarTag.ACCELERATION_RANGE_MG.start // 6 bits * 256mg/LSB
        else null

    return SensorDataSampleValue(
            temperature = temperature,
            humidity = humidity,
            acceleration = acceleration,
            pressure = pressure
    )

}