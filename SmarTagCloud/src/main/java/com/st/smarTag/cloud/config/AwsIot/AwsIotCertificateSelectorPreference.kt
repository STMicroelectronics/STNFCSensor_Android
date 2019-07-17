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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import com.st.smarTag.cloud.R

internal class AwsIotCertificateSelectorPreference : DialogPreference{
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int,
                defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)


    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr)


    constructor(context: Context, attrs: AttributeSet?):super(context, attrs)

    constructor(context: Context):super(context)

    override fun getDialogLayoutResource(): Int {
        return R.layout.pref_dialog_aws_certificate
    }

}

/**
 * this class is needed to store the certificate direclty inside the aws sdk both the device and ca are needed
 */
internal class AwsIotCertificateSelectorDialogFragment : PreferenceDialogFragmentCompat() {


    /*create an intent to open a new activity to select a file*/
    private fun getFileSelectIntent(): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        return intent
    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)

        view?.findViewById<Button>(R.id.prefAws_selectCertificateButton)?.setOnClickListener {
            startActivityForResult(getFileSelectIntent(), SELECT_CERTIFICATE_FILE)
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

    private fun updateCertificateValue(file: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun updatePrivateValue(file: Any) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        private const val SELECT_PRIVATEKEY_FILE = 2
        private const val SELECT_CERTIFICATE_FILE = 1

        fun newInstance(key:String): AwsIotCertificateSelectorDialogFragment {
            val fragment = AwsIotCertificateSelectorDialogFragment()
            val b = Bundle(1)
            b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.setArguments(b)

            return fragment
        }

    }
}