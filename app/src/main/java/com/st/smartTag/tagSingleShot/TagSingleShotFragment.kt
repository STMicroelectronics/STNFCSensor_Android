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

package com.st.smartTag.tagSingleShot

import android.animation.ObjectAnimator
import androidx.lifecycle.Observer
import androidx.fragment.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.st.smartTag.NfcTagViewModel
import com.st.smartTag.R

import com.st.smartTag.SmarTagService
import com.st.smartaglib.model.SensorDataSample
import com.st.smartTag.util.DataView
import com.st.smartTag.util.getTypeSerializableExtra

class TagSingleShotFragment : androidx.fragment.app.Fragment() {

    private val nfcServiceResponse = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            when (intent?.action) {
                SmarTagService.READ_TAG_SAMPLE_DATA_ACTION -> {
                    val sensorData: SensorDataSample = intent.getTypeSerializableExtra(SmarTagService.EXTRA_TAG_SAMPLE_DATA)
                    smartTag.newSample(sensorData)
                }
                SmarTagService.READ_TAG_WAIT_ANSWER_ACTION ->{
                    val timeout = intent.getLongExtra(SmarTagService.EXTRA_WAIT_ANSWER_TIMEOUT_MS_DATA,0)
                    smartTag.startWaitingAnswer(timeout)
                }
                SmarTagService.SINGLE_SHOT_DATA_NOT_READY_ACTION -> {
                    smartTag.readFail()
                }
                SmarTagService.READ_TAG_ERROR_ACTION -> {
                    val msg = intent.getStringExtra(SmarTagService.EXTRA_ERROR_STR)
                    nfcTagHolder.nfcTagError(msg)
                }
            }
        }
    }

    private lateinit var smartTag: TagSingleShotVewModel
    private lateinit var nfcTagHolder: NfcTagViewModel

    private lateinit var temperatureView: DataView
    private lateinit var humidityView: DataView
    private lateinit var pressureView: DataView
    private lateinit var vibrationView: DataView
    private lateinit var timeoutProgress:ProgressBar
    private lateinit var waitingView: ViewGroup
    private lateinit var dataView: ViewGroup
    private lateinit var errorMessageView: TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_single_shot, container, false)
        temperatureView = rootView.findViewById(R.id.singleShot_temperature)
        humidityView = rootView.findViewById(R.id.singleShot_humidity)
        pressureView = rootView.findViewById(R.id.singleShot_pressure)
        vibrationView = rootView.findViewById(R.id.singleShot_acc)
        timeoutProgress = rootView.findViewById(R.id.singleShot_timeoutProgress)
        waitingView = rootView.findViewById(R.id.singleShot_waiting)
        dataView = rootView.findViewById(R.id.singleShot_data)
        errorMessageView = rootView.findViewById(R.id.singleShot_readFailMessageView)
        return rootView
    }

    private fun showDataView(){
        dataView.visibility = View.VISIBLE
        waitingView.visibility = View.GONE
    }

    private fun showWaitingView(){
        dataView.visibility = View.GONE
        waitingView.visibility = View.VISIBLE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        nfcTagHolder = NfcTagViewModel.create(activity!!)
        smartTag = TagSingleShotVewModel.create(activity!!)
        initializeSmartTagObserver()
        initializeNfcTagObserver()
    }

    private fun initializeSmartTagObserver() {
        smartTag.sensorDataSample.observe(this, Observer {
            updateDataSample(it)
        })
        smartTag.waitingAnswer.observe(this, Observer {
            showWaitingView(it)
        })
        smartTag.singleShotReadFail.observe(this, Observer {
            updateErrorMessageView(it)
        })
    }

    private fun updateErrorMessageView(readFail: Boolean?){
        errorMessageView.visibility = if(readFail == true) View.VISIBLE else View.GONE
    }

    private fun showWaitingView(timeout: Long?) {
        if(timeout==null)
            return
        val animateProgress = ObjectAnimator.ofInt(timeoutProgress,"progress",0,100)
        animateProgress.duration=timeout
        showWaitingView()
        animateProgress.start()
    }

    private fun initializeNfcTagObserver() {
        nfcTagHolder.nfcTag.observe(this, Observer {
            if (it != null) {
                SmarTagService.startSingleShotRead(context!!, it)
            }else{
                updateDataSample(null)
            }
        })
    }

    private fun Float?.valueOrNan() : Float = this ?: Float.NaN

    private fun updateDataSample(sensorSample: SensorDataSample?) {
        showDataView()
        temperatureView.value= sensorSample?.temperature.valueOrNan().toString()
        pressureView.value = sensorSample?.pressure.valueOrNan().toString()
        vibrationView.value = sensorSample?.acceleration.valueOrNan().toString()
        humidityView.value = sensorSample?.humidity.valueOrNan().toString()

    }

    override fun onResume() {
        super.onResume()
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context!!)
                .registerReceiver(nfcServiceResponse, SmarTagService.getReadSingleShotFilter())
    }


    override fun onPause() {
        super.onPause()
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context!!)
                .unregisterReceiver(nfcServiceResponse)
    }

}
