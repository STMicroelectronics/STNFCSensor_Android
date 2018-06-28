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

package com.st.smartTag

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.support.v4.content.FileProvider
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.st.smartTag.model.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Service to store the data into a csv file
 */
class ExportDataService : IntentService("ExportDataService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_STORE_CSV_LOG == action) {
                val conf = intent.getParcelableExtra<SamplingConfiguration>(EXTRA_CONF)
                val extreme = intent.getParcelableExtra<TagExtreme>(EXTRA_EXTREME)
                val sensorSamples = intent.getParcelableArrayListExtra<SensorDataSample>(EXTRA_SAMPLES)
                val eventSamples = intent.getParcelableArrayListExtra<EventDataSample>(EXTRA_EVENTS)
                handleStoreCSVLog(conf, extreme, sensorSamples,eventSamples)
            }
        }
    }

    /**
     * Create the directory and the file where store the logs
     * The file will be created into the external storage memory
     * @return file where store the logs
     */
    private fun createLotFile(): File {
        val directory = Environment.getExternalStorageDirectory()
        val fileName = String.format(FILE_NAME_FORMAT, FILE_DATA_FORMAT.format(Date()))
        val file = File(directory, fileName)
        file.parentFile.mkdirs() //create the missing directory
        file.createNewFile() // create the file
        return file
    }

    /**
     * print into [out] the configuration [conf]
     */
    private fun writeTagConfiguration(out: Formatter, conf: SamplingConfiguration) {
        val enableFormat = "%s Enabled:, %s\n"
        out.format("Sampling Interval,%d,seconds\n", conf.samplingInterval_s)
        out.format(enableFormat,getString(R.string.data_temperature_name), conf.temperatureConf.isEnable.yesOrNo())
        out.format(enableFormat,getString(R.string.data_humidity_name), conf.humidityConf.isEnable.yesOrNo())
        out.format(enableFormat,getString(R.string.data_pressure_name), conf.pressureConf.isEnable.yesOrNo())
        out.format(enableFormat,getString(R.string.data_acceleration_name), conf.accelerometerConf.isEnable.yesOrNo())
        if(conf.accelerometerConf.isEnable){
            out.format(enableFormat,getString(R.string.data_orientation_name), conf.orientationConf.isEnable.yesOrNo())
            out.format(enableFormat,getString(R.string.data_wakeUp_name), conf.wakeUpConf.isEnable.yesOrNo())
        }
        if (conf.mode == SamplingConfiguration.Mode.SamplingWithThreshold)
            writeTagConfigurationThreshold(out, conf)
        out.format("\n\n")
    }

    /**
     * print into [out] the threshold used for the logging
     */
    private fun writeTagConfigurationThreshold(out: Formatter, conf: SamplingConfiguration) {
        out.format("Threshold\n,Min,Max\n")
        if (conf.temperatureConf.isEnable)
            printThreshold(out, getString(R.string.data_temperature_name), conf.temperatureConf.threshold)
        if (conf.humidityConf.isEnable)
            printThreshold(out, getString(R.string.data_humidity_name), conf.humidityConf.threshold)
        if (conf.pressureConf.isEnable)
            printThreshold(out, getString(R.string.data_pressure_name), conf.pressureConf.threshold)
        if (conf.accelerometerConf.isEnable)
            printThreshold(out, getString(R.string.data_acceleration_name), conf.accelerometerConf.threshold)
        if(conf.orientationConf.isEnable)
            printThreshold(out, getString(R.string.data_orientation_name), conf.orientationConf.threshold)
        if(conf.wakeUpConf.isEnable)
            printThreshold(out, getString(R.string.data_wakeUp_name), conf.wakeUpConf.threshold)
    }

    /**
     * print into [out] the threshold with the format: [name],[th].min,[th].max
     */
    private fun printThreshold(out:Formatter, name:String , th:Threshold){
        out.format(NUMBER_LOCALE,"%s,%.1f,%.1f\n",name,th.min.valueOrNan(),th.max.valueOrNan())
    }

    /**
     * print into [out] the min and max value from the [sample] data.
     * [name] say what the [sample] represent, and [unit] is the unit of the [sample] data
     */
    private fun printExtreme(out: Formatter, name: String, unit: String, sample: DataExtreme) {
        out.format(NUMBER_LOCALE,"Maximum %s:,%f,%s,%s\n", name, sample.maxValue, unit,
                CVS_DATA_FORMAT.format(sample.maxDate))
        out.format(NUMBER_LOCALE,"Minimum %s:,%f,%s,%s\n", name, sample.minValue, unit,
                CVS_DATA_FORMAT.format(sample.minDate))
    }


    /**
     * print into [out] all the data extreme detected by the tag
     */
    private fun writeTagExtremeData(out: Formatter, extreme: TagExtreme) {
        out.format("Extreme measurements\n")
        out.format("Acquisition start:,%s\n", CVS_DATA_FORMAT.format(extreme.acquisitionStart))
        out.format(",Value,Unit,Date\n")
        extreme.temperature?.let {
            printExtreme(out, getString(R.string.data_temperature_name), getString(R.string.data_temperature_unit), it)
        }
        extreme.humidity?.let {
            printExtreme(out, getString(R.string.data_humidity_name), getString(R.string.data_humidity_unit), it)
        }
        extreme.pressure?.let {
            printExtreme(out, getString(R.string.data_pressure_name), getString(R.string.data_pressure_unit), it)
        }
        extreme.vibration?.let {
            out.format(NUMBER_LOCALE,"Maximum Acceleration:,%f,%s,%s\n",
                    it.maxValue, getString(R.string.data_acceleration_unit),
                    CVS_DATA_FORMAT.format(it.maxDate))
        }
        out.format("\n\n")
    }

    /**
     * create and store the data into a csv file
     * [conf] tag configuration to store in the file
     * [extreme] extreme data to store in the file
     * [sensorSample] list of sensor sample to store in the file
     */
    private fun handleStoreCSVLog(conf: SamplingConfiguration?, extreme: TagExtreme?,
                                  sensorSample: ArrayList<SensorDataSample>?,
                                  eventSamples: ArrayList<EventDataSample>?) {
        try {

            val file = createLotFile()
            val outFormatter = Formatter(file)
            if (conf != null)
                writeTagConfiguration(outFormatter, conf)
            if (extreme != null)
                writeTagExtremeData(outFormatter, extreme)
            if (sensorSample != null && sensorSample.isNotEmpty()) {
                writeTagSamples(outFormatter, sensorSample)
            }
            if (eventSamples != null && eventSamples.isNotEmpty()) {
                writeTagEvents(outFormatter, eventSamples)
            }
            outFormatter.close()
            notifyExportSuccess(getUriFromFile(file))
        } catch (e: IOException) {
            Log.e("Export", e.localizedMessage)
            notifyExportError(e.localizedMessage)
        }
    }

    private fun getUriFromFile(file:File):Uri{
        val fileProviderPackage = applicationContext.packageName
        return FileProvider.getUriForFile(this, fileProviderPackage,file)
    }

    /**
     * print the async events [eventSamples] into [out]
     */
    private fun writeTagEvents(out: Formatter, eventSamples: ArrayList<EventDataSample>) {
        out.format("Events\n")
        out.format("Date,Event,%s, %s (%s)\n",getString(R.string.data_orientation_name),
                getString(R.string.data_acceleration_name),getString(R.string.data_acceleration_unit))
        eventSamples.forEach {
            out.format(NUMBER_LOCALE,"%s,%s,%s,%f\n", CVS_DATA_FORMAT.format(it.date),
                    it.events.fold(""){ str,event -> str + " "+ event.name },
                    it.currentOrientation.name,
                    it.acceleration.valueOrNan());
        }
        out.format("\n\n")
    }

    /**
     * store the [sensorSample] list into the [out] file
     */
    private fun writeTagSamples(out: Formatter, sensorSample: List<SensorDataSample>) {
        out.format("Data Log\n")
        out.format("Date, %s (%s), %s (%s), %s (%s), %s (%s)\n",
                getString(R.string.data_pressure_name),getString(R.string.data_pressure_unit),
                getString(R.string.data_humidity_name),getString(R.string.data_humidity_unit),
                getString(R.string.data_temperature_name),getString(R.string.data_temperature_unit),
                getString(R.string.data_acceleration_name),getString(R.string.data_acceleration_unit))
        sensorSample.forEach {
            out.format(NUMBER_LOCALE,"%s,%f,%f,%f,%f\n", CVS_DATA_FORMAT.format(it.date),
                    it.pressure.valueOrNan(), it.humidity.valueOrNan(),
                    it.temperature.valueOrNan(), it.acceleration.valueOrNan())
        }
        out.format("\n\n")
    }


    /**
     * send a local broadcast with the action: [ACTION_EXPORT_ERROR] to notify that the export
     * fails with an error that can be found in the [EXTRA_EXPORT_ERROR] string
     */
    private fun notifyExportError(error: String) {
        val intent = Intent(ACTION_EXPORT_ERROR)
        intent.putExtra(EXTRA_EXPORT_ERROR, error)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * send a local broadcast with the action [ACTION_EXPORT_ERROR] to notify that the data are
     * correctly exported
     * the file uri is stored inside the [EXTRA_EXPORTED_FILE] extra
     */
    private fun notifyExportSuccess(file: Uri) {
        val intent = Intent(ACTION_EXPORT_SUCCESS)
        intent.putExtra(EXTRA_EXPORTED_FILE, file)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    companion object {

        // date to use as file name
        private val FILE_DATA_FORMAT = SimpleDateFormat("dd-MMM-yy_HHmmss", Locale.getDefault())
        // date format to use inside the csv file
        private val CVS_DATA_FORMAT = SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault())
        //path and file extension to use to store the cvs file
        private val FILE_NAME_FORMAT = "/STMicroelectronics/STNFCSensor/%s.csv"

        //use us locale to be secure to have . as decimal separator
        private val NUMBER_LOCALE = Locale.US

        private val ACTION_STORE_CSV_LOG = ExportDataService::class.java.canonicalName + ".ACTION_STORE_CSV_LOG"
        private val EXTRA_CONF = ExportDataService::class.java.canonicalName + ".EXTRA_CONF"
        private val EXTRA_EXTREME = ExportDataService::class.java.canonicalName + ".EXTRA_EXTREME"
        private val EXTRA_SAMPLES = ExportDataService::class.java.canonicalName + ".EXTRA_SAMPLES"
        private val EXTRA_EVENTS = ExportDataService::class.java.canonicalName + ".EXTRA_EVENTS"

        val ACTION_EXPORT_ERROR = ExportDataService::class.java.canonicalName + ".ACTION_EXPORT_ERROR"
        val EXTRA_EXPORT_ERROR = ExportDataService::class.java.canonicalName + ".EXTRA_EXPORT_ERROR"

        val ACTION_EXPORT_SUCCESS = ExportDataService::class.java.canonicalName + ".ACTION_EXPORT_COMPLETE"
        val EXTRA_EXPORTED_FILE = ExportDataService::class.java.canonicalName + ".EXTRA_EXPORTED_FILE"

        /**
         * return an intent filter to caputre all the broadcast message send by this service
         */
        fun getExportDataResponseFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction(ACTION_EXPORT_ERROR)
                addAction(ACTION_EXPORT_SUCCESS)
            }
        }

        private fun addDataSample(intent:Intent, dataSample: List<DataSample>?){
            if(dataSample==null)
                return

            val sensorSample = dataSample.filterIsInstance<SensorDataSample>()
            val eventSamples = dataSample.filterIsInstance<EventDataSample>()

            if (sensorSample.isNotEmpty())
                intent.putExtra(EXTRA_SAMPLES, ArrayList(sensorSample))
            if (eventSamples.isNotEmpty())
                intent.putExtra(EXTRA_EVENTS, ArrayList(eventSamples))

        }

        /**
         * start the service to store the data into a csv file
         * [conf] tag configuration
         * [extreme] sensor extreme data
         * [sensorSample] list of sample to write
         * [eventSamples] list of async events to write
         */
        fun startExportCSVData(context: Context, conf: SamplingConfiguration?, extreme: TagExtreme?, dataSample: List<DataSample>?) {
            val intent = Intent(context, ExportDataService::class.java)
            intent.action = ACTION_STORE_CSV_LOG
            if (conf != null)
                intent.putExtra(EXTRA_CONF, conf)
            if (extreme != null)
                intent.putExtra(EXTRA_EXTREME, extreme)
            addDataSample(intent,dataSample)
            context.startService(intent)
        }

        /**
         * convert a boolean to the string yes (true) no (false)
         */
        private fun Boolean.yesOrNo(): String = if (this) "Yes"  else  "No"

        /**
         * if the value is null return [Float.NaN] otherwise return the variable value
         */
        private fun Float?.valueOrNan(): Float = this ?: Float.NaN
        private fun Int?.valueOrNan(): Float = if(this!=null) this.toFloat() else Float.NaN
    }
}