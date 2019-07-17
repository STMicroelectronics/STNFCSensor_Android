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
package com.st.smarTag.cloud.config.securePreferenceStorage

import android.content.Context
import androidx.preference.PreferenceDataStore
import android.util.Base64
import java.security.MessageDigest

import javax.crypto.Cipher


open class EncryptedPreferenceStorage(context:Context): PreferenceDataStore(){

    companion object {
        private val MASTER_KEY = EncryptedPreferenceStorage::class.java.name
    }

    private val hashAlgorithm = MessageDigest.getInstance("SHA-256")


    private val cipherKey = KeyStoreHelper(context).getOrGenerateSymmetricKey(MASTER_KEY)
    private val cipher: Cipher = Cipher.getInstance(cipherKey.algoProvider)
    private val preferences = context.getSharedPreferences(MASTER_KEY,Context.MODE_PRIVATE)

    private fun computeHash(data:String): String{
        hashAlgorithm.reset()
        val hash = hashAlgorithm.digest(data.toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    private fun encrypt(data: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, cipherKey.key,cipherKey.param)
        val bytes = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun decrypt(data: String): String {
        cipher.init(Cipher.DECRYPT_MODE, cipherKey.key,cipherKey.param)
        val encryptedData = Base64.decode(data, Base64.NO_WRAP)
        val decryptedData = cipher.doFinal(encryptedData)
        return String(decryptedData)
    }

    override fun getInt(key: String?, defValue: Int): Int {
        if(key==null)
            return defValue
        return preferences.getInt(key,defValue)
    }

    override fun putInt(key: String?, value: Int) {
        if(key==null)
            return

        preferences.edit().putInt(key,value).apply()
    }

    override fun getString(key: String?, defValue: String?): String? {
        if(key == null){
            return defValue
        }

        val encKey = computeHash(key)
        val encValue = preferences.getString(encKey,null)
        return if(encValue!=null) decrypt(encValue) else defValue
    }

    override fun putString(key: String?, value: String?) {
        if(key ==null)
            return

        val encKey = computeHash(key)
        val encValue = if(value!=null) encrypt(value) else null

        preferences.edit()
                .putString(encKey,encValue)
                .apply()
    }

}