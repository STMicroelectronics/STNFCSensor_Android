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

package com.st.smartTag.nfc

import android.nfc.Tag
import android.nfc.tech.NfcV
import com.st.smartTag.util.lsb
import com.st.smartTag.util.msb
import java.io.IOException

/**
 * class with utility function to interact with an ST25DV Tag
 */
class ST25DVTag (private val tag:NfcV){

    /**
     * write a tag memory cell
     * [address] cell address to write
     * [data] cell data to write, the data cell must be of 4 bytes otherwise an exception will be thrown
     * @throws ST25DVException if the data length is not 4
     * @throws IOException if there is some IO problems
     */
    fun write(address:Short, data:ByteArray){
        //Log.d("ST25DVTag","Write: "+address+" data:"+ Arrays.toString(data))
        if(data.size!=4){
            throw ST25DVException("The memory is write at block of 4 bytes, impossible write "+data.size +"bytes")
        }
        val writeCommand = ByteArray(8)
        writeCommand[0] = ST25DV_REQUEST_HEADER
        writeCommand[1] = WRITE_COMMAND
        writeCommand[2] = address.lsb()
        writeCommand[3] = address.msb()
        writeCommand[4] = data[0]
        writeCommand[5] = data[1]
        writeCommand[6] = data[2]
        writeCommand[7] = data[3]
        safeTransceive(writeCommand)
    }

    /**
     * read a nfc memory cell
     * [address] cell to read
     * it return an ByteArray of 4 elements.
     * @throws IOException
     */
    fun read(address: Short): ByteArray{
        //Log.d("ST25DVTag","Read: "+address)

        val readCommand = byteArrayOf(
                ST25DV_REQUEST_HEADER,
                READ_COMMAND,
                address.lsb(),
                address.msb()
        )
        return safeTransceive(readCommand)
    }

    /**
     * send the default password to read the tag
     */
    private fun presentPassword(){
        val resetPassword = byteArrayOf(
                ST25DV_REQUEST_HEADER, 0xB3.toByte(), 0x02,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        safeTransceive(resetPassword)
    }

    /**
     * send the command to configure the gpo
     */
    private fun configureGPO(){
        val configureGPO = byteArrayOf(
                ST25DV_REQUEST_HEADER, 0xA1.toByte(), 0x02, 0x00, 0x81.toByte())
        safeTransceive(configureGPO)
    }

    /**
     * possible gpo level
     */
    enum class GPOLevel{
        Low,
        High
    }

    /**
     * change the gpo level to [level]
     */
    private fun setGPOLevel(level:GPOLevel){
        configureGPO()
        val levelValue = (if(level==GPOLevel.High) 0 else 1).toByte()
        safeTransceive(byteArrayOf(
                ST25DV_REQUEST_HEADER, 0xA9.toByte(), 0x02, levelValue))
    }

    /**
     *  open a connection with the tag
     */
    fun connect(){
        tag.connect()
        //presentPassword()
        //configureGPO()
        //setGPOLevel(GPOLevel.High)
    }

    /**
     * close a connection with the tag
     */
    fun close(){
        //presentPassword()
        //configureGPO()
        //setGPOLevel(GPOLevel.Low)
        tag.close()
    }


    /**
     * Turn ON Energy Harvesting of NFC EEPROM, this will power the attached microcontroller
     */
    fun enableEnergyHarvesting() {
        presentPassword()
        val enableEnergyHarvesting = byteArrayOf(
                ST25DV_REQUEST_HEADER,
                0xAE.toByte(),
                0x02,
                0x02,0x01
        )
        safeTransceive(enableEnergyHarvesting)
    }



    /**
     * Turn OFF Energy Harvesting of NFC EEPROM, this will power down the attached microcontroller
     */
    fun disableEnergyHarvesting() {
        presentPassword()
        val enableEnergyHarvesting = byteArrayOf(
                ST25DV_REQUEST_HEADER,
                0xAE.toByte(),
                0x02,
                0x02,0x00
        )
        safeTransceive(enableEnergyHarvesting)
    }

    /**
     * exec an I/O operation if the st25dv return an error code the command is try multiple times
     * end at the end the error code is thrown
     * @return ST25DV response values
     * @throws IOException if the communication fails
     */
    private fun safeTransceive(command: ByteArray): ByteArray {
        var nTry = 0
        var errorCode = COMMAND_OK
        do {
            val response = tag.transceive(command)
            errorCode = response[0]
            //Log.d("SmartTag","ErrorCode:"+errorCode)
            if (errorCode == COMMAND_OK)
                return response.copyOfRange(1,response.size)
            else {
                nTry++
                Thread.sleep(COMMAND_DELAY)
            }
        } while (nTry < COMMAND_RETRY)
        throw ST25DVException("IOError: code " + errorCode)
    }


    companion object {
        private val COMMAND_RETRY = 5
        private val COMMAND_DELAY = 5L
        private val COMMAND_OK: Byte = 0x00
        private val ST25DV_REQUEST_HEADER: Byte = 0x02
        private val READ_COMMAND: Byte = 0x30
        private val WRITE_COMMAND: Byte = 0x31

        /**
         * build a ST25DVTag object
         * @return if [tag] is a valid NfcV type tag an object of type ST25DVTag otherwise null
         */
        fun get(tag:Tag):ST25DVTag?{
            val nfcV = NfcV.get(tag)
            return if(nfcV!=null)
                ST25DVTag(nfcV)
            else
                null
        }

    }

}

class ST25DVException(message: String?) : IOException(message)