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

import com.st.smartaglib.model.SamplingConfiguration
import com.st.smartaglib.model.SensorConfiguration
import com.st.smartaglib.model.Threshold
import com.st.smartaglib.util.leUShort
import com.st.smartaglib.util.lsb
import com.st.smartaglib.util.msb
import kotlin.experimental.and

/** encode a sampling mode into a byte value */
private fun SamplingConfiguration.Mode.pack():Byte{
    return when(this){
        SamplingConfiguration.Mode.Inactive -> 0.toByte()
        SamplingConfiguration.Mode.Sampling -> 1.toByte()
        SamplingConfiguration.Mode.OneShot -> 2.toByte()
        SamplingConfiguration.Mode.SamplingWithThreshold -> 3.toByte()
        SamplingConfiguration.Mode.SaveNextSample -> 4.toByte()
        SamplingConfiguration.Mode.Unknown -> throw IllegalArgumentException("Unknown Mode not supported")
    }
}

/**
 * decode the sampling mode from a byte
 */
private fun parseSamplingMode(rawValue:Byte): SamplingConfiguration.Mode {
    return when(rawValue){
        0.toByte() -> SamplingConfiguration.Mode.Inactive
        1.toByte() -> SamplingConfiguration.Mode.Sampling
        2.toByte() -> SamplingConfiguration.Mode.OneShot
        3.toByte() -> SamplingConfiguration.Mode.SamplingWithThreshold
        4.toByte() -> SamplingConfiguration.Mode.SaveNextSample
        else -> SamplingConfiguration.Mode.Unknown
    }
}


/**
 * encode the enabled sensor using a bit for each sensor.
 * bit #1 = temperature
 * bit #2 = humidity
 * bit #3 = pressure
 * bit #4 = accelerometer
 * bit #5 = orientation
 * bit #6 = wakeUp
 */
private fun packFlags(conf: SamplingConfiguration): Byte {
    var packValue = 0
    if (conf.temperatureConf.isEnable)
        packValue = packValue or 0x01
    if (conf.humidityConf.isEnable)
        packValue = packValue or 0x02
    if (conf.pressureConf.isEnable)
        packValue = packValue or 0x04
    if (conf.accelerometerConf.isEnable)
        packValue = packValue or 0x08
    if (conf.orientationConf.isEnable)
        packValue = packValue or 0x10
    if (conf.wakeUpConf.isEnable)
        packValue = packValue or 0x20
    return packValue.toByte()
}

/**
 * encode the sampling configuration into a  nfc memory cell.
 */
internal fun SamplingConfiguration.pack():ByteArray{
    val interval = samplingInterval_s.toShort()
    val mode = mode.pack()
    return byteArrayOf(
            interval.lsb(),
            interval.msb(),
            mode, //isEnable
            packFlags(this))
}

/**
 * extract the bit in position [position], it return true if the bit is 1 false otherwise
 */
private fun Byte.extractBit(position: Int): Boolean = (this and (1 shl position).toByte()) != 0.toByte()

/**
 * restore the sampling configuration from a nfc memory cell.
 * [data] nfc memory cell containing the enabled sensors, the sampling mode and the sampling interval
 * [tempHumTh] min and max to use as threshold for the temperature and the humidity data.
 *  The value will be recorded only if it is outside the range
 * [presAccTh] min and max for the pressure, and min for the acceleration.
 */
internal fun unpackSamplingConfiguration(data: ByteArray, tempHumTh: MaxMinTempHumCell,
                                presAccTh: MaxMinPresAccCell): SamplingConfiguration {

    //data[0,1] = sampling interval
    val samplingInterval = data.leUShort
    val mode = parseSamplingMode(data[2])
    val logStatus = data[3]
    val logTemperature = logStatus.extractBit(0)
    val logHumidity = logStatus.extractBit(1)
    val logPressure = logStatus.extractBit(2)
    val logAcceleration = logStatus.extractBit(3)
    val logOrientation = logStatus.extractBit(4)
    val logWakeUp = logStatus.extractBit(5)

    return SamplingConfiguration(
            samplingInterval_s = samplingInterval,
            mode = mode,
            temperatureConf = SensorConfiguration(logTemperature, Threshold(tempHumTh.tempMax, tempHumTh.tempMin)),
            pressureConf = SensorConfiguration(logPressure, Threshold(presAccTh.maxPressure, presAccTh.minPressure)),
            humidityConf = SensorConfiguration(logHumidity, Threshold(tempHumTh.humMax, tempHumTh.humMin)),
            accelerometerConf = SensorConfiguration(logAcceleration, Threshold(presAccTh.accelerationValue, null)),
            orientationConf = SensorConfiguration(logOrientation, Threshold(null, null)),
            wakeUpConf = SensorConfiguration(logWakeUp, Threshold(presAccTh.accelerationValue, null))
    )
}