package com.tbmyo.otomasyon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.tbmyo.otomasyon.databinding.FragmentDeviceControlBinding
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tbmyo.otomasyon.network.ApiClient
import com.tbmyo.otomasyon.network.ApiService
import com.tbmyo.otomasyon.network.DeviceStatusResponse
import com.tbmyo.otomasyon.network.Device
import com.tbmyo.otomasyon.network.User
import androidx.navigation.fragment.findNavController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log

class DeviceControlFragment : Fragment() {

    private var _binding: FragmentDeviceControlBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDeviceControlBinding.inflate(inflater, container, false)
        val view = binding.root

        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)

        fetchUserInformation()

        val masterKey = MasterKey.Builder(requireContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPref = EncryptedSharedPreferences.create(
            requireContext(),
            "sharedPrefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val userRole = sharedPref.getString("USER_ROLE", "user") ?: "user"

        if (userRole == "administrator") {
            binding.adminButton.visibility = View.VISIBLE
        } else {
            binding.adminButton.visibility = View.GONE
        }

        binding.adminButton.setOnClickListener {
            findNavController().navigate(R.id.action_deviceControlFragment_to_adminFragment)
        }

        fetchDeviceStatuses()

        binding.loginInfoTextView.visibility = View.VISIBLE

        return view
    }

    private fun fetchUserInformation() {
        apiService.getUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val user = response.body()?.firstOrNull()
                    if (user != null) {
                        binding.loginInfoTextView.text = "${user.email} tarafından giriş yapıldı"
                    }
                } else {
                    Log.e("DeviceControl", "Failed to fetch user information: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("DeviceControl", "Error: ${t.message}")
            }
        })
    }

    private fun fetchDeviceStatuses() {
        apiService.getDeviceStatuses().enqueue(object : Callback<DeviceStatusResponse> {
            override fun onResponse(call: Call<DeviceStatusResponse>, response: Response<DeviceStatusResponse>) {
                if (response.isSuccessful) {
                    val devices = response.body()?.devices ?: emptyList()
                    setupRecyclerView(devices)
                } else {
                    Log.e("DeviceControl", "Failed to fetch device statuses: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<DeviceStatusResponse>, t: Throwable) {
                Log.e("DeviceControl", "Error: ${t.message}")
            }
        })
    }

    private fun setupRecyclerView(devices: List<Device>) {
        binding.deviceRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.deviceRecyclerView.adapter = DeviceAdapter(devices, requireContext(), apiService)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
