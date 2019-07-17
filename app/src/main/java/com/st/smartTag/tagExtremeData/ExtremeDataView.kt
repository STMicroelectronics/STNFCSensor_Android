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

package com.st.smartTag.tagExtremeData

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.st.smartTag.R
import java.util.*

/**
 * Custom view to display the min an max keys for a sensor
 * it has an image called [dataImage] that can be set using the extremeDataImg xml attribute
 * a data unit called [dataUnit] that can be set using the extremeDataUnit xml attribute
 * the format used to display the data can be set using the attribute extremeDataFormat
 * if a sensor doesn't have a min or max key them can be hide with the attribute: extremeHideMinValues or
 * extremeHideMaxValues
 */
class ExtremeDataView : FrameLayout {

    private var mDataFormat: String = "%f"

    private var mDataUnit: String? = ""
    var dataUnit: String?
        get() = mDataUnit
        set(value) {
            mDataUnit = value
            val maxUnit = findViewById<TextView>(R.id.extremeData_maxUnit)
            maxUnit.text = value
            val minUnit = findViewById<TextView>(R.id.extremeData_minUnit)
            minUnit.text = value
        }


    private var mImage: Drawable? = null
    var dataImage: Drawable?
        get() = mImage
        set(value) {
            mImage = value
            val img = findViewById<ImageView>(R.id.singleShot_data_image)
            img.setImageDrawable(mImage)
        }


    var maxValue: CharSequence?
        get() = mMaxValue.text
        set(value) {
            mMaxValue.text = value
        }

    var maxDateValue: CharSequence?
        get() = mMaxDateValue.text
        set(value) {
            mMaxDateValue.text = value
        }

    var minDateValue: CharSequence?
        get() = mMinDateValue.text
        set(value) {
            mMinDateValue.text = value
        }

    var minValue: CharSequence?
        get() = mMinValue.text
        set(value) {
            mMinValue.text = value
        }

    private val mMaxValue by lazy { findViewById<TextView>(R.id.extremeData_maxValue) }
    private val mMaxDateValue by lazy { findViewById<TextView>(R.id.extremeData_maxDateValue) }

    private val mMinValue by lazy { findViewById<TextView>(R.id.extremeData_minValue) }
    private val mMinDateValue by lazy { findViewById<TextView>(R.id.extremeData_minDateValue) }

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
        inflate(context, R.layout.view_extreme_data, this)

        val a = context.obtainStyledAttributes(
                attrs, R.styleable.ExtremeDataView, defStyle, 0)

        dataUnit = a.getString(
                R.styleable.ExtremeDataView_extremeDataUnit)

        mDataFormat = a.getString(
                R.styleable.ExtremeDataView_extremeDataFormat) ?: "%f"

        if (a.hasValue(R.styleable.ExtremeDataView_extremeDataImg)) {
            val img = findViewById<ImageView>(R.id.singleShot_data_image)
            img.setImageResource(a.getResourceId(R.styleable.ExtremeDataView_extremeDataImg, 0))
        }

        val hideMin = a.getBoolean(R.styleable.ExtremeDataView_extremeHideMinValues, false)
        hideMinValues(hideMin)
        val hideMax = a.getBoolean(R.styleable.ExtremeDataView_extremeHideMaxValues, false)
        hideMaxValues(hideMax)
        a.recycle()



    }

    private fun hideMinValues(hide: Boolean) {
        val visibility = if (hide) View.INVISIBLE else View.VISIBLE

        mMinValue.visibility = visibility
        mMinDateValue.visibility = visibility
        findViewById<View>(R.id.extremeData_minUnit).visibility = visibility
        findViewById<View>(R.id.extremeData_minValueLabel).visibility = visibility

    }

    private fun hideMaxValues(hide: Boolean) {
        val visibility = if (hide) View.INVISIBLE else View.VISIBLE

        mMaxValue.visibility = visibility
        mMaxDateValue.visibility = visibility
        findViewById<View>(R.id.extremeData_maxUnit).visibility = visibility
        findViewById<View>(R.id.extremeData_maxValueLabel).visibility = visibility
    }

    fun setMax(value: Float, date: String) {
        maxValue = String.format(Locale.getDefault(),mDataFormat,value)
        maxDateValue = date
    }

    fun setMin(value: Float, date: String) {
        minValue=String.format(Locale.getDefault(),mDataFormat,value)
        minDateValue=date
    }

}
