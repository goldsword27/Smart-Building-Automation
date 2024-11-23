package com.tbmyo.otomasyon
//Günlük kayıtlarının görüntülendiği arayüz
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.tbmyo.otomasyon.databinding.FragmentLogBinding
import com.tbmyo.otomasyon.network.ApiClient
import com.tbmyo.otomasyon.network.ApiService
import com.tbmyo.otomasyon.network.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class LogFragment : Fragment() {

    private lateinit var binding: FragmentLogBinding
    private lateinit var logAdapter: LogAdapter
    private var logs: MutableList<Log> = mutableListOf()
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLogBinding.inflate(inflater, container, false)


        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)


        setupRecyclerView()


        fetchLogs()


        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("GMT+3")
        val currentDateAndTime = sdf.format(Date())
        binding.toolbar.findViewById<TextView>(R.id.timestampTextView).text = "$currentDateAndTime"


        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }


        binding.toolbar.findViewById<ImageButton>(R.id.powerConsumptionButton).setOnClickListener {
            findNavController().navigate(R.id.action_logFragment_to_powerConsumptionFragment)
        }

        return binding.root
    }


    private fun setupRecyclerView() {
        logAdapter = LogAdapter(logs)
        binding.logRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.logRecyclerView.adapter = logAdapter
    }


    private fun fetchLogs() {
        apiService.getLogs().enqueue(object : Callback<List<Log>> {
            override fun onResponse(call: Call<List<Log>>, response: Response<List<Log>>) {
                if (response.isSuccessful) {
                    // API'den gelen log verilerini listeye ekliyoruz.
                    logs.clear()
                    logs.addAll(response.body() ?: listOf())
                    // Adapteri bilgilendirerek verilerin güncellenmesini sağlıyoruz.
                    logAdapter.notifyDataSetChanged()
                } else {
                    // API'den gelen hata durumunu kullanıcıya gösteriyoruz.
                    Toast.makeText(context, "Loglar yüklenemedi", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Log>>, t: Throwable) {
                // İstek sırasında bir hata oluştuğunda kullanıcıya gösteriyoruz.
                Toast.makeText(context, "Hata: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
