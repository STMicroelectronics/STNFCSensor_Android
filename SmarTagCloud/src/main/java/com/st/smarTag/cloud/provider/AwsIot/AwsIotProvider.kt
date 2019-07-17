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
package com.st.smarTag.cloud.provider.AwsIot

import android.util.Log
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttMessageDeliveryCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.st.smarTag.cloud.provider.CloudProvider
import com.st.smarTag.cloud.provider.json.buildSmarTagJsonSerializer
import com.st.smartaglib.model.*
import java.lang.IllegalArgumentException
import java.util.concurrent.Executors

internal class AwsIotProvider(private val settings:AwsIotParameters) : CloudProvider {

    private class AwsConnection(val client: AWSIotMqttManager) : CloudProvider.Connection

    private fun checkParameters():Throwable?{
        if(settings.cliendId.isNullOrBlank()){
            return IllegalArgumentException("ClientID can not be empty")
        }
        if(settings.endpoint.isNullOrBlank()){
            return IllegalArgumentException("Endpoint can not be empty")
        }
        if(settings.keyStore==null){
            return IllegalArgumentException("Invalid Certificate or Private key file")
        }
        return null
    }

    override fun connect(callback: CloudProvider.ConnectionCallback) {
        val parametersError = checkParameters()
        if(parametersError!=null){
            callback.onFail(parametersError)
            return
        }
        val manager = AWSIotMqttManager(settings.cliendId,settings.endpoint)
        manager.isAutoReconnect = false
        manager.connect(settings.keyStore) { status, throwable ->
            if (throwable != null) {
                callback.onFail(throwable)
            }else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                callback.onSuccess(AwsConnection(manager))
            }
        }
    }

    private val jsonSerializer = buildSmarTagJsonSerializer()

    private fun uploadDataSamples(mqtt: AWSIotMqttManager, topic:String, data:List<DataSample> ,callback: CloudProvider.PublishCallback){
        BACKGROUND_THREAD.submit{
            val json = jsonSerializer.toJson(data)
            mqtt.publishString(json,
                    topic,
                    MESSAGE_QOS,AWSIotMqttMessageDeliveryCallbackAdapter(callback),null)
        }//submit
    }

    override fun uploadSamples(conn: CloudProvider.Connection, samples: List<DataSample>,callback: CloudProvider.PublishCallback) {


        val eventDataSample = samples.getEventDataSample()

        conn.client?.let { mqtt ->

            uploadDataSamples(mqtt,settings.topic + SENSOR_EVENT_TOPIC,eventDataSample, object : CloudProvider.PublishCallback{
                override fun onSuccess() {
                    val sensorDataSample = samples.getSensorDataSample()
                    uploadDataSamples(mqtt,settings.topic + SENSOR_DATA_TOPIC,sensorDataSample, callback)
                }

                override fun onFail(error: Throwable?) {
                    callback.onFail(error)
                }
            })

        }//client != null
    }


    override fun disconnect(connection: CloudProvider.Connection) {
        BACKGROUND_THREAD.submit{
            connection.client?.disconnect()
        }
    }

    override fun uploadExtreme(conn: CloudProvider.Connection, extremeData: TagExtreme,callback: CloudProvider.PublishCallback) {
        conn.client?.let { mqtt ->
            BACKGROUND_THREAD.submit {
                mqtt.publishString(jsonSerializer.toJson(extremeData),
                        settings.topic+EXTREME_TOPIC,
                        MESSAGE_QOS, AWSIotMqttMessageDeliveryCallbackAdapter(callback),null)
            }//submit
        }//client!=null
    }

    private val CloudProvider.Connection.client: AWSIotMqttManager?
            get() = (this as? AwsIotProvider.AwsConnection)?.client



    companion object {

        private class AWSIotMqttMessageDeliveryCallbackAdapter(val userCallback:CloudProvider.PublishCallback):AWSIotMqttMessageDeliveryCallback{
            override fun statusChanged(status: AWSIotMqttMessageDeliveryCallback.MessageDeliveryStatus?, userData: Any?) {
                when(status){
                    AWSIotMqttMessageDeliveryCallback.MessageDeliveryStatus.Success -> {
                        userCallback.onSuccess()
                    }
                    AWSIotMqttMessageDeliveryCallback.MessageDeliveryStatus.Fail ->
                        userCallback.onFail(null)
                    null -> return
                }
            }

        }

        //the background thread is needed since we can't start a disconnection from the same
        //thread that
        private val BACKGROUND_THREAD = Executors.newSingleThreadExecutor()

        private val MESSAGE_QOS = AWSIotMqttQos.QOS1

        private const val EXTREME_TOPIC="/extremes"
        private const val SENSOR_DATA_TOPIC="/sensorData"
        private const val SENSOR_EVENT_TOPIC="/eventData"
    }

}
