package com.tbmyo.otomasyon
//RecyclerView için günlük kayıtlarının listelenmesini sağlayan adaptör.
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tbmyo.otomasyon.databinding.ItemLogBinding
import com.tbmyo.otomasyon.network.Log
import java.text.SimpleDateFormat
import java.util.*

class LogAdapter(private val logs: List<Log>) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun getItemCount(): Int = logs.size

    inner class LogViewHolder(private val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(log: Log) {
            // Veritabanından gelen timestamp değerini bizim biçime göre formatlayarak göster
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val formattedTimestamp = sdf.format(Date(log.timestamp))

            binding.detailsTextView.text = "${log.user} tarafından ${log.action}."
            binding.timestampTextView.text = formattedTimestamp
        }
    }
}
