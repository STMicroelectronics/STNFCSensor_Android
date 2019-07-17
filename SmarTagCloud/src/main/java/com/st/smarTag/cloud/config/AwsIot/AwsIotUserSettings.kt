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

import android.content.Context
import com.st.smarTag.cloud.config.securePreferenceStorage.EncryptedPreferenceStorage
import java.nio.charset.StandardCharsets

internal class AwsIotUserSettings(context: Context) : EncryptedPreferenceStorage(context){

    var clientId:String?
        get() {
            val user = getString(CLIENT_ID_KEY,null)
            return if(user.isNullOrBlank()) null else user
        }
        set(value) = putString(CLIENT_ID_KEY,value)

    var endpoint:String?
        get() {
            val user = getString(ENDPOINT_KEY,null)
            return if(user.isNullOrBlank()) null else user
        }
        set(value) = putString(ENDPOINT_KEY,value)

    var privateKey:ByteArray?
        get() {
            val strKey = getString(PRIVATEKEY_KEY,null)
            return strKey?.toByteArray(StandardCharsets.UTF_8)
        }
        set(value) = putString(PRIVATEKEY_KEY,value?.toString(StandardCharsets.UTF_8))

    var certificateKey:ByteArray?
        get() {
            val strKey = getString(CERTIFICATE_KEY,null)
            return strKey?.toByteArray(StandardCharsets.UTF_8)
        }
        set(value) = putString(CERTIFICATE_KEY,value?.toString(StandardCharsets.UTF_8))

    var certificateName:String?
        get() {
            return getString(CERTIFICATE_NAME_KEY,null)
        }
        set(value) = putString(CERTIFICATE_NAME_KEY,value)

    var privateKeyName:String?
        get() {
            return getString(PRIVATEKEY_NAME_KEY,null)
        }
        set(value) = putString(PRIVATEKEY_NAME_KEY,value)

    companion object {

        const val CLIENT_ID_KEY = "awsIot.clientId"
        const val ENDPOINT_KEY ="awsIot.endpoint"
        const val CERTIFICATE_KEY ="awsIot.certificate"
        const val CERTIFICATE_NAME_KEY ="awsIot.certificateName"
        const val PRIVATEKEY_KEY ="awsIot.privateKey"
        const val PRIVATEKEY_NAME_KEY ="awsIot.privateKeyName"
    }
}