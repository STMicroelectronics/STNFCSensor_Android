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
import android.nfc.Tag
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.st.smartTag.model.DataSample
import com.st.smartTag.model.SamplingConfiguration
import com.st.smartTag.model.TagExtreme
import com.st.smartTag.nfc.SmarTag
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Service used to communicate the with the nfc tag
 */
class SmartTagService : IntentService("SmartTagService") {

    /* broadcast manager used to notify the service results*/
    private lateinit var broadcast: LocalBroadcastManager

    override fun onCreate() {
        super.onCreate()
        broadcast = LocalBroadcastManager.getInstance(this)
    }

    /**
     * notify that the the service start to tread the tag
     */
    private fun notifyReadStart() {
        broadcast.sendBroadcast(Intent(READ_TAG_START_ACTION))
    }

    /**
     * notify that the read fails with the message: [msg]
     */
    private fun notifyReadError(msg: String) {
        val intent = Intent(READ_TAG_ERROR_ACTION)
        intent.putExtra(EXTRA_ERROR_STR, msg)
        broadcast.sendBroadcast(intent)
    }

    /**
     * notify the configuration [conf] read from the tag
     */
    private fun notifyReadConfiguration(conf: SamplingConfiguration) {
        val intent = Intent(READ_TAG_CONFIGURATION_ACTION)
        intent.putExtra(EXTRA_TAG_CONFIGURATION, conf)
        broadcast.sendBroadcast(intent)
    }

    /**
     * notify that the service is starting to write the tag
     */
    private fun notifyWriteStart() {
        broadcast.sendBroadcast(Intent(WRITE_TAG_START_ACTION))
    }

    /**
     * the write fails with the message: [msg]
     */
    private fun notifyWriteError(msg: String) {
        val intent = Intent(WRITE_TAG_ERROR_ACTION)
        intent.putExtra(EXTRA_ERROR_STR, msg)
        broadcast.sendBroadcast(intent)
    }

    /**
     * the write complete with success
     */
    private fun notifyWriteSuccess() {
        val intent = Intent(WRITE_TAG_COMPLETE_ACTION)
        broadcast.sendBroadcast(intent)
    }

    /**
     * notify that a data sample [sample] is read from the tag
     */
    private fun notifyReadDataSample(sample: DataSample) {
        val intent = Intent(READ_TAG_SAMPLE_DATA_ACTION)
        intent.putExtra(EXTRA_TAG_SAMPLE_DATA, sample)
        broadcast.sendBroadcast(intent)
    }

    /**
     * notify that the service will read [numSample] data sample
     */
    private fun notifyNumberSample(numSample: Int) {
        val intent = Intent(READ_TAG_NUMBER_SAMPLE_DATA_ACTION)
        intent.putExtra(EXTRA_TAG_NUMBER_SAMPLE, numSample)
        broadcast.sendBroadcast(intent)
    }

    /**
     * notify that the extreme [data] are read from the tag
     */
    private fun notifyReadDataExtreme(data: TagExtreme) {
        val intent = Intent(READ_TAG_EXTREME_DATA_ACTION)
        intent.putExtra(EXTRA_TAG_EXTREME_DATA, data)
        broadcast.sendBroadcast(intent)
    }

    /**
     * notify that the service is waiting [waitingTime] ms to enable the harvesting mode in the tag
     */
    private fun notifyWaitingCharge(waitingTime:Long) {
        val intent = Intent(READ_TAG_WAIT_ANSWER_ACTION)
        intent.putExtra(EXTRA_WAIT_ANSWER_TIMEOUT_MS_DATA,waitingTime)
        broadcast.sendBroadcast(intent)
    }

    /**
     * notify that the tag is not yet ready to read the data using the harvesting mode
     */
    private fun notifySingleShotNotReady() {
        val intent = Intent(SINGLE_SHOT_DATA_NOT_READY_ACTION)
        broadcast.sendBroadcast(intent)
    }

    /**
     * extract the action parameter and dispatch to the correct function
     */
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val tag = intent.getParcelableExtra<Tag>(EXTRA_SMART_TAG) ?: return
            val smarTag = SmarTag.get(tag) ?: return
            when (intent.action) {
                READ_TAG_CONFIGURATION -> {
                    handleReadConfigurationRequest(smarTag)
                }
                WRITE_TAG_CONFIGURATION -> {
                    val conf = intent.getParcelableExtra<SamplingConfiguration>(EXTRA_TAG_CONFIGURATION)
                    handleWriteConfRequest(smarTag, conf)
                }
                READ_TAG_DATA_EXTREME -> {
                    handleReadDataExtreme(smarTag)
                }
                READ_TAG_DATA_SAMPLE -> {
                    handleReadDataSample(smarTag)
                }
                READ_SINGLE_SHOT_DATA -> {
                    handleReadSingleShot(smarTag)
                }
            }
        }
    }

    private fun handleReadSingleShot(tag: SmarTag) {
        try {
            var nWait = 0
            val dataSample = tag.readSingleShotData {
                if (nWait != 0) {
                    notifySingleShotNotReady()
                }
                nWait++
                notifyWaitingCharge(it)
            }
            notifyReadDataSample(dataSample)
        } catch (e: IOException) {
            Log.d("SmartTag" + this, e.message ?: "no message")
            notifyReadError(e.localizedMessage ?: getString(R.string.smartTag_genericIOError))
        } finally {
            isSingleShotRequestOngoing.set(false)
        }//Try-catch
    }// handleReadSingleShot

    private fun handleReadDataSample(tag: SmarTag) {
        try {
            tag.readDataSample(
                    onReadNumberOfSample ={numberOfSample ->
                        if(numberOfSample!=null)
                            notifyNumberSample(numberOfSample)
                        else
                            notifyReadError(getString(R.string.smartTag_invalidSampleNumber))
                    },
                    onReadSample = { notifyReadDataSample(it) }
            )
        } catch (e: IOException) {
            notifyReadError(e.localizedMessage ?: getString(R.string.smartTag_genericIOError))
        }//try catch
    }//handleReadDataSample

    private fun handleReadDataExtreme(tag: SmarTag) {
        try {
            val data = tag.readDataExtremes()
            if (data != null)
                notifyReadDataExtreme(data)
            else {
                notifyReadError(getString(R.string.smartTag_invalidExtreme))
            }//if else
        } catch (e: IOException) {
            notifyReadError(e.localizedMessage ?: getString(R.string.smartTag_genericIOError))
        } //try catch
    }//handleReadDataExtreme

    private fun handleWriteConfRequest(tag: SmarTag, conf: SamplingConfiguration) {
        notifyWriteStart()
        try {
            tag.writeTagConfiguration(conf)
            notifyWriteSuccess()
        } catch (e: IOException) {
            notifyWriteError(e.localizedMessage ?: getString(R.string.smartTag_genericIOError))
        }//try catch
    }//handleWriteConfRequest

    private fun handleReadConfigurationRequest(smarTag: SmarTag) {
        notifyReadStart()
        try {
            val conf = smarTag.readTagConfiguration()
            notifyReadConfiguration(conf)
        } catch (e: IOException) {
            notifyReadError(e.localizedMessage ?: getString(R.string.smartTag_genericIOError))
        } //try catch
    }//handleReadConfigurationRequest

    companion object {

        val READ_TAG_START_ACTION = SmartTagService::class.java.canonicalName + ".READ_TAG_START_ACTION"

        val READ_TAG_CONFIGURATION_ACTION = SmartTagService::class.java.canonicalName + ".READ_CONFIG_ACTION"
        val EXTRA_TAG_CONFIGURATION = SmartTagService::class.java.canonicalName + ".EXTRA_TAG_CONFIGURATION"

        val READ_TAG_EXTREME_DATA_ACTION = SmartTagService::class.java.canonicalName + ".READ_TAG_EXTREME_DATA_ACTION"
        val EXTRA_TAG_EXTREME_DATA = SmartTagService::class.java.canonicalName + ".EXTRA_TAG_EXTREME_DATA"

        val READ_TAG_NUMBER_SAMPLE_DATA_ACTION = SmartTagService::class.java.canonicalName + ".READ_TAG_NUMBER_SAMPLE_DATA_ACTION"
        val EXTRA_TAG_NUMBER_SAMPLE = SmartTagService::class.java.canonicalName + ".EXTRA_TAG_NUMBER_SAMPLE"
        val SINGLE_SHOT_DATA_NOT_READY_ACTION = SmartTagService::class.java.canonicalName + ".SINGLE_SHOT_DATA_NOT_READY_ACTION"
        val READ_TAG_SAMPLE_DATA_ACTION = SmartTagService::class.java.canonicalName + ".READ_TAG_SAMPLE_DATA_ACTION"
        val EXTRA_TAG_SAMPLE_DATA = SmartTagService::class.java.canonicalName + ".EXTRA_TAG_SAMPLE_DATA"

        val READ_TAG_WAIT_ANSWER_ACTION = SmartTagService::class.java.canonicalName + ".READ_TAG_WAIT_ANSWER_ACTION"
        val EXTRA_WAIT_ANSWER_TIMEOUT_MS_DATA = SmartTagService::class.java.canonicalName + ".EXTRA_WAIT_ANSWER_TIMEOUT_MS_DATA"

        val READ_TAG_ERROR_ACTION = SmartTagService::class.java.canonicalName + ".READ_TAG_ERROR_ACTION"

        val WRITE_TAG_START_ACTION = SmartTagService::class.java.canonicalName + ".WRITE_TAG_START_ACTION"
        val WRITE_TAG_ERROR_ACTION = SmartTagService::class.java.canonicalName + ".WRITE_TAG_ERROR_ACTION"
        val WRITE_TAG_COMPLETE_ACTION = SmartTagService::class.java.canonicalName + ".WRITE_TAG_COMPLETE_ACTION"

        private val READ_TAG_CONFIGURATION = SmartTagService::class.java.canonicalName + ".READ_CONFIG"

        private val WRITE_TAG_CONFIGURATION = SmartTagService::class.java.canonicalName + ".WRITE_CONFIG"
        private val READ_TAG_DATA_EXTREME = SmartTagService::class.java.canonicalName + ".READ_TAG_DATA_EXTREME"
        private val READ_TAG_DATA_SAMPLE = SmartTagService::class.java.canonicalName + ".READ_TAG_DATA_SAMPLE"
        private val READ_SINGLE_SHOT_DATA = SmartTagService::class.java.canonicalName + ".READ_SINGLE_SHOT_DATA"

        val EXTRA_ERROR_STR = SmartTagService::class.java.canonicalName + ".EXTRA_ERROR_STR"
        private val EXTRA_SMART_TAG = SmartTagService::class.java.canonicalName + ".EXTRA_SMART_TAG"

        /**
         * get the intent filter to detect all the action fired to read and write the tag configuration:
         * - READ_TAG_START_ACTION when the service start reading the configuration
         * - READ_TAG_CONFIGURATION_ACTION when the configuration is read
         *      (the configuration is present in the EXTRA_TAG_CONFIGURATION extra)
         * - READ_TAG_ERROR_ACTION if the read operation fails, the error message is in EXTRA_ERROR_STR extra
         *
         * - WRITE_TAG_START_ACTION when the service start writing the configuration
         * - WRITE_TAG_ERROR_ACTION if the write operation fails, the error message is in EXTRA_ERROR_STR extra
         * - WRITE_TAG_COMPLETE_ACTION when the write operation complete without error
         */
        fun getReadWriteConfigurationFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction(READ_TAG_START_ACTION)
                addAction(READ_TAG_CONFIGURATION_ACTION)
                addAction(READ_TAG_ERROR_ACTION)

                addAction(WRITE_TAG_START_ACTION)
                addAction(WRITE_TAG_ERROR_ACTION)
                addAction(WRITE_TAG_COMPLETE_ACTION)
            }
        }

        /**
         * get the intent filter to detect all the action fired when reading the extreme data
         * - READ_TAG_START_ACTION when the service start reading the data
         * - READ_TAG_EXTREME_DATA_ACTION when the configuration is read
         *      (the configuration is present in the EXTRA_TAG_EXTREME_DATA extra)
         * - READ_TAG_ERROR_ACTION if the read operation fails, the error message is in EXTRA_ERROR_STR extra
         */
        fun getReadDataExtremeFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction(READ_TAG_START_ACTION)
                addAction(READ_TAG_EXTREME_DATA_ACTION)
                addAction(READ_TAG_ERROR_ACTION)
            }
        }


        /**
         * get the intent filter to detect all the action fired when reading the data samples
         * - READ_TAG_START_ACTION when the service start reading the data
         * - READ_TAG_NUMBER_SAMPLE_DATA_ACTION when the number of available data is read.
         *      the number of sample is inside the EXTRA_TAG_NUMBER_SAMPLE extra
         * - READ_TAG_SAMPLE_DATA_ACTION when a data sample is read
         *      the data sample is inside the EXTRA_TAG_SAMPLE_DATA extra
         * - READ_TAG_ERROR_ACTION if the read operation fails, the error message is in EXTRA_ERROR_STR extra
         */
        fun getReadDataSampleFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction(READ_TAG_START_ACTION)
                addAction(READ_TAG_NUMBER_SAMPLE_DATA_ACTION)
                addAction(READ_TAG_SAMPLE_DATA_ACTION)
                addAction(READ_TAG_ERROR_ACTION)
            }
        }

        /**
         * start a service to read the tag configuration
         * [context] context to use to start the service
         * [tag] nfc tag where read the data
         */
        fun startReadConfiguration(context: Context, tag: Tag) {
            val intent = Intent(context, SmartTagService::class.java)
            intent.action = READ_TAG_CONFIGURATION
            intent.putExtra(EXTRA_SMART_TAG, tag)
            context.startService(intent)
        }

        /**
         * start a service to read the sensor extreme from the [tag]
         * the result will be notify with a broadcast message with the action [READ_TAG_EXTREME_DATA_ACTION]
         */
        fun startReadingDataExtreme(context: Context, tag: Tag) {
            val intent = Intent(context, SmartTagService::class.java)
            intent.action = READ_TAG_DATA_EXTREME
            intent.putExtra(EXTRA_SMART_TAG, tag)
            context.startService(intent)
        }

        /**
         * start a service to read the samples from the [tag]
         * the results will be notify as boradcast message with the action:
         * [READ_TAG_NUMBER_SAMPLE_DATA_ACTION] with the number of total samples
         * [READ_TAG_SAMPLE_DATA_ACTION] containing the read sample
         */
        fun startReadingDataSample(context: Context, tag: Tag) {
            val intent = Intent(context, SmartTagService::class.java)
            intent.action = READ_TAG_DATA_SAMPLE
            intent.putExtra(EXTRA_SMART_TAG, tag)
            context.startService(intent)
        }

        /**
         * start a service to set the new [conf] to the [tag]
         * the success will be notify as a broadcast message with the action [WRITE_TAG_COMPLETE_ACTION]
         */
        fun storeConfiguration(context: Context, tag: Tag, conf: SamplingConfiguration) {
            val intent = Intent(context, SmartTagService::class.java)
            intent.action = WRITE_TAG_CONFIGURATION
            intent.putExtra(EXTRA_SMART_TAG, tag)
            intent.putExtra(EXTRA_TAG_CONFIGURATION, conf)
            context.startService(intent)
        }

        /**
         * get the intent filter to detect all the action fired when reading the data samples
         * - READ_TAG_WAIT_ANSWER_ACTION when the service start waiting to charging the tag
         * - READ_TAG_SAMPLE_DATA_ACTION when a data sample is read
         *      the data sample is inside the EXTRA_TAG_SAMPLE_DATA extra
         * - SINGLE_SHOT_DATA_NOT_READY_ACTION when the data are not ready and we will wait again
         * - READ_TAG_ERROR_ACTION if the read operation fails, the error message is in EXTRA_ERROR_STR extra
         */
        fun getReadSingleShotFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction(READ_TAG_WAIT_ANSWER_ACTION)
                addAction(READ_TAG_SAMPLE_DATA_ACTION)
                addAction(SINGLE_SHOT_DATA_NOT_READY_ACTION)
                addAction(READ_TAG_ERROR_ACTION)
            }
        }

        // we keep this static variable to avoid to start again the single shot in case the tag
        // is re detected when a single shot is already running
        private val isSingleShotRequestOngoing = AtomicBoolean(false)


        fun startSingleShotRead(context:Context, tag:Tag){
            val intent = Intent(context, SmartTagService::class.java)
            intent.action = READ_SINGLE_SHOT_DATA
            intent.putExtra(EXTRA_SMART_TAG, tag)
            //it isSingleShotRequestOngoing == false, set to true
            if(isSingleShotRequestOngoing.compareAndSet(false,true))
                context.startService(intent)
            else
                Log.d("SmartTag","single shot ongoing")
        }
    }



}
