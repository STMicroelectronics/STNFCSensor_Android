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

import com.st.smartaglib.util.leUInt
import com.st.smartaglib.util.toLeUInt32
import java.util.*

private const val BASE_YEAR = 2018   // year offset

/**
 * convert a date in the format compatible with the nfc firmware
 */
internal fun GregorianCalendar.pack():ByteArray{
    var tC = 0L
    tC = tC or (get(GregorianCalendar.SECOND).toLong() and 0x0000003F)
    tC = tC or (get(GregorianCalendar.MINUTE).toLong() shl 6 and 0x00000FC0)
    tC = tC or (get(GregorianCalendar.HOUR_OF_DAY).toLong() shl 12 and 0x0001F000)
    tC = tC or ((get(GregorianCalendar.MONTH) + 1).toLong() shl 17 and 0x001E0000)
    tC = tC or (get(GregorianCalendar.DAY_OF_MONTH).toLong() shl 21 and 0x03E00000)
    tC = tC or ((get(GregorianCalendar.YEAR) - BASE_YEAR).toLong() shl 26 and -0x4000000)
    return tC.toLeUInt32
}

/**
 * extract the data from a nfc memory cell
 */
internal fun unpackGregorianCalendar(rawData: ByteArray):GregorianCalendar{
    val longDate =rawData.leUInt and 0x7FFFFFFFL // ingore the first bit
    val year = (longDate.ushr(26).toByte().toInt() and 0x003F)      // 6 bits
    val day = (longDate.ushr(21).toByte().toInt() and 0x001F)      // 5 bits
    val month = (longDate.ushr(17).toByte().toInt() and 0x000F)     // 4 bits;
    val hour = (longDate.ushr(12).toByte().toInt() and 0x001F)      // 5 bits;
    val min = (longDate.ushr(6).toByte().toInt() and 0x003F)        // 6 bits;
    val sec = (longDate.toByte().toInt() and 0x003F)              // 6 bits;
    val date = GregorianCalendar()
    date.set(GregorianCalendar.YEAR, BASE_YEAR + year)
    date.set(GregorianCalendar.MONTH, month - 1)
    date.set(GregorianCalendar.DAY_OF_MONTH, day)
    date.set(GregorianCalendar.HOUR_OF_DAY, hour)
    date.set(GregorianCalendar.MINUTE, min)
    date.set(GregorianCalendar.SECOND, sec)
    return date
}