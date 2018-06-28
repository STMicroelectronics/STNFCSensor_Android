package com.st.smartTag.nfc

import com.st.smartTag.util.extractBEUIntFrom
import kotlin.experimental.and

data class NDefRecordHeader(val tnf: Byte,
                            val typeLength: Byte,
                            val payloadLength: Long){

    //tag type is the last 3 bit
    val type:Byte
    get() = tnf.and(0x07)

    val isShortRecord:Boolean
    get() = isShortRecord(tnf)

    val length:Short
    get() = if(isShortRecord) 3 else 6

    val isLastRecord:Boolean
    get() = (tnf and 0x40) != 0.toByte()
}

fun ST25DVTag.readStringFromByteOffset(byteOffset:Short, length:Short):String{
    //we can read 4 byte at the time
    val startCellOffset = byteOffset.rem(4)
    val startRead = ((byteOffset-startCellOffset)/4).toShort()
    val endRead = (startRead + length.div(4) + 1).toShort() // +1 if the value is not multiple of 4

    val finalString = StringBuffer()

    (startRead..endRead).forEach { i ->
        val byteString = read(i.toShort())
        byteString.forEach { finalString.append(it.toChar()) }
    }
    return finalString.substring(startCellOffset,startCellOffset+length);

}

private fun isShortRecord(tnf:Byte):Boolean = tnf.and(0x10) == 1.toByte()

fun ST25DVTag.getNDefRecordFromOffset(offset:Short):NDefRecordHeader{
    val header_part1 = read(offset)
    val header_part2 = read(offset.inc())
    val header = header_part1+header_part2
    val payloadLength:Long = if (isShortRecord(header[0])){
        header[2].toLong()
    }else{
        header.extractBEUIntFrom(2)
    }
    return NDefRecordHeader(
            tnf = header[0],
            typeLength = header[1],
            payloadLength = payloadLength
    )


}

