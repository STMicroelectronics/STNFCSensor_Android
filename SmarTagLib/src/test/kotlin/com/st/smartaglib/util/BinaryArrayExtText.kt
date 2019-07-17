package com.st.smartaglib.util

import org.junit.Assert
import org.junit.Test


internal class BinaryArrayExtTest {

    @Test
    fun byteToShortWorkInLittleEndian() {

        var shortValue = byteArrayOf(0x00, 0x00).leUShort
        Assert.assertEquals(0, shortValue)

        shortValue = byteArrayOf(0xFF.toByte(), 0xFF.toByte()).leUShort
        Assert.assertEquals(65535, shortValue)

        shortValue = byteArrayOf(0xFF.toByte(), 0x7F.toByte()).leUShort
        Assert.assertEquals(32767, shortValue)

        shortValue = byteArrayOf(0x00.toByte(), 0x80.toByte()).leUShort
        Assert.assertEquals(32768, shortValue)

        shortValue = byteArrayOf(0xFF.toByte(), 0x00.toByte()).leUShort
        Assert.assertEquals(255, shortValue)

        shortValue = byteArrayOf(0x00.toByte(), 0xFF.toByte()).leUShort
        Assert.assertEquals(65280, shortValue)

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
    fun unsignedIntToByteWorkInLittleEndian() {
        var bytes = 0L.toLeUInt32
        var expeted = byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())
        Assert.assertTrue(expeted  contentEquals  bytes)

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
