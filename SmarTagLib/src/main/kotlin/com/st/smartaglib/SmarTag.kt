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

package com.st.smartaglib

import com.st.smartaglib.model.io.*
import com.st.smartaglib.model.*
import com.st.smartaglib.util.toLeUInt32
import java.util.*
import kotlin.experimental.and

class SmarTag(private val tag: SmarTagIO) {

    private val memoryLayout by lazy {
        val tagSize = if (hasExtendedCCFile()) NFCTAG_64K_SIZE else NFCTAG_4K_SIZE
        SmarTagMemoryLayout(tagSize, findSmarTagRecord(tagSize))
    }

    private fun findSmarTagRecord(tagSize:Short):Short{
        var offset:Short = if (hasExtendedCCFile()) 3 else 2
        do {
            val recordHeader = tag.getNDefRecordFromOffset(offset)
            if (recordHeader.type == NDEF_EXTERNAL_TYPE) {
                // *4 since we want the byte offset not the cell offset
                val recordType = tag.readStringFromByteOffset((offset*4 + recordHeader.length).toShort(), recordHeader.typeLength.toShort())
                if (recordType.equals(NDEF_SMARTAG_TYPE)) {
                    val payloadOffset = recordHeader.typeLength + recordHeader.length + recordHeader.idLength
                    return (offset+payloadOffset.div(4)).toShort()
                }
            }
            val recordSize = (recordHeader.length + recordHeader.typeLength + recordHeader.payloadLength + recordHeader.idLength)
            offset = (offset + recordSize.div(4)).toShort()
        }while (offset<(tagSize-1) && !recordHeader.isLastRecord)
        return offset
    }

    private fun hasExtendedCCFile(): Boolean {
        val cc = tag.read(0)
        //cc = 0xE2 0x40 length1 length2
        return cc[2]== EXTENDED_CC_LENGTH
    }

    private fun write(address:Short, data:ByteArray){
        tag.write((memoryLayout.ndefHeaderSize+address).toShort(),data)
    }

    private fun writeZero(address: Short){
        write(address,0L.toLeUInt32)
    }

    private fun read(address: Short) : ByteArray{
        return tag.read((memoryLayout.ndefHeaderSize+address).toShort())
    }


    private fun writeLogConfiguration(conf: SamplingConfiguration) {
        write(SAMPLING_CONFIGURATION_ADDR,conf.pack())
    }

    private fun resetTempHumMinMax() {
        //invert min/max to be secure to log the fist sample
        val defaultMinMax = MaxMinTempHumCell( tempMax = TEMPERATURE_RANGE_C.start,
                tempMin = TEMPERATURE_RANGE_C.endInclusive,
                humMax = HUMIDITY_RANGE.start,
                humMin = HUMIDITY_RANGE.endInclusive)
        write(MAXMIN_TEMP_HUM_ADDR,
                defaultMinMax.pack())
        writeZero(MAX_TEMP_EXTREME_TIMESTAMP)
        writeZero(MIN_TEMP_EXTREME_TIMESTAMP)
        writeZero(MAX_HUM_EXTREME_TIMESTAMP)
        writeZero(MIN_HUM_EXTREME_TIMESTAMP)

    }

    private fun resetPresAccMinMax() {
        val defaultMinMax = MaxMinPresAccCell(maxPressure = PRESSURE_RANGE_MBAR.start,
                minPressure = PRESSURE_RANGE_MBAR.endInclusive,
                accelerationValue = ACCELERATION_RANGE_MG.start)

        write(MAXMIN_PRES_ACC_EXTREME_ADDR,
                defaultMinMax.pack())
        writeZero(MIN_PRES_EXTREME_TIMESTAMP)
        writeZero(MAX_PRES_EXTREME_TIMESTAMP)
        writeZero(MAX_ACC_EXTREME_TIMESTAMP)
    }

    private fun setDateAndTime() {
        write(TIMESTAMP_CONFIGURATION_ADDR, GregorianCalendar().pack())
    }

    private fun setNewConfigurationAvailable() {
        val tagStatus = TagStatusCell(newConfigurationAvailable = true)
        write(TAG_STATUS_ADDR,tagStatus.pack())
    }


    fun writeTagConfiguration(conf: SamplingConfiguration) {
        tag.connect()

        setDateAndTime()
        writeLogConfiguration(conf)
        writeSamplingThreshold(conf)
        if(conf.mode!=SamplingConfiguration.Mode.SaveNextSample) {
            initializeFirstSamplePosition()
            resetTempHumMinMax()
            resetPresAccMinMax()
        }
        setNewConfigurationAvailable()
        tag.close()
    }

    private fun writeSamplingThreshold(conf: SamplingConfiguration) {
        val tempTh = conf.temperatureConf.threshold
        val humTh = conf.humidityConf.threshold
        writeTempHumidityThreshold(tempTh,humTh)

        val presTh = conf.pressureConf.threshold
        val accTh = conf.accelerometerConf.threshold
        writePresAndAccThreshold(presTh,accTh)
    }

    private fun initializeFirstSamplePosition() {
        write(SAMPLE_POSITION_INFO_ADDR,
               SamplePositionCell(memoryLayout.firstDataSamplePtr, 0).pack())
    }

    private fun readDataFromAddress(address: Short): Date {
        val byteDate = read(address)
        return unpackGregorianCalendar(byteDate).time
    }

    private fun readVibrationExtremeData(): DataExtreme {

        val rawData = read(MAXMIN_PRES_ACC_EXTREME_ADDR)
        val accData = unpackMaxMinPresAccCell(rawData)

        val maxAccDate = readDataFromAddress(MAX_ACC_EXTREME_TIMESTAMP)
        return DataExtreme(Date(0), Float.NaN, maxAccDate, accData.accelerationValue)
    }

    private fun readTemperatureExtremeData(): DataExtreme {

        val data = read(MAXMIN_TEMP_HUM_ADDR)
        val minMaxData = unpackMaxMinTempHumCell(data)
        val minTempDate = readDataFromAddress(MIN_TEMP_EXTREME_TIMESTAMP)
        val maxTempDate = readDataFromAddress(MAX_TEMP_EXTREME_TIMESTAMP)
        return DataExtreme(minTempDate, minMaxData.tempMin, maxTempDate, minMaxData.tempMax)
    }


    private fun readHumidityExtremeData(): DataExtreme {

        val data = read(MAXMIN_TEMP_HUM_ADDR)
        val humData = unpackMaxMinTempHumCell(data)
        val minHumDate = readDataFromAddress(MIN_HUM_EXTREME_TIMESTAMP)
        val maxHumDate = readDataFromAddress(MAX_HUM_EXTREME_TIMESTAMP)

        return DataExtreme(minHumDate, humData.humMin, maxHumDate, humData.humMax)
    }


    private fun readPressureExtremeData(): DataExtreme {

        val rawData = read(MAXMIN_PRES_ACC_EXTREME_ADDR)

        val pressData = unpackMaxMinPresAccCell(rawData)

        val minPresDate = readDataFromAddress(MIN_PRES_EXTREME_TIMESTAMP)
        val maxPresDate = readDataFromAddress(MAX_PRES_EXTREME_TIMESTAMP)

        return DataExtreme(minPresDate, pressData.minPressure ,
                maxPresDate, pressData.maxPressure)
    }

    private fun readOneShotData(): SensorDataSample {
        val temperature = readTemperatureExtremeData()
        val pressure = readPressureExtremeData()
        val humidity = readHumidityExtremeData()
        val acc = readVibrationExtremeData()
        return SensorDataSample(Date(),
                temperature.minValue,pressure.minValue,humidity.minValue,acc.maxValue)
    }

    private fun singleShotDataAreReady(): Boolean {
        val response = read(TAG_STATUS_ADDR)
        val status = unpackTagStatus(response)
        //Log.d("SmartTag"+this,"response: "+status)
        //val sensorDataSample = readOneShotData(tag)
        //Log.d("SmartTag","Data: "+sensorDataSample)
        return status.singleShotResponseReady
    }

    fun readSingleShotData(beforeWait: ((Long)->Unit)?=null) : SensorDataSample {
        tag.connect()
        // no need to write the configuration since the single shot will automatically start when
        // the board wake up
        //writeSingleShotConfiguration()
        do {
            beforeWait?.invoke(WAIT_SINGLE_SHOT_ANSWER_MS)
            Thread.sleep(WAIT_SINGLE_SHOT_ANSWER_MS)
        }while (!singleShotDataAreReady())
        val dataSample = readOneShotData()
        tag.close()
        return  dataSample
    }


    fun readTagConfiguration(): SamplingConfiguration = readTagConfiguration(true)

    private fun readTagConfiguration(openConnection:Boolean): SamplingConfiguration{
        if(openConnection)
            tag.connect()
        val data = read(SAMPLING_CONFIGURATION_ADDR)
        val tempHumTh = readTempAndHumidityThreshold()
        val presAccTh = readPresAndAccThreshold()
        val conf = unpackSamplingConfiguration(data, tempHumTh, presAccTh)
        if(openConnection)
            tag.close()
        return conf
    }

    private fun readAcquisitionStart(): Date? {
        return readDataFromAddress(TIMESTAMP_CONFIGURATION_ADDR)
    }

    fun readDataExtremes(): TagExtreme? {

        tag.connect()
        val acquisitionStarted = readAcquisitionStart() ?: return null
        val conf = readTagConfiguration(false)
        var temperature: DataExtreme? = null
        if (conf.temperatureConf.isEnable) {
            temperature = readTemperatureExtremeData()
        }

        var humidity: DataExtreme? = null
        if (conf.humidityConf.isEnable) {
            humidity = readHumidityExtremeData()
        }

        var pressure: DataExtreme? = null
        if (conf.pressureConf.isEnable) {
            pressure = readPressureExtremeData()
        }

        var vibration: DataExtreme? = null
        if (conf.accelerometerConf.isEnable) {
            vibration = readVibrationExtremeData()
        }

        tag.close()

        return TagExtreme(
                acquisitionStarted,
                temperature,
                pressure,
                humidity, vibration)
    }

    private fun isAsyncEventSample(rawDataValue:ByteArray): Boolean {
        return (rawDataValue[3] and 0x80.toByte()) != 0.toByte()
    }


    private fun readDataSample( index: Int, conf: SamplingConfiguration): DataSample {
        val rawDataValue = read((FIRST_SAMPLE_POSITION + 2 * index).toShort())

        val data = unpackGregorianCalendar(rawDataValue).time

        val dataAddress = (FIRST_SAMPLE_POSITION + 2 * index + 1).toShort()
        val rawSampleValue = read(dataAddress)
        if(isAsyncEventSample(rawDataValue)){
            val eventSample = unpackEventDataSampleValue(rawSampleValue)
            return EventDataSample(data,
                    acceleration = eventSample.vibration,
                    currentOrientation = Orientation.valueOf(eventSample.orientation),
                    events = AccelerationEvent.extractEvent(eventSample.event))
        }else {
            val sampleValues = unpackSensorDataSampleValue(read(dataAddress))
            return SensorDataSample(
                    data,
                    temperature = if (conf.temperatureConf.isEnable) sampleValues.temperature else null,
                    pressure = if (conf.pressureConf.isEnable) sampleValues.pressure else null,
                    humidity = if (conf.humidityConf.isEnable) sampleValues.humidity else null,
                    acceleration = if (conf.accelerometerConf.isEnable) sampleValues.acceleration else null
            )
        }
    }



  private fun readSampleInfo(): SamplePositionCell {
        val response = read(SAMPLE_POSITION_INFO_ADDR)
        return unpackSamplePosition(response)
    }

    fun readDataSample(onReadNumberOfSample:(Int?)->Unit,onReadSample:(DataSample)->Unit) {

        tag.connect()
        val conf = readTagConfiguration(false)
        val sampleInfo = readSampleInfo()
        onReadNumberOfSample(sampleInfo.sampleCounter)
        // each sample has size 2 memory cell -> /2 go from memory address to sample index
        // -1 since we want the last sample and we have the next sample 
        val lastSampleIndex = (sampleInfo.nextSamplePtr - memoryLayout.firstDataSamplePtr)/2 -1
        var firstSampleIndex = if (sampleInfo.sampleCounter >= memoryLayout.maxSample) {
            lastSampleIndex + 1
        } else {
            0
        }
        for (i in 0 until sampleInfo.sampleCounter) {
            val sample = readDataSample(firstSampleIndex, conf)
            onReadSample(sample)
            firstSampleIndex = (firstSampleIndex +1) % memoryLayout.maxSample
        }
        tag.close()
    }

    fun readFwVersion(): Version {
        tag.connect()
        val rawData = read(FW_VERSION_ADD)
        tag.close()
        return unpackVersion(rawData)
    }


    private fun writePresAndAccThreshold(pressure:Threshold,acceleration:Threshold){
        val rawData = MaxMinPresAccCell(pressure.min ?: PRESSURE_RANGE_MBAR.start,
                pressure.max ?: PRESSURE_RANGE_MBAR.endInclusive,
                 acceleration.max ?: ACCELERATION_RANGE_MG.start).pack()
        write(PRESS_ACC_TH_CONFIGURATION_ADDR,rawData)
    }

    private fun readPresAndAccThreshold(): MaxMinPresAccCell {
        val data = read(PRESS_ACC_TH_CONFIGURATION_ADDR)
        return unpackMaxMinPresAccCell(data)
    }

    private fun readTempAndHumidityThreshold():MaxMinTempHumCell{
        val data = read(TEMP_HUM_TH_CONFIGURATION_ADDR)
        return unpackMaxMinTempHumCell(data)
    }

    private fun writeTempHumidityThreshold(temperature:Threshold,humidity:Threshold){
        val rawData = MaxMinTempHumCell( temperature.max ?: TEMPERATURE_RANGE_C.start,
                temperature.min ?: TEMPERATURE_RANGE_C.endInclusive,
                humidity.max ?: HUMIDITY_RANGE.start,
                humidity.min ?: HUMIDITY_RANGE.endInclusive).pack()
        write(TEMP_HUM_TH_CONFIGURATION_ADDR,rawData)
    }


    companion object {

        data class Version(val major:Byte,val minor:Byte,val patch:Byte)

        val TEMPERATURE_RANGE_C = -40.0f..85.0f
        val HUMIDITY_RANGE = 0.0f..100.0f
        val PRESSURE_RANGE_MBAR = 810.0f..1210.0f
        val ACCELERATION_RANGE_MG = 0.0f..16000.0f
        val VALID_SAMPLING_RATE_INTERVAL = 1..0xFFFF

        private const val EXTENDED_CC_LENGTH = 0x00.toByte()

        private const val WAIT_SINGLE_SHOT_ANSWER_MS = 7000L

        private const val FW_VERSION_ADD = 0x00.toShort()
        private const val SAMPLING_CONFIGURATION_ADDR = 0x01.toShort()
        private const val TIMESTAMP_CONFIGURATION_ADDR = 0x02.toShort()
        private const val TEMP_HUM_TH_CONFIGURATION_ADDR = 0x03.toShort()
        private const val PRESS_ACC_TH_CONFIGURATION_ADDR = 0x04.toShort()
        private const val TAG_STATUS_ADDR = 0x05.toShort()
        private const val MAX_TEMP_EXTREME_TIMESTAMP = 0x06.toShort()
        private const val MIN_TEMP_EXTREME_TIMESTAMP = 0x07.toShort()

        private const val MAX_HUM_EXTREME_TIMESTAMP = 0x08.toShort()
        private const val MIN_HUM_EXTREME_TIMESTAMP = 0x09.toShort()
        private const val MAXMIN_TEMP_HUM_ADDR = 0x0A.toShort()

        private const val MAX_PRES_EXTREME_TIMESTAMP = 0x0B.toShort()
        private const val MIN_PRES_EXTREME_TIMESTAMP = 0x0C.toShort()
        private const val MAX_ACC_EXTREME_TIMESTAMP =  0x0D.toShort()
        private const val MAXMIN_PRES_ACC_EXTREME_ADDR = 0x0E.toShort()

        private const val SAMPLE_POSITION_INFO_ADDR = 0x0F.toShort()

        private const val FIRST_SAMPLE_POSITION: Short = 0x0010.toShort()

        private const val NFCTAG_4K_SIZE = 0x80.toShort()
        private const val NFCTAG_64K_SIZE = 0x800.toShort()

        private const val NDEF_EXTERNAL_TYPE = 0x04.toByte()
        private const val NDEF_SMARTAG_TYPE = "st.com:smartag"

    }

    private data class SmarTagMemoryLayout(
            val totalSize:Short,
            val ndefHeaderSize: Short) {

        val firstDataSamplePtr: Short = (FIRST_SAMPLE_POSITION +ndefHeaderSize).toShort()
        //totalSize -1 since the last cell is used for the TLV
        val lastDataSample:Short = totalSize.dec()
        val maxSample:Short

        init {
            //2 = 1 (time stamp)+ 1 (sensorDataSample)
            maxSample = ((lastDataSample- firstDataSamplePtr)/2).toShort()
        }
    }


}

