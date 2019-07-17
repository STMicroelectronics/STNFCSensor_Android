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
import com.st.smartaglib.util.toLeUInt32
import kotlin.math.roundToLong

/**
 * content of a cell with the min and max for the pressure and accelerometer
 */
internal data class MaxMinPresAccCell(
        val minPressure:Float,
        val maxPressure:Float,
        val accelerationValue:Float)

/**
 * encode the structure data into a nfc memory cell, the pressure is encoded with one decimal position,
 * the accelerometer is encoded removing 6 lsb.
 */
internal fun MaxMinPresAccCell.pack():ByteArray{
    val pressMin = ((minPressure - SmarTag.PRESSURE_RANGE_MBAR.start)*10.0f).toLong()
    val pressMax = ((maxPressure - SmarTag.PRESSURE_RANGE_MBAR.start)*10.0f).toLong()
    //do a proper rounding insad of trouncate the acceleration
    val accValue = (accelerationValue/256.0).roundToLong()

    var packValue = (accValue and 0x3F).shl(24)
    packValue = packValue or (pressMin and 0xFFF).shl(12)
    packValue = packValue or (pressMax and 0xFFF)
    return packValue.toLeUInt32
}

/**
 * extract the min max information from a nfc memory cell
 */
internal fun unpackMaxMinPresAccCell(rawData: ByteArray): MaxMinPresAccCell {
    val longData = rawData.leUInt
    val accelerationValue = (((longData ushr 24) and 0x0000003F) shl 8).toFloat() // 256mg/LSB
    val minPressure = ((longData.ushr(12) and 0xFFF)/10.0f + SmarTag.PRESSURE_RANGE_MBAR.start) // Minimum Pressure 12 bits
    val maxPressure = ((longData and 0xFFF)/10.0f + SmarTag.PRESSURE_RANGE_MBAR.start)// Maximum Pressure 12 bits
    return MaxMinPresAccCell(minPressure, maxPressure, accelerationValue)

}
