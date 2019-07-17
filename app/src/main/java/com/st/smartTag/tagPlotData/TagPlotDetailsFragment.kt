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

package com.st.smartTag.tagPlotData

import android.app.AlertDialog
import android.app.Dialog
import androidx.fragment.app.DialogFragment
import android.widget.TextView
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.st.smartTag.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class TagPlotDetailsFragment : androidx.fragment.app.DialogFragment(){

    private fun loadView() : androidx.recyclerview.widget.RecyclerView {
        return LayoutInflater.from(activity)
                .inflate(R.layout.dialog_plot_details, null, false) as androidx.recyclerview.widget.RecyclerView
    }

    private lateinit var detailListView: androidx.recyclerview.widget.RecyclerView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        detailListView = loadView()
        val datas = arguments?.getParcelableArrayList<Sample>(ARGS_PLOT_DATA)
        val unit = arguments?.getString(ARGS_DATA_UNIT)
        val dataFormat = arguments?.getString(ARGS_DATA_FORMAT)
        builder.setTitle(getString(R.string.plot_detailsDialogTitle))
        if(datas!=null && unit!=null && dataFormat!=null)
            detailListView.adapter = PlotDetailsAdapter(datas,dataFormat,unit)
        builder.setView(detailListView)

        builder.setNeutralButton(android.R.string.ok,null)
        return builder.create()
    }


    companion object {

        data class Sample(val date: Date,val value:Float):Parcelable{
            constructor(parcel: Parcel) : this(
                    Date(parcel.readLong()),
                    parcel.readFloat())

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeLong(date.time)
                parcel.writeFloat(value)
            }

            override fun describeContents(): Int {
                return 0
            }

            companion object CREATOR : Parcelable.Creator<Sample> {
                override fun createFromParcel(parcel: Parcel): Sample {
                    return Sample(parcel)
                }

                override fun newArray(size: Int): Array<Sample?> {
                    return arrayOfNulls(size)
                }
            }
        }

        private val ARGS_PLOT_DATA = TagPlotDetailsFragment::class.java.name+".PLOT_DATA"
        private val ARGS_DATA_UNIT = TagPlotDetailsFragment::class.java.name+".DATA_UNIT"
        private val ARGS_DATA_FORMAT = TagPlotDetailsFragment::class.java.name+".DATA_FORMAT"

        fun newInstance(dataSet:ArrayList<Sample>,dataFormat:String,dataUnit:String): androidx.fragment.app.DialogFragment {
            val fragment = TagPlotDetailsFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARGS_PLOT_DATA,dataSet)
            args.putString(ARGS_DATA_UNIT,dataUnit)
            args.putString(ARGS_DATA_FORMAT,dataFormat)
            fragment.arguments = args
            return fragment
        }

        class PlotDetailsAdapter(private val dataSet: ArrayList<Sample>,
                                 private val dataFormat:String,
                                 private val dataUnit: String) : androidx.recyclerview.widget.RecyclerView.Adapter<PlotDetailsAdapter.Companion.ViewHolder>() {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(android.R.layout.simple_list_item_1, parent, false)
                return ViewHolder(view)
            }

            override fun getItemCount(): Int {
                return dataSet.size
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                holder.display(dataSet[position],dataFormat,dataUnit)
            }

            companion object {
                val DATE_FORMAT = SimpleDateFormat("HH:mm:ss dd/MMM", Locale.getDefault())

                class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

                    private val textView:TextView = itemView.findViewById(android.R.id.text1)

                    fun display(data: Sample,dataFormat:String, dataUnit: String) {
                        textView.text = String.format("%s: $dataFormat %s",
                                DATE_FORMAT.format(data.date), data.value,dataUnit) }
                    }
                }
            }
        }

    }
