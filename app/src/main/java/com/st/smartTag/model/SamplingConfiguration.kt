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

package com.st.smartTag.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Class containing a threshold, the data will be logged only if the value is outside this threshold
 */
data class Threshold(val max:Float?,val min:Float?) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readValue(Float::class.java.classLoader) as? Float,
            parcel.readValue(Float::class.java.classLoader) as? Float) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(max)
        parcel.writeValue(min)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Threshold> {
        override fun createFromParcel(parcel: Parcel): Threshold {
            return Threshold(parcel)
        }

        override fun newArray(size: Int): Array<Threshold?> {
            return arrayOfNulls(size)
        }
    }

}

/**
 * configuration for a specific sensor
 * [isEnable] tell if the smartag will log this sensor
 * [threshold] tell when log the data (only if the log mode is [SamplingConfiguration.Mode.SamplingWithThreshold])
 */
data class SensorConfiguration(
        val isEnable:Boolean,
        val threshold: Threshold) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            parcel.readParcelable(Threshold::class.java.classLoader)) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isEnable) 1 else 0)
        parcel.writeParcelable(threshold, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SensorConfiguration> {
        override fun createFromParcel(parcel: Parcel): SensorConfiguration {
            return SensorConfiguration(parcel)
        }

        override fun newArray(size: Int): Array<SensorConfiguration?> {
            return arrayOfNulls(size)
        }
    }

}

/**
 * SmarTag configuration structure
 * [samplingInterval_s] interval between sampling
 * [temperatureConf] configuration to use for the temperature sensor
 * [humidityConf] configuration to use for the humidity sensor
 * [pressureConf] configuration to use for the pressure sensor
 * [mode] logging mode
 */
data class SamplingConfiguration(
        val samplingInterval_s: Int,
        val temperatureConf:SensorConfiguration,
        val humidityConf:SensorConfiguration,
        val pressureConf:SensorConfiguration,
        val accelerometerConf:SensorConfiguration,
        val orientationConf:SensorConfiguration,
        val wakeUpConf:SensorConfiguration,
        val mode:Mode ):Parcelable{

    /**
     * Logging mode
     * [Inactive] don't log anything
     * [Sampling] take a sampling each [samplingInterval_s],  without the threshold
     * [OneShot] not used,
     * [SamplingWithThreshold] take a sampling each [samplingInterval_s] and store it only if it is outside the threshold
     * [SaveNextSample] force to store the next sample without looking the threshold
     */
    enum class Mode {
        Inactive,
        Sampling,
        OneShot,
        SamplingWithThreshold,
        SaveNextSample,
        Unknown
    }

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readParcelable(SensorConfiguration::class.java.classLoader),
            parcel.readParcelable(SensorConfiguration::class.java.classLoader),
            parcel.readParcelable(SensorConfiguration::class.java.classLoader),
            parcel.readParcelable(SensorConfiguration::class.java.classLoader),
            parcel.readParcelable(SensorConfiguration::class.java.classLoader),
            parcel.readParcelable(SensorConfiguration::class.java.classLoader),
            Mode.valueOf(parcel.readString())) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(samplingInterval_s)
        parcel.writeParcelable(temperatureConf, flags)
        parcel.writeParcelable(humidityConf, flags)
        parcel.writeParcelable(pressureConf, flags)
        parcel.writeParcelable(accelerometerConf, flags)
        parcel.writeParcelable(orientationConf, flags)
        parcel.writeParcelable(wakeUpConf, flags)
        parcel.writeString(mode.toString())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SamplingConfiguration> {
        override fun createFromParcel(parcel: Parcel): SamplingConfiguration {
            return SamplingConfiguration(parcel)
        }

        override fun newArray(size: Int): Array<SamplingConfiguration?> {
            return arrayOfNulls(size)
        }
    }

}
