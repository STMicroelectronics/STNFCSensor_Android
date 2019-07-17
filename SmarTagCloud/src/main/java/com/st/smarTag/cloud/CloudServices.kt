package com.st.smarTag.cloud

import android.content.Context
import androidx.preference.PreferenceFragmentCompat
import com.st.smarTag.cloud.config.AwsIot.AwsIotPreferenceFragment
import com.st.smarTag.cloud.config.AwsIot.AwsIotUserSettings
import com.st.smarTag.cloud.config.genericMqtt.GenericMqttPreferenceFragment
import com.st.smarTag.cloud.config.IBMWatson.IBMWatsonPreferenceFragment
import com.st.smarTag.cloud.config.IBMWatson.IBMWatsonUserSettings
import com.st.smarTag.cloud.config.genericMqtt.GenericMqttUserSettings
import com.st.smarTag.cloud.provider.AwsIot.AwsIotParameters
import com.st.smarTag.cloud.provider.AwsIot.AwsIotProvider
import com.st.smarTag.cloud.provider.CloudProvider
import com.st.smarTag.cloud.provider.IBMWatson.IBMWatsonParameters
import com.st.smarTag.cloud.provider.IBMWatson.IBMWatsonProvider
import com.st.smarTag.cloud.provider.genericMqtt.GenericMqttParameters
import com.st.smarTag.cloud.provider.genericMqtt.GenericMqttProvider

private fun buildGenericMqttProvider(context: Context,tagId: String):CloudProvider{
    val userSettings = GenericMqttUserSettings(context)
    val settings = GenericMqttParameters(userSettings,tagId)
    return GenericMqttProvider(context,settings)
}

private fun buildIbmWatsonQuickStart(context: Context,tagId: String):CloudProvider{
    return IBMWatsonProvider(context, IBMWatsonParameters.getQuickStartParameters(tagId))
}

private fun buildIbmWatson(context: Context,tagId: String):CloudProvider{
    val userSettings = IBMWatsonUserSettings(context)
    val parameters = IBMWatsonParameters(userSettings,tagId)
    return IBMWatsonProvider(context,parameters)
}

private fun buildAWSIot(context: Context,tagId: String):CloudProvider{
    val userSettings = AwsIotUserSettings(context)
    val parameters = AwsIotParameters(context,userSettings,tagId)
    return AwsIotProvider(parameters)
}


internal object CloudServices {

    val GENERIC_MQTT = CloudService("Generic MQTT","cloudLog_genericMqtt", ::buildGenericMqttProvider, GenericMqttPreferenceFragment::class.java)
    val IBM_WATSON = CloudService("IBM Watson","cloudLog_IBMWatson",::buildIbmWatson, IBMWatsonPreferenceFragment::class.java)
    val AWS_IOT = CloudService("AWS IoT","cloudLog_AWSIoT",::buildAWSIot, AwsIotPreferenceFragment::class.java)
    val IBM_WATSON_QUICKSTART = CloudService("IBM Watson QuickStart","cloudLog_IBMWatsonQuickStart",::buildIbmWatsonQuickStart)

    data class CloudService(val name:CharSequence,
                            val key:CharSequence,
                            val builder:(Context, String)->CloudProvider,
                            val conf:Class<out PreferenceFragmentCompat>?=null

    )

    private val services = arrayOf(
            GENERIC_MQTT,
            IBM_WATSON,
            AWS_IOT)

    val names:Array<CharSequence>
        get() = services.map { it.name }.toTypedArray()

    val keys:Array<CharSequence>
        get() = services.map { it.key }.toTypedArray()


    fun getConfigFragment(key:CharSequence):Class<out PreferenceFragmentCompat>?{
        return services.find { it.key == key }?.conf
    }

    fun nameForKey(key:CharSequence):CharSequence?{
        return services.find { it.key == key }?.name
    }

    fun getServiceForKey(key: CharSequence) : CloudService? = services.find { it.key==key }

}