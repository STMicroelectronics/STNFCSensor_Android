package com.st.smarTag.cloud.provider

import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken

internal class MqttActionAdapter(val userCallback: CloudProvider.PublishCallback): IMqttActionListener {
    override fun onSuccess(asyncActionToken: IMqttToken?) {
        userCallback.onSuccess()
    }

    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
        userCallback.onFail(exception)
    }
}