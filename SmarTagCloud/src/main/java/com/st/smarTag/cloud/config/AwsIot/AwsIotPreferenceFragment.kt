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
package com.st.smarTag.cloud.config.AwsIot

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import android.widget.Toast
import com.st.smarTag.cloud.R
import com.st.smarTag.cloud.config.preferences.*
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern
import com.google.android.material.snackbar.Snackbar
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


internal class AwsIotPreferenceFragment : PreferenceFragmentCompat(){

    private val  config by lazy { AwsIotUserSettings(requireContext()) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        preferenceManager.preferenceDataStore = config

        setPreferencesFromResource(R.xml.preference_aws_iot,rootKey)

        setUpEndpointPreference()
        setUpClientIdPreference()
        setUpCertificatePreference()
        setUpPrivateKeyPreference()

    }

    private fun setUpPrivateKeyPreference() {
        val preference = findPreference(AwsIotUserSettings.PRIVATEKEY_KEY)
        updatePrivateKeySummary()
        preference.setOnPreferenceClickListener {
            if(hasReadSDPermission(READ_PERMISSION_PRIVATEKEY_FILE))
                startActivityForResult(getFileSelectIntent(), SELECT_PRIVATEKEY_FILE)
            true
        }
    }

    /*create an intent to open a new activity to select a file*/
    private fun getFileSelectIntent(): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        return intent
    }

    private fun setUpCertificatePreference() {
        val preference = findPreference(AwsIotUserSettings.CERTIFICATE_KEY)
        updateCertificateSummary()
        preference.setOnPreferenceClickListener {
            if(hasReadSDPermission(READ_PERMISSION_CERTIFICATE_FILE))
                startActivityForResult(getFileSelectIntent(),SELECT_CERTIFICATE_FILE)
            true
        }
    }

    private fun setUpEndpointPreference() {
        val preference = findPreference(AwsIotUserSettings.ENDPOINT_KEY)
        updateEndpointSummary(preference,config.endpoint)

        preference.setOnPreferenceChangeListener { _, newValue ->
            val newServer = newValue as String
            if(ENPOINT_MATCHER.match(newServer)) {
                updateEndpointSummary(preference, newServer)
                true
            }else {
                Toast.makeText(requireContext(), R.string.prefAws_invalidEndpoint, Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    private fun updateCertificateSummary(){
        val preference = findPreference(AwsIotUserSettings.CERTIFICATE_KEY)
        preference.summary = config.certificateName
    }

    private fun updateEndpointSummary(pref: Preference, url:String?){
        if(url.isNullOrBlank()){
            pref.summary = getString(R.string.prefAws_endpointDescription)
        }else{
            pref.summary = getString(R.string.prefAws_endpointSummary,url)
        }
    }

    private fun setUpClientIdPreference() {
        val preference = findPreference(AwsIotUserSettings.CLIENT_ID_KEY)

        updateClientIdSummary(preference,config.clientId)

        preference.setOnPreferenceChangeListener { _, newValue ->
            val newUser = newValue as String
            updateClientIdSummary(preference,newUser)
            true
        }
    }

    private fun updateClientIdSummary(pref: Preference, name : String?){
        if(name.isNullOrBlank()){
            pref.summary = getString(R.string.prefAws_clientIdDescription)
        }else{
            pref.summary = getString(R.string.prefAws_clientIdSummary, name)
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {

        when(preference){
            is AwsIotCertificateSelectorPreference -> {
                val fragment = AwsIotCertificateSelectorDialogFragment.newInstance(preference.key)
                showPreferenceDialog(fragment)
            }
            else ->{
                super.onDisplayPreferenceDialog(preference)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null)
            return

        val file = data.data ?: return

        when(requestCode){
            SELECT_PRIVATEKEY_FILE -> updatePrivateValue(file)
            SELECT_CERTIFICATE_FILE -> updateCertificateValue(file)
        }
    }

    private fun readAllFile(file:Uri):ByteArray?{
        val inFile = requireContext().contentResolver.openInputStream(file) ?: return null
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        do{
            val length = inFile.read(buffer)
            if(length!=-1){
                result.write(buffer)
            }
        }while (length!=-1)
        return result.toByteArray()
    }

    private fun updatePrivateValue(file: Uri) {
        config.privateKeyName = getFileName(file);
        config.privateKey = readAllFile(file)
        updatePrivateKeySummary()
    }

    private fun updatePrivateKeySummary() {
        val preference = findPreference(AwsIotUserSettings.PRIVATEKEY_KEY)
        preference.summary = config.privateKeyName
    }

    private fun updateCertificateValue(file: Uri) {
        config.certificateName = getFileName(file)
        config.certificateKey = readAllFile(file)
        updateCertificateSummary()
    }

    private fun getFileName(file: Uri): String? {
        when(file.scheme){
            "file" -> {
                return file.lastPathSegment
            }
            "content" -> {
                val cursor = requireContext().contentResolver.query(file,null,null,null,null)
                cursor?.moveToFirst()
                val fileName = cursor?.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                cursor?.close()
                return fileName
            }
            else -> {
                return null
            }
        }
    }

    /**
     * check it we have the permission to write data on the sd
     * @return true if we have it, false if we ask for it
     */
    private fun hasReadSDPermission(requestCode: Int): Boolean {

        val activity = requireActivity()

        if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                Snackbar.make(view!!, R.string.prefAws_requestReadPermission,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok //onClick
                        ) {
                            requestPermissions(
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    requestCode)
                        }.show()
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        requestCode)
            }//if-else
            return false
        } else
            return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        val requestToComplete = permissionToActionRequest(requestCode)
        if(requestToComplete!=null){
            val granted = grantResults.firstOrNull() ?: PackageManager.PERMISSION_DENIED
            if(granted == PackageManager.PERMISSION_GRANTED)
                startActivityForResult(getFileSelectIntent(),requestToComplete)
            else
                Snackbar.make(view!!, R.string.prefAws_deniedReadPermission,
                        Snackbar.LENGTH_SHORT).show()
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }//onRequestPermissionsResult


    private fun Pattern.match(str:String):Boolean{
        return this.matcher(str).matches()
    }

    companion object {

        fun permissionToActionRequest( permissionRequestCode:Int):Int?{
            return when(permissionRequestCode){
                READ_PERMISSION_PRIVATEKEY_FILE -> SELECT_PRIVATEKEY_FILE
                READ_PERMISSION_CERTIFICATE_FILE -> SELECT_CERTIFICATE_FILE
                else -> null
            }
        }

        private const val SELECT_CERTIFICATE_FILE = 1
        private const val SELECT_PRIVATEKEY_FILE = 2
        private const val READ_PERMISSION_CERTIFICATE_FILE = 3
        private const val READ_PERMISSION_PRIVATEKEY_FILE = 4
        private val ENPOINT_MATCHER = Pattern.compile("([-_\\w]*)\\.iot\\.([-_\\w]*)\\.amazonaws\\.com")

    }
}
