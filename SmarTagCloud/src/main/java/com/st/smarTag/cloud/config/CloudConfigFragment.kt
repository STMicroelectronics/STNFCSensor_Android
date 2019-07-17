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
package com.st.smarTag.cloud.config

import android.os.Bundle
import androidx.preference.CheckBoxPreference

import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.st.smarTag.cloud.CloudServices

import com.st.smarTag.cloud.R


internal class CloudConfigFragment : PreferenceFragmentCompat() {

    private lateinit var enableCloudLog:CheckBoxPreference
    private lateinit var serviceSelector:ListPreference
    private lateinit var configServiceScreen: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_cloud_config,rootKey)

        configServiceScreen = preferenceScreen.findPreference(SERVICE_CONFIG_KEY)
        serviceSelector = preferenceScreen.findPreference(SERVICE_SELECTOR_KEY) as ListPreference
        setupServiceSelector(serviceSelector)
        enableCloudLog = preferenceScreen.findPreference(SERVICE_ENABLE_KEY) as CheckBoxPreference
        setupEnableService(enableCloudLog)


    }

    private fun setupServiceSelector(serviceSelectorList: ListPreference){

        serviceSelectorList.apply {
            entries = CloudServices.names
            entryValues = CloudServices.keys
        }

        serviceSelectorList.setOnPreferenceChangeListener { _, newValue ->
            val newKey = newValue as String
            serviceSelectorList.summary= getString(R.string.prefCloudService_summary,CloudServices.nameForKey(newKey))
            setServiceConfigScreen(newKey)
            true
        }

        setServiceConfigScreen(serviceSelectorList.value)
        serviceSelectorList.entry?.let { value ->
            serviceSelectorList.summary= getString(R.string.prefCloudService_summary,value)
        }

    }

    private fun setupEnableService(enableService:CheckBoxPreference){
        enableService.setOnPreferenceChangeListener { _, newValue ->
            val enableCloudLog = newValue as Boolean
            enableCloudServiceConfiguration(enableCloudLog)
            true
        }

        enableCloudServiceConfiguration(enableService.isChecked)
    }

    private fun enableCloudServiceConfiguration(enable:Boolean){
        serviceSelector.isEnabled = enable
        configServiceScreen.isEnabled =enable
    }

    private fun setServiceConfigScreen(serviceKey:String?){
        if(serviceKey==null) {
            configServiceScreen.isVisible = false
            return
        }
        val configFragment = CloudServices.getConfigFragment(serviceKey)
        if(configFragment!=null){
            configServiceScreen.key=serviceKey
            configServiceScreen.fragment = configFragment::class.java.name
            configServiceScreen.isVisible=true
        }else{
            configServiceScreen.isVisible=false
        }
    }

    companion object {
        const val SERVICE_SELECTOR_KEY = "cloudConfig.service"
        const val SERVICE_ENABLE_KEY = "cloudConfig.enabled"
        private const val SERVICE_CONFIG_KEY = "cloudConfig.serviceConf"
    }

}
