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

package com.st.smartaglib.util

import java.nio.ByteBuffer
import java.nio.ByteOrder

//fun ByteArray.extractShortFrom(index: Int=0): Short = (this[index+1].toInt() shl 8 or this[index].toInt()).toShort()
val ByteArray.leUShort: Int
    get() {return this.extractLeUShortFrom(0)}

/**
 * extract a short value merging 2 consecutive bytes, the msb is in [index+1] , and the lsb in [index]
 */
fun ByteArray.extractLeUShortFrom(index: Int): Int = ByteBuffer.wrap(this,index,2).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xFFFF


val ByteArray.leUInt: Long
    get() {return this.extractUIntFrom(0)}

/**
 * extract a UInt32 value merging 4 consecutive bytes, the msb is in [index+3] , and the lsb in [index]
 */
fun ByteArray.extractUIntFrom(index: Int=0): Long {
    return (ByteBuffer.wrap(this, index, 4).order(ByteOrder.LITTLE_ENDIAN).int)
            .toLong() and 0xFFFFFFFFL
}

fun ByteArray.extractBEUIntFrom(index: Int=0): Long {
    return (ByteBuffer.wrap(this, index, 4).order(ByteOrder.BIG_ENDIAN).int)
            .toLong() and 0xFFFFFFFFL
}

/**
 * Returns the bytes array in little endian format of the value to convert.
 *
 * @return the bytes array in little endian of the value, the array is 4 bytes length
 */
val Long.toLeUInt32: ByteArray
    get() = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((this and 0xFFFFFFFFL).toInt()).array()

/**
 * get the last significative byte of the short
 */
fun Short.lsb() = this.toByte()

/**
 * get the most significative byte of the short
 */
fun Short.msb() = (this.toInt() shr 8).toByte()