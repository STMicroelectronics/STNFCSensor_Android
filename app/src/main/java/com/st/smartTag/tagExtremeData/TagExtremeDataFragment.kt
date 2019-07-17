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

package com.st.smartTag.tagExtremeData

import androidx.lifecycle.Observer
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.st.smartTag.NfcTagViewModel
import com.st.smartTag.R
import com.st.smartTag.SmarTagService
import com.st.smartTag.util.getTypeSerializableExtra

import com.st.smartaglib.model.DataExtreme
import com.st.smartaglib.model.TagExtreme
import java.text.SimpleDateFormat
import java.util.*


class TagExtremeDataFragment : androidx.fragment.app.Fragment() {

    private val nfcServiceResponse = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            when (intent?.action) {
                SmarTagService.READ_TAG_EXTREME_DATA_ACTION -> {
                    val data = intent.getTypeSerializableExtra<TagExtreme>(SmarTagService.EXTRA_TAG_EXTREME_DATA)
                    smartTag.newExtremeData(data)
                }
                SmarTagService.READ_TAG_ERROR_ACTION -> {
                    val msg = intent.getStringExtra(SmarTagService.EXTRA_ERROR_STR)
                    nfcTagHolder.nfcTagError(msg)
                }
            }
        }
    }

    private lateinit var smartTag: TagExtremeViewModel
    private lateinit var nfcTagHolder: NfcTagViewModel

    private lateinit var temperatureView: ExtremeDataView
    private lateinit var humidityView: ExtremeDataView
    private lateinit var pressureView: ExtremeDataView
    private lateinit var vibrationView: ExtremeDataView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_data_extreme, container, false)
        temperatureView = rootView.findViewById(R.id.extreme_temperature)
        humidityView = rootView.findViewById(R.id.extreme_humidity)
        pressureView = rootView.findViewById(R.id.extreme_pressure)
        vibrationView = rootView.findViewById(R.id.extreme_vibration)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        nfcTagHolder = NfcTagViewModel.create(activity!!)
        smartTag = TagExtremeViewModel.create(activity!!)
        initializeSmartTagObserver()
        initializeNfcTagObserver()
    }

    private fun initializeSmartTagObserver() {
        smartTag.dataExtreme.observe(this, Observer {
            updateExtremeData(it)
        })
    }

    private fun initializeNfcTagObserver() {
        nfcTagHolder.nfcTag.observe(this, Observer {
            if (it != null)
                SmarTagService.startReadingDataExtreme(context!!, it)
        })
    }


    private fun updateExtremeData(extremes: TagExtreme?) {
        temperatureView.setExtreme(extremes?.temperature)

        humidityView.setExtreme(extremes?.humidity)
        pressureView.setExtreme(extremes?.pressure)
        vibrationView.setExtreme(extremes?.vibration)

    }

    override fun onResume() {
        super.onResume()
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context!!)
                .registerReceiver(nfcServiceResponse, SmarTagService.getReadDataExtremeFilter())
    }


    override fun onPause() {
        super.onPause()
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context!!)
                .unregisterReceiver(nfcServiceResponse)
    }

    private fun ExtremeDataView.setExtreme(data: DataExtreme?) {
        if (data == null) {
            visibility = View.GONE
            return
        }
        val dateFormatter = SimpleDateFormat("HH:mm:ss\ndd/MMM/yy", Locale.getDefault())
        visibility = View.VISIBLE
        setMax(data.maxValue, dateFormatter.format(data.maxDate))
        setMin(data.minValue, dateFormatter.format(data.minDate))
    }

}
