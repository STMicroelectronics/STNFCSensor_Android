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
package com.st.smarTag.cloud.config.preferences

import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.EditTextPreferenceDialogFragmentCompat
import androidx.preference.PreferenceDialogFragmentCompat
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.st.smarTag.cloud.R
import java.util.regex.Pattern

internal class ServerUrlPreference : EditTextPreference {
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int,
                defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)


    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr)


    constructor(context: Context, attrs: AttributeSet?):super(context, attrs)

    constructor(context: Context):super(context)

    var serverUrl:String
        get() = getPersistedString("")
        set(value) { persistString(value) }


    override fun getDialogLayoutResource(): Int {
        return R.layout.perf_dialog_server_url
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        val defaultValue = a?.getString(index)
        return defaultValue ?: ""
    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        if(!restoreValue && defaultValue!=null) {
            serverUrl = defaultValue as String
        }
    }

}

class ServerUrlPreferenceDialog : EditTextPreferenceDialogFragmentCompat(){

    private lateinit var editText: EditText

    private lateinit var urlValidator: Pattern

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        urlValidator=arguments?.getSerializable(URL_VALIDATOR_KEY) as Pattern

    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
        if(view==null)
            return
        editText = view.findViewById(android.R.id.edit)

        val preference = preference as ServerUrlPreference

        val currenctAddres = preference.serverUrl
        editText.setText(currenctAddres)
    }


    override fun onDialogClosed(positiveResult: Boolean) {
        if(!positiveResult)
            return

        val newValue = editText.text.toString()
        if(newValue.isNotBlank() && urlValidator.match(newValue)){
            val preference = preference as ServerUrlPreference
            if (preference.callChangeListener(newValue)){
                preference.serverUrl = newValue
            }
        }else{
            Toast.makeText(requireContext(),"invalid url",Toast.LENGTH_SHORT).show()
        }
    }

    private fun Pattern.match(str:String):Boolean{
        return this.matcher(str).matches()
    }

    companion object {

        private val URL_VALIDATOR_KEY = ServerUrlPreferenceDialog::class.java.name+".URL_VALIDATOR_KEY"

        fun newInstance(key:String, urlValidator: Pattern): ServerUrlPreferenceDialog {
            val fragment = ServerUrlPreferenceDialog()
            val b = Bundle(2)
            b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            b.putSerializable(URL_VALIDATOR_KEY,urlValidator)
            fragment.setArguments(b)

            return fragment
        }
    }
}