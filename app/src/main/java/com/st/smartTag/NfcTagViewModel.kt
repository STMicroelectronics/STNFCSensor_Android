/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
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

package com.st.smartTag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import android.nfc.Tag
import androidx.fragment.app.FragmentActivity

/**
 * View model containing the last nfc tag detected and the last error happening during the use of
 * the tag
 */
class NfcTagViewModel : ViewModel() {

    private val _nfcTag = MutableLiveData<Tag>()

    /**
     * last nfc tag detected, if null no tag are detected, or an error happen
     */
    val nfcTag: LiveData<Tag>
        get() = _nfcTag

    private val _ioError = MutableLiveData<String>()

    /**
     * last error description or null if not error
     */
    val ioError: LiveData<String>
        get() = _ioError

    /**
     * call when a new [tag] is available, it resets the error key
     */
    fun nfcTagDiscovered(tag: Tag) {
        //Log.d("nfcTag", "Tag: " + tag + "Id: " + tag.id.contentToString())
        if (_nfcTag.value != tag)
            _nfcTag.postValue(tag)
        //remove old error
        _ioError.postValue(null)
    }

    /**
     * call when the [nfcTag] is lost/disconnected
     */
    fun nfcTagLost() {
        _nfcTag.postValue(null)
    }

    /**
     * call when an [error] happen during the IO to using the [nfcTag]
     */
    fun nfcTagError(error: String) {
        nfcTagLost()
        _ioError.postValue(error)
    }


    companion object {
        /**
         * helper function to create a viewmodel attach to the [activity]
         */
        fun create(activity: androidx.fragment.app.FragmentActivity): NfcTagViewModel {
            return ViewModelProviders.of(activity).get(NfcTagViewModel::class.java)
        }
    }

}