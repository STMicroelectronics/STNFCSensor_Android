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
package com.st.smarTag.cloud.config.genericMqtt

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import android.widget.Toast
import com.st.smarTag.cloud.R
import com.st.smarTag.cloud.config.preferences.*
import java.util.regex.Pattern


internal class GenericMqttPreferenceFragment : PreferenceFragmentCompat(){

    private val  config by lazy { GenericMqttUserSettings(requireContext()) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        preferenceManager.preferenceDataStore = config

        setPreferencesFromResource(R.xml.preference_generic_mqtt,rootKey)

        setUpPortPreference()
        setUpUserPreference()
        setUpServerPreference()
    }

    private fun setUpServerPreference() {
        val preference = findPreference(GenericMqttUserSettings.SERVER_KEY)
        updateServiceSummary(preference,config.server)

        preference.setOnPreferenceChangeListener { _, newValue ->
            val newServer = newValue as String
            if(URL_VALIDATOR.match(newServer)) {
                updateServiceSummary(preference, newServer)
                true
            }else {
                Toast.makeText(requireContext(),R.string.prefMqtt_invalidServerAddress, Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    private fun updateServiceSummary(pref:Preference, url:String?){
        if(url.isNullOrBlank()){
            pref.summary = getString(R.string.prefMqtt_serverDescription)
        }else{
            pref.summary = getString(R.string.prefMqtt_serverSummary,url)
        }
    }

    private fun setUpUserPreference() {
        val preference = findPreference(GenericMqttUserSettings.USERNAME_KEY)

        updateUserSummary(preference,config.username)

        preference.setOnPreferenceChangeListener { _, newValue ->
            val newUser = newValue as String
            updateUserSummary(preference,newUser)
            true
        }
    }

    private fun updateUserSummary(pref:Preference, name : String?){
        if(name.isNullOrBlank()){
            pref.summary = getString(R.string.prefMqtt_userNameDescription)
        }else{
            pref.summary = getString(R.string.prefMqtt_userNameSummary, name)
        }
    }

    private fun setUpPortPreference() {
        val preference = findPreference(GenericMqttUserSettings.PORT_KEY)

        updatePortSummary(preference,config.port)
        preference.setOnPreferenceChangeListener { _, newValue ->
            val newPort = newValue as Int
            updatePortSummary(preference,newPort)
            true
        }
    }

    private fun updatePortSummary(pref:Preference,port:Int){
        pref.summary=getString(R.string.prefMqtt_portSummary,port)
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {

        when(preference){
            is ServerPortPreference -> {
                val fragment = ServerPortPreferenceDialog.newInstance(preference.key)
                showPreferenceDialog(fragment)
            }
            is PasswordTextPreference -> {
                val fragment = PasswordTextPreferenceDialog.newInstance(preference.key)
                showPreferenceDialog(fragment)
            }
            else ->{
                super.onDisplayPreferenceDialog(preference)
            }
        }
    }

    private fun Pattern.match(str:String):Boolean{
        return this.matcher(str).matches()
    }

    companion object {
        private val URL_VALIDATOR= Pattern.compile("(tcp|ssh)://.*")
    }
}
