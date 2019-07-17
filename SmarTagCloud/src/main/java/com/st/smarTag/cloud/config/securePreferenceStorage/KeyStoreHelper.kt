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

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.math.BigInteger
import java.security.*
import java.security.spec.AlgorithmParameterSpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal

/**
 * This class wraps [KeyStore] class apis with some additional possibilities.
 */
class KeyStoreHelper(private val context: Context) {

    private val keyStore: KeyStore = createAndroidKeyStore()

    fun getOrGenerateAsymmetricKeyPair(alias: String): KeyPair {
        val privateKey = keyStore.getKey(alias, null) as PrivateKey?
        val publicKey = keyStore.getCertificate(alias)?.publicKey

        return if (privateKey != null && publicKey != null) {
            KeyPair(publicKey, privateKey)
        } else {
            createAndroidKeyStoreAsymmetricKey(alias)
            return getOrGenerateAsymmetricKeyPair(alias)
        }
    }

    data class SymmetricKey(
            val algoProvider:String,
            val param: AlgorithmParameterSpec,
            val key:Key)

    fun getOrGenerateSymmetricKey(alias:String):SymmetricKey{
        val key = if (hasMarshmallow()) {
            getAESKeyApi23(alias)
        } else {
            getAESKeyApiPre23(alias)
        }

        return SymmetricKey(TRANSFORMATION_SYMMETRIC, buildAESIV(),key)
    }

    private fun getAESKeyApiPre23(alias: String): SecretKey {
        val rsaKey = getOrGenerateAsymmetricKeyPair(alias+"_RSA")
        val pref = context.getSharedPreferences(KeyStoreHelper::class.java.name, Context.MODE_PRIVATE)
        var encryptedKey = pref.getString(alias, null)
        if(encryptedKey == null) {
            encryptedKey = rsaEncrypt(generateRandomKey(32),rsaKey.public)
            pref.edit()
                    .putString(alias,encryptedKey)
                    .apply()
        }
        val temp = rsaDecrypt(encryptedKey,rsaKey.private)
        return SecretKeySpec(rsaDecrypt(encryptedKey,rsaKey.private), "AES")
    }

    private fun generateRandomKey(size:Int):ByteArray{
        val key = ByteArray(size)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(key)
        return key
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun getAESKeyApi23(alias: String):Key{
        return if (keyStore.containsAlias(alias)) {
            keyStore.getKey(alias,null)
        }else{
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGenerator.init(
                    KeyGenParameterSpec.Builder(alias,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setRandomizedEncryptionRequired(false)
                            .build())
            keyGenerator.generateKey()
        }
    }

    private fun createAndroidKeyStoreAsymmetricKey(alias: String): KeyPair {
        val generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")

        if (hasMarshmallow()) {
            initGeneratorWithKeyGenParameterSpec(generator, alias)
        } else {
            initGeneratorWithKeyPairGeneratorSpec(generator, alias)
        }

        return generator.generateKeyPair()
    }



    fun removeAndroidKeyStoreKey(alias: String) = keyStore.deleteEntry(alias)

    private fun initGeneratorWithKeyPairGeneratorSpec(generator: KeyPairGenerator, alias: String) {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.YEAR, 20)

        val builder = KeyPairGeneratorSpec.Builder(context)
                .setAlias(alias)
                .setSerialNumber(BigInteger.ONE)
                .setSubject(X500Principal("CN=${alias} CA Certificate"))
                .setStartDate(startDate.time)
                .setEndDate(endDate.time)

        generator.initialize(builder.build())
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun initGeneratorWithKeyGenParameterSpec(generator: KeyPairGenerator, alias: String) {
        val builder = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
        generator.initialize(builder.build())
    }

    private fun createAndroidKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        return keyStore
    }

    private fun hasMarshmallow() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    private fun rsaEncrypt(data: ByteArray,pubKey:PublicKey): String {
        val cipher: Cipher = Cipher.getInstance(TRANSFORMATION_ASYMMETRIC)
        cipher.init(Cipher.ENCRYPT_MODE, pubKey)
        val bytes = cipher.doFinal(data)
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun rsaDecrypt(data: String,privateKey: PrivateKey): ByteArray {
        val cipher: Cipher = Cipher.getInstance(TRANSFORMATION_ASYMMETRIC)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedData = Base64.decode(data, Base64.DEFAULT)
        return cipher.doFinal(encryptedData)
    }

    private fun buildAESIV() : AlgorithmParameterSpec{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // GCMParameterSpec should always be present in Java 7 or newer, but it's missing on
            // some Android devices with API level <= 19. Fortunately, we can initialize the cipher
            // with just an IvParameterSpec. It will use a tag size of 96 bits.
            //return IvParameterSpec(buf, offset, len);
            return  IvParameterSpec(FIX_IV)
        }
        return GCMParameterSpec(FIX_IV.size*8, FIX_IV)
    }

    companion object {
        private const val TRANSFORMATION_ASYMMETRIC = "RSA/ECB/PKCS1Padding"
        private const val TRANSFORMATION_SYMMETRIC = "AES/GCM/NoPadding"
        private val FIX_IV  = byteArrayOf(0x21, 0xa1.toByte(), 0x62, 0x29,
                0x75, 0xde.toByte(), 0xe5.toByte(), 0xe6.toByte(), 0xcd.toByte(), 0x4f.toByte(),
                0x55, 0x21)
    }

}
