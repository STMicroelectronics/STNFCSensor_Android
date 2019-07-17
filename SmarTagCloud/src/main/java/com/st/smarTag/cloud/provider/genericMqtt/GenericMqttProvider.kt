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
package com.st.smarTag.cloud.provider.genericMqtt

import android.content.Context
import android.util.Log
import com.st.smarTag.cloud.provider.CloudProvider
import com.st.smarTag.cloud.provider.MqttActionAdapter
import com.st.smarTag.cloud.provider.json.*
import com.st.smartaglib.model.DataSample
import com.st.smartaglib.model.TagExtreme
import com.st.smartaglib.model.getEventDataSample
import com.st.smartaglib.model.getSensorDataSample
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

internal class GenericMqttProvider(private val context:Context,private val settings:GenericMqttParameters) : CloudProvider{


    private class GenerciMqttConnection(val client: MqttAndroidClient) : CloudProvider.Connection

    override fun connect(callback: CloudProvider.ConnectionCallback) {
        val server = settings.connectionUrl
        if(server==null){
            callback.onFail(IllegalArgumentException("Mqtt server address not specified"))
            return
        }
        val client = MqttAndroidClient(context, server,settings.deviceId)
        val options = MqttConnectOptions().apply {
            isCleanSession=true
            settings.userName?.let {
                userName=it
            }
            settings.password?.let {
                password=it.toCharArray()
            }
        }
        //is the has leaked ServiceConnection due to a in progress disconnection done during a new connection?
        // https://github.com/eclipse/paho.mqtt.android/issues/313
        client.connect(options,context,object : IMqttActionListener{
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                callback.onSuccess(GenerciMqttConnection(client))
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                if(exception!=null)
                    callback.onFail(exception)
                client.close()
                client.unregisterResources()
            }

        })
    }

    private val jsonSerializer = buildSmarTagJsonSerializer()

    override fun uploadSamples(conn: CloudProvider.Connection, samples: List<DataSample>, callback: CloudProvider.PublishCallback) {
        conn.client?.let { mqtt ->
            val sensorJson = jsonSerializer.toJson(samples.getSensorDataSample())
            val sensorMessage = MqttMessage(sensorJson.toByteArray(Charsets.UTF_8))
            mqtt.publish(settings.deviceId+SENSOR_DATA_TOPIC,sensorMessage,null, object :IMqttActionListener{
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    val eventsJson = jsonSerializer.toJson(samples.getEventDataSample())
                    val eventMessage = MqttMessage(eventsJson.toByteArray(Charsets.UTF_8))
                    mqtt.publish(settings.deviceId + SENSOR_EVENT_TOPIC,eventMessage,null, MqttActionAdapter(callback))
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    callback.onFail(exception)
                }
            })
        }
    }


    override fun uploadExtreme(conn: CloudProvider.Connection, extremeData: TagExtreme,callback: CloudProvider.PublishCallback) {
        conn.client?.let { mqtt ->
            val json = jsonSerializer.toJson(extremeData)
            val message = MqttMessage(json.toByteArray(Charsets.UTF_8))
            mqtt.publish(settings.deviceId + EXTREME_TOPIC,message,null,MqttActionAdapter(callback))
        }
    }

    override fun disconnect(connection: CloudProvider.Connection) {

        connection.client?.apply {
            close()
            unregisterResources()
        }
    }

    private val CloudProvider.Connection.client
        get() = (this as? GenerciMqttConnection)?.client

    companion object {
        private const val EXTREME_TOPIC="/extremes"
        private const val SENSOR_DATA_TOPIC="/sensorData"
        private const val SENSOR_EVENT_TOPIC="/eventData"
    }

}