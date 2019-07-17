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

package com.st.smartTag.tagPlotData

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.fragment.app.FragmentActivity
import android.util.Log
import com.st.smartaglib.model.DataSample
import com.st.smartaglib.model.EventDataSample
import com.st.smartaglib.model.SensorDataSample

class TagDataViewModel : ViewModel() {

    private val _numberSample = MutableLiveData<Int>()
    /**
     * number of sensor and event sample read from the tag
     */
    val numberSample: LiveData<Int>
        get() = _numberSample


    private val _isUpdating = MutableLiveData<Boolean>()

    val isUpdating : LiveData<Boolean>
        get() = _isUpdating

    private val _allSampleList = MutableLiveData<MutableList<DataSample>>()

    /**
     * list of [numberSample] sample read from the tag
     */
    val allSampleList: LiveData<MutableList<DataSample>>
        get() = _allSampleList

    private val _sensorSampleList = MutableLiveData<MutableList<SensorDataSample>>()

    /**
     * list with all the sensor data read from the tag
     * @note the event samples with an acceleration will be mapped as a sensor sample with only the acceleration data
     */
    val sensorSampleList: LiveData<MutableList<SensorDataSample>>
        get() = _sensorSampleList

    private val _lastSensorSample = MutableLiveData<SensorDataSample>()

    /**
     * last sensor sample read from the board
     */
    val lastSensorSample: LiveData<SensorDataSample>
        get() = _lastSensorSample

    private val _eventSampleList = MutableLiveData<MutableList<EventDataSample>>()

    /**
     * list with all the event data sample read from the tag
     */
    val eventSampleList: LiveData<MutableList<EventDataSample>>
        get() = _eventSampleList

    private val _lastEventSample = MutableLiveData<EventDataSample>()

    /**
     * last event sensor read from the board
     */
    val lastEventSample: LiveData<EventDataSample>
        get() = _lastEventSample

    private fun appendSensorSample( sensorSample: SensorDataSample){
        _lastSensorSample.value = sensorSample
        _sensorSampleList.value?.add(sensorSample)
    }

    private fun appendEventDataSample(eventSample:EventDataSample){
        _eventSampleList.value?.add(eventSample)
        _lastEventSample.value = eventSample
        //if it has the acceleration data, create also a sensor sample
        if(eventSample.acceleration!=null) {
            val accelerationSample = SensorDataSample(eventSample.date,
                    null, null, null, eventSample.acceleration?.toFloat())
            appendSensorSample(accelerationSample)
        }
    }

    fun appendSample(sensorSample: DataSample) {
        _allSampleList.value?.add(sensorSample)
        _isUpdating.value = _allSampleList.value?.size != _numberSample.value
        Log.d("VM",""+_allSampleList.value?.size +"->"+_numberSample.value)

        when(sensorSample){
            is SensorDataSample -> {
                appendSensorSample(sensorSample)
            }
            is EventDataSample -> {
                appendEventDataSample(sensorSample)
            }
        }
    }

    /**
     * set the new sample number
     * this method will also reset all the sensor list
     */
    fun setNumberSample(num: Int) {
        _numberSample.value = num
        cleanSampleData()
    }

    private fun cleanSampleData() {
        _sensorSampleList.value = mutableListOf()
        _eventSampleList.value = mutableListOf()
        _allSampleList.value = mutableListOf()
        _lastSensorSample.value = null
        _lastEventSample.value = null
        _isUpdating.value=false
    }

    companion object {
        fun create(activity: androidx.fragment.app.FragmentActivity): TagDataViewModel {
            return ViewModelProviders.of(activity).get(TagDataViewModel::class.java)
        }
    }
}
