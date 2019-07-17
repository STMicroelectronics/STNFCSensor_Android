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

import android.content.Context
import com.st.smarTag.cloud.config.securePreferenceStorage.EncryptedPreferenceStorage

internal class GenericMqttUserSettings(context:Context) : EncryptedPreferenceStorage(context){

    var port:Int
        get() = getInt(PORT_KEY, DEFAULT_PORT)
        set(value) = putInt(PORT_KEY,value)

    var username:String?
        get() {
            val user = getString(USERNAME_KEY,null)
            return if(user.isNullOrBlank()) null else user
        }
        set(value) = putString(USERNAME_KEY,value)

    var password:String?
        get() {
            val user = getString(PASSWROD_KEY,null)
            return if(user.isNullOrBlank()) null else user
        }
        set(value) = putString(PASSWROD_KEY,value)

    var server:String?
        get() = getString(SERVER_KEY,null)
        set(value) = putString(SERVER_KEY,value)

    companion object {

        private const val DEFAULT_PORT = 1883

        const val PORT_KEY = "genericMqtt.port"
        const val SERVER_KEY ="genericMqtt.server"
        const val USERNAME_KEY ="genericMqtt.userName"
        const val PASSWROD_KEY ="genericMqtt.authToken"
    }
}