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

package com.st.smartTag.util

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.st.smartTag.R

class DataView : FrameLayout {


    var value: CharSequence?
        get() = mValue.text
        set(value) {
            mValue.text = value
            mUnit.visibility= View.VISIBLE
        }

    private lateinit var mValue: TextView
    private lateinit var mUnit: TextView

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
        inflate(context, R.layout.view_single_shot_data, this)

        mValue = findViewById(R.id.singleShot_data_value)
        mUnit = findViewById(R.id.singleShot_data_unit)

        val a = context.obtainStyledAttributes(
                attrs, R.styleable.DataView, defStyle, 0)

        setUnitValue(a,mUnit)
        setTitle(a)
        setImage(a)

        a.recycle()


    }

    private fun setImage(a: TypedArray) {
        if (a.hasValue(R.styleable.DataView_dataImg)) {
            val img = findViewById<ImageView>(R.id.singleShot_data_image)
            img.setImageResource(a.getResourceId(R.styleable.DataView_dataImg, 0))
        }
    }

    private fun setUnitValue(a: TypedArray,unitTextView: TextView) {
        unitTextView.visibility = View.INVISIBLE

        if (!a.hasValue(R.styleable.DataView_dataUnit))
            return

        val dataUnit = a.getString(
                R.styleable.DataView_dataUnit)

        unitTextView.text = dataUnit
    }

    private fun setTitle(a: TypedArray) {
        if (!a.hasValue(R.styleable.DataView_dataTitle))
            return

        val title = a.getString(
                R.styleable.DataView_dataTitle)

        val titleTextView = findViewById<TextView>(R.id.singleShot_data_title)
        titleTextView.text = title
    }


}
