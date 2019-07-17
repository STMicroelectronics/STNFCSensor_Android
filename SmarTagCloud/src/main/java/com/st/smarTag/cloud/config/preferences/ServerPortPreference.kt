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
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import com.st.smarTag.cloud.R
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.EditTextPreferenceDialogFragmentCompat
import androidx.preference.PreferenceDialogFragmentCompat

private const val DEFAULT_PORT=0

internal class ServerPortPreference : EditTextPreference {
   constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int,
                           defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)


   constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr)


   constructor(context: Context, attrs: AttributeSet?):super(context, attrs)

   constructor(context: Context):super(context)

   var port:Int
       get() = getPersistedInt(DEFAULT_PORT)
       set(value) { persistInt(value) }


   override fun getDialogLayoutResource(): Int {
       return R.layout.perf_dialog_server_port;
   }

   override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
       val defaultValue = a?.getInt(index, DEFAULT_PORT)
       return defaultValue ?: DEFAULT_PORT
   }

   override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
       if(!restoreValue && defaultValue!=null) {
           port = defaultValue as Int
       }
   }

}

class ServerPortPreferenceDialog : EditTextPreferenceDialogFragmentCompat(){

    private lateinit var editText: EditText

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
        if(view==null)
            return
        editText = view.findViewById(android.R.id.edit)

        val preference = preference as ServerPortPreference

        val currentPort = preference.port
        editText.setText(currentPort.toString())
    }


    override fun onDialogClosed(positiveResult: Boolean) {
        if(!positiveResult)
            return

        val newValue = editText.text.toString().toIntOrNull()
        if(newValue!=null){
            val preference = preference as ServerPortPreference
            if (preference.callChangeListener(newValue)){
                preference.port=newValue
            }
        }
    }

    companion object {
        fun newInstance(key:String): ServerPortPreferenceDialog {
            val fragment = ServerPortPreferenceDialog()
            val b = Bundle(1)
            b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.setArguments(b)

            return fragment
        }
    }
}