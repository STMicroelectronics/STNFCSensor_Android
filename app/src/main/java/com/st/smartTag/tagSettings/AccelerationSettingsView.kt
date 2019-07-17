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

package com.st.smartTag.tagSettings

import android.content.Context
import com.google.android.material.textfield.TextInputLayout
import android.util.AttributeSet
import android.view.View
import android.widget.CompoundButton
import android.widget.FrameLayout
import com.st.smartTag.R
import com.st.smartTag.util.InputChecker
import com.st.smartTag.util.floatValue
import com.st.smartTag.util.isVisible

class AccelerationSettingsView : FrameLayout {

    private lateinit var isEnableCheckBox: CompoundButton

    private lateinit var eventsLayout:View
    private lateinit var isOrientationEnableCheckBox: CompoundButton
    private lateinit var isWakeUpEnableCheckBox: CompoundButton

    private lateinit var accelerationThresholdInputLayout: TextInputLayout

    var isSensorEnabled: Boolean
        get() = isEnableCheckBox.isChecked
        set(value) {
            isEnableCheckBox.isChecked=value
        }

    var isOrientationEnabled: Boolean
        get() = isOrientationEnableCheckBox.isChecked
        set(value) {
            isOrientationEnableCheckBox.isChecked=value
        }

    var isWakeUpEnabled: Boolean
        get() = isWakeUpEnableCheckBox.isChecked
        set(value) {
            isWakeUpEnableCheckBox.isChecked=value
        }

    var accThreshold:Float?
        get() = accelerationThresholdInputLayout.floatValue
        set(value) {
            accelerationThresholdInputLayout.floatValue = value
        }

    var maxAccThreshold:Float? = null

    var enableEvents: Boolean = false
        set(value) {
            field=value
            updateThresholdVisibility()
        }

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs, defStyle)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        inflate(context, R.layout.view_configure_accelerometer_sensor, this)

        isEnableCheckBox = findViewById(R.id.confAcc_enableCheckBox)
        eventsLayout = findViewById(R.id.confAcc_eventsLayout)
        isOrientationEnableCheckBox = findViewById(R.id.confAcc_orientationEnableCheckBox)
        isWakeUpEnableCheckBox = findViewById(R.id.confAcc_wakeUpEnableCheckBox)

        setUpAccelerationThresholdInput()

        isEnableCheckBox.setOnCheckedChangeListener { _, _ ->
            updateThresholdVisibility()
        }

    }

    private fun setUpAccelerationThresholdInput() {
        accelerationThresholdInputLayout = findViewById(R.id.confAcc_accelerationThLayout)
        accelerationThresholdInputLayout.hint = context.getString(R.string.confSensor_maxHint_format,
                context.getString(R.string.data_acceleration_unit))
        accelerationThresholdInputLayout.editText?.addTextChangedListener(object : InputChecker(accelerationThresholdInputLayout) {
            override fun validate(input: String): Boolean {
                val max = maxAccThreshold ?: return true // no range = valid input
                return accelerationThresholdInputLayout.floatValue ?: Float.MIN_VALUE <= max
            }

            override fun getErrorString(): String {
                return context.getString(R.string.confSensor_maxThError_Format,maxAccThreshold)
            }

        })
    }


    private fun updateThresholdVisibility() {
        val showEvents = isSensorEnabled and enableEvents
        eventsLayout.isVisible = showEvents
        accelerationThresholdInputLayout.visibility = if(showEvents) View.VISIBLE else View.INVISIBLE
    }
}