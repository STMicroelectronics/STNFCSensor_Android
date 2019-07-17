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
package com.st.smarTag.cloud.provider.IBMWatson

import com.st.smarTag.cloud.config.IBMWatson.IBMWatsonUserSettings
import java.util.*

internal data class IBMWatsonParameters(
        val organization:String,
        val deviceId:String,
        val authToken:CharArray,
        val deviceType: String ="STNFCSensor",
        val eventsTopic:String = deviceId
){

    constructor(userSettings : IBMWatsonUserSettings,deviceId: String):this(
            organization =  userSettings.organization,
            deviceType =  userSettings.deviceType,
            authToken = userSettings.authToken.toCharArray(),
            deviceId = userSettings.deviceId,
            eventsTopic = deviceId
    )

    val connectionUrl = "ssl://$organization.messaging.internetofthings.ibmcloud.com:8883"
    val connectionDeviceId="d:$organization:$deviceType:$deviceId"


    companion object {

        fun getQuickStartParameters(deviceId:String):IBMWatsonParameters{
            return IBMWatsonParameters("quickstart",deviceId,CharArray(0))
        }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IBMWatsonParameters

        if (organization != other.organization) return false
        if (deviceId != other.deviceId) return false
        if (!Arrays.equals(authToken, other.authToken)) return false
        if (deviceType != other.deviceType) return false
        if (eventsTopic != other.eventsTopic) return false
        if (connectionUrl != other.connectionUrl) return false
        if (connectionDeviceId != other.connectionDeviceId) return false

        return true
    }

    override fun hashCode(): Int {
        var result =  organization.hashCode()
        result = 31 * result + deviceId.hashCode()
        result = 31 * result + Arrays.hashCode(authToken)
        result = 31 * result + deviceType.hashCode()
        result = 31 * result + eventsTopic.hashCode()
        result = 31 * result + connectionUrl.hashCode()
        result = 31 * result + connectionDeviceId.hashCode()
        return result
    }
}