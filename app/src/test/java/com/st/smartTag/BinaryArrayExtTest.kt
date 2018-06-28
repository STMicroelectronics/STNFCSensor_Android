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

package com.st.smartTag

import com.st.smartTag.util.leShort
import com.st.smartTag.util.leUInt
import com.st.smartTag.util.toLeUInt32
import junit.framework.Assert
import org.junit.Test
import java.util.*

class BinaryArrayExtTest{

    @Test
    fun byteToShortWorkInLittleEndian(){

        var shortValue = byteArrayOf(0x00,0x00).leShort
        Assert.assertEquals(0,shortValue)

        shortValue = byteArrayOf(0xFF.toByte(), 0xFF.toByte()).leShort
        Assert.assertEquals(-1,shortValue)

        shortValue = byteArrayOf(0xFF.toByte(), 0x7F.toByte()).leShort
        Assert.assertEquals(32767,shortValue)

        shortValue = byteArrayOf(0x00.toByte(), 0x80.toByte()).leShort
        Assert.assertEquals(-32768,shortValue)

        shortValue = byteArrayOf(0xFF.toByte(), 0x00.toByte()).leShort
        Assert.assertEquals(255,shortValue)

        shortValue = byteArrayOf(0x00.toByte(), 0xFF.toByte()).leShort
        Assert.assertEquals(-256,shortValue)

    }

    @Test
    fun byteToUnsignedIntWorkInLittleEndian() {

        var number = byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()).leUInt
        Assert.assertEquals(0L, number)

        number = byteArrayOf(0xFF.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()).leUInt
        Assert.assertEquals(255L, number)

        number = byteArrayOf(0x00.toByte(), 0x00.toByte(), 0xFF.toByte(), 0xFF.toByte()).leUInt
        Assert.assertEquals(4294901760L, number)

        number = byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x80.toByte()).leUInt
        Assert.assertEquals(2147483648L, number)

        number = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x7F.toByte()).leUInt
        Assert.assertEquals(2147483647L, number)

        number = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()).leUInt
        Assert.assertEquals(4294967295L, number)

    }

    @Test
    fun unsignedIntToByteWorkInLittleEndian(){
        var bytes = 0L.toLeUInt32
        var expeted = byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())
        Assert.assertTrue(expeted contentEquals bytes)

        bytes = 255L.toLeUInt32
        expeted = byteArrayOf(0xFF.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())
        Assert.assertTrue(expeted contentEquals bytes)

        bytes = 4294901760L.toLeUInt32
        expeted = byteArrayOf(0x00.toByte(), 0x00.toByte(), 0xFF.toByte(), 0xFF.toByte())
        Assert.assertTrue(expeted contentEquals bytes)

        bytes = 2147483648L.toLeUInt32
        expeted = byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x80.toByte())
        Assert.assertTrue(expeted contentEquals bytes)

        bytes = 2147483647L.toLeUInt32
        expeted = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x7F.toByte())
        Assert.assertTrue(expeted contentEquals bytes)

        bytes = 4294967295L.toLeUInt32
        expeted = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
        Assert.assertTrue(expeted contentEquals bytes)
    }

}


