package com.tbmyo.otomasyon

import android.app.ProgressDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.tbmyo.otomasyon.databinding.ItemDeviceBinding
import com.tbmyo.otomasyon.network.ApiService
import com.tbmyo.otomasyon.network.DeviceControlRequest
import com.tbmyo.otomasyon.network.DeviceResponse
import com.tbmyo.otomasyon.network.Device
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import org.json.JSONException
import org.json.JSONObject

class DeviceAdapter(private val devices: List<Device>, private val context: Context, private val apiService: ApiService) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root)

    private var progressDialog: ProgressDialog? = null
    private var currentStatus = mutableMapOf<Int, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.binding.deviceName.text = device.name

        // Dinleyiciyi temizleyin
        holder.binding.deviceSwitch.setOnCheckedChangeListener(null)

        // Mevcut durumu ayarlayın
        holder.binding.deviceSwitch.isChecked = currentStatus[device.id] ?: (device.status == "on")

        // Yeni dinleyici ayarlayın
        holder.binding.deviceSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != currentStatus[device.id]) {
                currentStatus[device.id] = isChecked
                showProgressDialog()
                controlDevice(device, isChecked, holder)
            }
        }

        // Diğer UI elemanlarını ayarlayın
        updateDeviceUI(device, holder)
    }

    override fun getItemCount(): Int = devices.size

    private fun updateDeviceUI(device: Device, holder: DeviceViewHolder) {
        holder.binding.deviceSwitch.isChecked = device.status == "on"
        holder.binding.deviceState.text = if (device.status == "on") "Cihaz açık" else "Cihaz kapalı"
        updateDeviceStateBackground(holder, device.status == "on")

        holder.binding.devicePowerConsumption.text = device.powerConsumption ?: "Bilinmiyor"
        holder.binding.deviceTemperature.text = device.temperature ?: "Bilinmiyor"
        holder.binding.deviceLightLevel.text = device.lightLevel ?: "Bilinmiyor"
        holder.binding.deviceSmokeLevel.text = device.smokeLevel ?: "Bilinmiyor"
    }

    private fun controlDevice(device: Device, isChecked: Boolean, holder: DeviceViewHolder, value: Int? = null) {
        val action = if (isChecked) "on" else "off"
        val status = if (device.type == "dimmer module") "dim" else action
        val controlValue = value?.toString() ?: if (isChecked) "1" else "0"
        val request = DeviceControlRequest(status, controlValue)

        apiService.controlDevice(device.id, request).enqueue(object : Callback<DeviceResponse> {
            override fun onResponse(call: Call<DeviceResponse>, response: Response<DeviceResponse>) {
                dismissProgressDialog()
                if (response.isSuccessful) {
                    Log.d("DeviceControl", "Response: ${response.body()}")
                    showToast("Cihaz başarıyla ${if (isChecked) "açıldı" else "kapandı"}")
                    updateDeviceStateBackground(holder, isChecked)
                    if (device.type == "dimmer module" && isChecked && value != null) {
                        holder.binding.deviceBrightness.text = "Parlaklık: %$value"
                    }
                    updateDeviceStateMessage(holder, device, isChecked, value)
                } else {
                    handleControlDeviceError(response)
                }
            }

            override fun onFailure(call: Call<DeviceResponse>, t: Throwable) {
                Log.e("DeviceControl", "Failure: ${t.message}")
                dismissProgressDialog()
                showToast("Hata: ${t.message}")
            }
        })
    }

    private fun updateDeviceStateBackground(holder: DeviceViewHolder, isOn: Boolean) {
        if (isOn) {
            holder.binding.deviceStateLayout.setBackgroundResource(R.drawable.state_background_on)
            holder.binding.deviceState.setTextColor(0xFF000000.toInt())
        } else {
            holder.binding.deviceStateLayout.setBackgroundResource(R.drawable.state_background_off)
            holder.binding.deviceState.setTextColor(0xFFFFFFFF.toInt())
        }
    }

    private fun updateDeviceStateMessage(holder: DeviceViewHolder, device: Device, isChecked: Boolean, value: Int?) {
        when (device.type) {
            "lamp" -> {
                holder.binding.deviceState.text = if (isChecked) "Lamba yanıyor" else "Lamba kapalı"
            }
            else -> {
                holder.binding.deviceState.text = if (isChecked) "Cihaz açık" else "Cihaz kapalı"
            }
        }
    }

    private fun handleControlDeviceError(response: Response<DeviceResponse>) {
        val errorBody = response.errorBody()?.string()
        Log.e("DeviceControl", "Response error: Code ${response.code()} - ${response.message()}, Body: $errorBody")

        val errorMessage = if (response.code() == 400) {
            errorBody?.let {
                try {
                    val jsonObject = JSONObject(it)
                    jsonObject.getString("message")
                } catch (e: JSONException) {
                    "Cihaz kontrolü başarısız: ${response.message()}"
                }
            } ?: "Cihaz kontrolü başarısız: ${response.message()}"
        } else {
            "Cihaz kontrolü başarısız: ${response.code()} - ${response.message()}"
        }

        showToast(errorMessage)
    }

    private fun showProgressDialog() {
        if (progressDialog == null || !progressDialog!!.isShowing) {
            progressDialog = ProgressDialog(context).apply {
                setMessage("Cihazla iletişim kuruluyor...")
                setCancelable(false)
                show()
            }
        }
    }

    private fun dismissProgressDialog() {
        progressDialog?.dismiss()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

