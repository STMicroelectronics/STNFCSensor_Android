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

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.st.smartTag.R
import com.st.smartTag.model.SensorDataSample
import com.st.smartTag.tagPlotData.TagPlotDetailsFragment.Companion.Sample
import java.util.*


class TagPlotDataFragment : Fragment() {

    private lateinit var smartTag: TagDataViewModel

    private lateinit var pressureView:View
    private lateinit var pressureChart: LineChart

    private lateinit var temperatureView:View
    private lateinit var temperatureChart:LineChart

    private lateinit var humidityView:View
    private lateinit var humidityChart: LineChart

    private lateinit var vibrationView:View
    private lateinit var vibrationChart: LineChart
    private var fistSampleTime:Long=0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_tag_plot_data, container, false)

        pressureView = rootView.findViewById(R.id.tagPlot_pressureView)
        pressureChart = rootView.findViewById<SmartTagPlotView>(R.id.tagPlot_pressurePlot).chart
        configureChart(pressureChart, PRESSURE_EXTREME)
        rootView.findViewById<Button>(R.id.tagPlot_pressureDetailsButton).setOnClickListener {
            showDetails(pressureChart.data.getDataSetByIndex(0),getString(R.string.data_pressure_format),
                    getString(R.string.data_pressure_unit)
            )
        }

        temperatureView = rootView.findViewById(R.id.tagPlot_temperatureView)
        temperatureChart = rootView.findViewById<SmartTagPlotView>(R.id.tagPlot_temperaturePlot).chart
        configureChart(temperatureChart, TEMPERATURE_EXTREME)
        rootView.findViewById<Button>(R.id.tagPlot_temperatureDetailsButton).setOnClickListener {
            showDetails(temperatureChart.data.getDataSetByIndex(0),getString(R.string.data_temperature_format),
                getString(R.string.data_temperature_unit))
        }

        humidityView = rootView.findViewById(R.id.tagPlot_humidityView)
        humidityChart = rootView.findViewById<SmartTagPlotView>(R.id.tagPlot_humidityPlot).chart
        configureChart(humidityChart, HUMIDITY_EXTREME)
        rootView.findViewById<Button>(R.id.tagPlot_humidityDetailsButton).setOnClickListener {
            showDetails(humidityChart.data.getDataSetByIndex(0),getString(R.string.data_humidity_format),
                    getString(R.string.data_humidity_unit))
        }

        vibrationView = rootView.findViewById(R.id.tagPlot_vibrationView)
        vibrationChart = rootView.findViewById<SmartTagPlotView>(R.id.tagPlot_vibrationPlot).chart
        configureChart(vibrationChart, VIBRATION_EXTREME)
        rootView.findViewById<Button>(R.id.tagPlot_vibrationDetailsButton).setOnClickListener {
            showDetails(vibrationChart.data.getDataSetByIndex(0),getString(R.string.data_vibration_format),
                    getString(R.string.data_acceleration_unit))
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        smartTag = TagDataViewModel.create(activity!!)
        initializeSmartTagObserver()
    }

    private fun initializeSmartTagObserver() {

        smartTag.sensorSampleList.observe(this, Observer<MutableList<SensorDataSample>> {
            pressureChart.clearPlotData()
            temperatureChart.clearPlotData()
            humidityChart.clearPlotData()
            vibrationChart.clearPlotData()
            it?.forEach { appendSample(it) }
        })

        smartTag.lastSensorSample.observe(this, Observer {
            it?.let { appendSample(it) }
        })

    }


    private fun configureChart(chart: LineChart, extreme: ChartExtreme?) {

        //hide chart description
        chart.description.isEnabled = false

        // isEnable touch gestures
        chart.setTouchEnabled(true)

        // isEnable scaling and dragging
        chart.setDragEnabled(true)
        chart.setScaleEnabled(true)
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true)

        //add empty point set
        chart.data = LineData()

        //noLegend
        chart.legend.isEnabled = false

        val xl = chart.xAxis
        xl.position = XAxis.XAxisPosition.BOTTOM
        xl.setDrawLabels(false)
        xl.setDrawGridLines(false)
        xl.setAvoidFirstLastClipping(true)

        chart.axisRight.isEnabled = false
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)

        if(extreme==null)
            chart.isAutoScaleMinMaxEnabled = true
        else {
            chart.axisLeft.apply{
                axisMinimum = extreme.min
                axisMaximum = extreme.max
            }
        }
    }


    private fun appendSample(sensorSample: SensorDataSample) {
        val x = sensorSample.date.time
        appendOrHide(pressureView,pressureChart,x,sensorSample.pressure)
        appendOrHide(humidityView,humidityChart,x,sensorSample.humidity)
        appendOrHide(vibrationView,vibrationChart,x,sensorSample.acceleration)
        appendOrHide(temperatureView,temperatureChart,x,sensorSample.temperature)
    }

    private fun appendOrHide(view:View,chart:LineChart,x: Long,y: Float?) {
        if(y!=null){
            appendValue(chart,x,y)
            view.visibility=View.VISIBLE
        }else{
            if(chart.data.dataSetCount==0) //hide the plot only if it empty
                view.visibility=View.GONE
        }
    }

    private fun appendValue(chart: LineChart, x:Long, y: Float) {
        if (chart.data.dataSetCount == 0) {
            chart.data.addDataSet(createSet())
            fistSampleTime = x
        }

        val dataSet = chart.data.getDataSetByIndex(0)

        val entry = Entry((x-fistSampleTime).toFloat(),y)
        dataSet.addEntry(entry)
        chart.syncPlotGui()
        chart.moveViewToX(entry.x)
    }

    private fun createSet(): LineDataSet {

        val set = LineDataSet(null, "NotUsed")
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.setDrawCircles(false)
        set.setDrawValues(false)
        set.setDrawHighlightIndicators(false)
        set.color = ContextCompat.getColor(context!!, R.color.colorPrimary)
        set.setCircleColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))
        return set
    }

    private fun LineChart.clearPlotData() {
        data.dataSets.clear()
        syncPlotGui()
    }

    private fun LineChart.syncPlotGui() {
        data.notifyDataChanged()
        notifyDataSetChanged()
    }

    private fun createdetailsDataSet(plotData: ILineDataSet):ArrayList<Sample>{
        val dataSet = ArrayList<Sample>(plotData.entryCount)
        for (i in 0 until plotData.entryCount){
            val plotEntry = plotData.getEntryForIndex(i);
            val date = Date(fistSampleTime+plotEntry.x.toLong())
            dataSet.add(Sample(date,plotEntry.y))
        }
        return dataSet
    }

    private fun showDetails(plotData: ILineDataSet?,dataFormat:String,unit:String) {
        if(plotData==null || plotData.entryCount==0) //no data no details to show
            return
        val dialog = TagPlotDetailsFragment.newInstance(createdetailsDataSet(plotData),dataFormat,unit)
        dialog.show(childFragmentManager, DETAILS_DIALOG_TAG)
    }

    companion object {

        private val DETAILS_DIALOG_TAG = TagPlotDataFragment::class.java.canonicalName +".DETAILS_DIALOG_TAG"

        private data class ChartExtreme(val min:Float,val max:Float)
        private val TEMPERATURE_EXTREME = ChartExtreme(min = -5.0f,max = 45.0f)
        private val PRESSURE_EXTREME = ChartExtreme(min = 950f,max = 1150f)
        private val HUMIDITY_EXTREME = ChartExtreme(min = 0f,max = 100f)
        private val VIBRATION_EXTREME = ChartExtreme(min = 600f,max = 63f*256f)
    }

}