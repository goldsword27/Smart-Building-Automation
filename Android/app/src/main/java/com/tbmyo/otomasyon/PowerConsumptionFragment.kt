package com.tbmyo.otomasyon

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.tbmyo.otomasyon.network.ApiClient
import com.tbmyo.otomasyon.network.ApiService
import com.tbmyo.otomasyon.network.Device
import com.tbmyo.otomasyon.network.DeviceStatusResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PowerConsumptionFragment : Fragment() {

    private lateinit var chart: HorizontalBarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_power_consumption, container, false)
        chart = view.findViewById(R.id.chart)
        fetchData()
        return view
    }

    private fun fetchData() {
        val apiClient = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        val call = apiClient.getDeviceStatuses()

        call.enqueue(object : Callback<DeviceStatusResponse> {
            override fun onResponse(call: Call<DeviceStatusResponse>, response: Response<DeviceStatusResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { deviceStatusResponse ->
                        Log.d("PowerConsumptionFragment", "Response: ${deviceStatusResponse.devices}")
                        val powerConsumptionData = deviceStatusResponse.devices
                            .filter { it.type == "wall plug" }
                            .map {
                                Log.d("PowerConsumptionFragment", "Device: $it")
                                it.powerConsumption.toFloatOrNull() ?: 0f
                            }
                        Log.d("PowerConsumptionFragment", "Filtered Data: $powerConsumptionData")
                        if (powerConsumptionData.isNotEmpty()) {
                            val firstMonthConsumption = powerConsumptionData.first()
                            val monthlyConsumption = generateMonthlyConsumption(firstMonthConsumption)
                            displayChart(monthlyConsumption)
                        } else {
                            Toast.makeText(context, "No data available", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeviceStatusResponse>, t: Throwable) {
                Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                Log.e("PowerConsumptionFragment", "Error: ${t.message}", t)
            }
        })
    }

    private fun generateMonthlyConsumption(firstMonthConsumption: Float): List<Float> {
        val monthlyConsumption = mutableListOf<Float>()
        for (i in 0 until 12) {
            monthlyConsumption.add(firstMonthConsumption - (i * 200))
        }
        return monthlyConsumption
    }

    private fun displayChart(data: List<Float>) {
        Log.d("PowerConsumptionFragment", "Displaying Chart with data: $data")
        val entries = data.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
        val barDataSet = BarDataSet(entries, "Wall Plug - Güç Tüketimi (kilowatt 'kW')")
        barDataSet.color = Color.BLUE // Bar rengi mavi olarak ayarlandı
        barDataSet.valueTextSize = 10f // Değerlerin yazı boyutu
        barDataSet.valueTextColor = Color.BLACK // Değerlerin yazı rengi

        val barData = BarData(barDataSet)
        chart.data = barData

        // X-Axis ve Y-Axis ayarları
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawLabels(true)
        xAxis.valueFormatter = object : ValueFormatter() {
            private val months = arrayOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")
            override fun getFormattedValue(value: Float): String {
                return months[value.toInt() % months.size]
            }
        }
        xAxis.textSize = 12f
        xAxis.textColor = Color.BLACK

        val yAxisLeft = chart.axisLeft
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.textSize = 12f
        yAxisLeft.textColor = Color.BLACK

        val yAxisRight = chart.axisRight
        yAxisRight.isEnabled = false // Sağ y eksenini kapat

        chart.description.isEnabled = false // Grafik açıklamasını kapat
        chart.setFitBars(true) // Barların grafiğe sığmasını sağlar
        chart.invalidate() // Grafiği güncellemek için
    }
}
