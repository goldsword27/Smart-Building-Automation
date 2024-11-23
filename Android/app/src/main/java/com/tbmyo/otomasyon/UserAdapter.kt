package com.tbmyo.otomasyon
//recyleview için Kullanıcıların listelenmesini sağlayan adaptör
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tbmyo.otomasyon.databinding.ItemUserBinding
import com.tbmyo.otomasyon.network.User

class UserAdapter(private val users: List<User>, private val onDeleteClick: (User) -> Unit) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position], onDeleteClick)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User, onDeleteClick: (User) -> Unit) {
            binding.emailTextView.text = user.email
            binding.deleteButton.setOnClickListener { onDeleteClick(user) }
        }
    }
}
