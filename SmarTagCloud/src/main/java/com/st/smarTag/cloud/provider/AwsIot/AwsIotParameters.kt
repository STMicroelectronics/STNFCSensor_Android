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
package com.st.smarTag.cloud.provider.AwsIot

import android.content.Context
import android.util.Log
import com.amazonaws.AmazonClientException
import com.amazonaws.mobileconnectors.iot.AWSIotCertificateException
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper
import com.st.smarTag.cloud.config.AwsIot.AwsIotUserSettings
import java.lang.IllegalArgumentException
import java.nio.charset.StandardCharsets
import java.security.KeyStore

// Filename of KeyStore file on the filesystem
private const val KEYSTORE_NAME = "iot_keystore"
// Password for the private key in the KeyStore
private const val KEYSTORE_PASSWORD = "password"
// Certificate and key aliases in the KeyStore
private const val CERTIFICATE_ID = "default"

internal class AwsIotParameters(
        val tagId:String,
        val cliendId:String?,
        val endpoint:String?,
        val keyStore:KeyStore?){


    val topic:String = "$cliendId/$tagId"

    constructor(ctx:Context,userSettings: AwsIotUserSettings,tagId: String):this(
            tagId = tagId,
            cliendId = userSettings.clientId,
            endpoint = userSettings.endpoint,
            keyStore = storeCertificate(ctx,
                    userSettings.certificateKey?.toString(StandardCharsets.UTF_8),
                    userSettings.privateKey?.toString(StandardCharsets.UTF_8))
    )

    companion object {
        private fun getKeystoreLocation(ctx: Context): String {
            return ctx.filesDir.absolutePath
        }

        private fun removeOldKeyStore(path: String) {
            if (AWSIotKeystoreHelper.isKeystorePresent(path, KEYSTORE_NAME)!!)
                AWSIotKeystoreHelper.deleteKeystoreAlias(CERTIFICATE_ID, path, KEYSTORE_NAME, KEYSTORE_PASSWORD)
        }

        private fun storeCertificate(ctx:Context,certContent:String?,prvKeyContent:String?):KeyStore?{
            if(certContent==null || prvKeyContent==null)
                return null

            val keystorePath = getKeystoreLocation(ctx)
            removeOldKeyStore(keystorePath)
            try {
                AWSIotKeystoreHelper.saveCertificateAndPrivateKey(CERTIFICATE_ID,
                        certContent,
                        prvKeyContent,
                        keystorePath, KEYSTORE_NAME, KEYSTORE_PASSWORD)
            }catch (ex: Exception) {
                when (ex) {
                    is AWSIotCertificateException,
                    is IllegalArgumentException,
                    is AmazonClientException -> {
                        Log.e("AWS Param", ex.toString())
                        return null
                    }
                    else -> throw ex
                }
            }//try catch


            // load keystore from file into memory to pass on
            // connection
            return AWSIotKeystoreHelper.getIotKeystore(CERTIFICATE_ID,
                    keystorePath, KEYSTORE_NAME, KEYSTORE_PASSWORD)
        }
    }


}