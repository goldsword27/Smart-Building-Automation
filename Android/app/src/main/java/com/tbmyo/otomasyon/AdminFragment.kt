package com.tbmyo.otomasyon
//Yönetici arayüzü fragmenti
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log // Bu satırı ekleyin
import android.util.Patterns // Bu satırı ekleyin
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tbmyo.otomasyon.databinding.FragmentAdminBinding
import com.tbmyo.otomasyon.network.ApiClient
import com.tbmyo.otomasyon.network.ApiService
import com.tbmyo.otomasyon.network.User
import com.tbmyo.otomasyon.network.UserRequest
import com.tbmyo.otomasyon.network.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tbmyo.otomasyon.R
import com.tbmyo.otomasyon.UserAdapter

class AdminFragment : Fragment() {

    private lateinit var binding: FragmentAdminBinding
    private lateinit var userAdapter: UserAdapter
    private var users: MutableList<User> = mutableListOf()
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminBinding.inflate(inflater, container, false)
        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)

        // Kullanıcı rolünü SharedPreferences'den okur
        val userRole = getUserRole(requireContext())

        if (userRole != "administrator") {
            Toast.makeText(context, "Bu bölüme erişim yetkiniz yok", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
            return binding.root
        }

        binding.addUserButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            if (email.isNotEmpty() && isValidEmail(email)) {
                showAddUserConfirmationDialog(email)
            } else {
                Toast.makeText(context, "Geçerli bir e-posta adresi giriniz", Toast.LENGTH_SHORT).show()
            }
        }

        // Log butonuna tıklama fonksiyonu
        binding.logButton.setOnClickListener {
            findNavController().navigate(R.id.action_adminFragment_to_logFragment)
        }

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        setupRecyclerView()
        fetchUsers()

        return binding.root
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(users) { user -> showDeleteConfirmationDialog(user) }
        binding.userRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.userRecyclerView.adapter = userAdapter
    }

    private fun fetchUsers() {
        apiService.getUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    users.clear()
                    val allUsers = response.body() ?: listOf()
                    users.addAll(allUsers.filter { it.email != "admin" }) // admin yerine admin kullanıcınızın e-posta adresini yazın
                    userAdapter.notifyDataSetChanged()
                } else {
                    Log.e("AdminFragment", "Kullanıcıları yüklerken hata: ${response.errorBody()?.string()}") // Bu satırı ekleyin
                    Toast.makeText(context, "Kullanıcıları yüklerken hata oluştu", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("AdminFragment", "Kullanıcıları yüklerken hata: ${t.message}") // Bu satırı ekleyin
                Toast.makeText(context, "Kullanıcıları yüklerken hata oluştu", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addUser(email: String) {
        val userRequest = UserRequest(email)
        apiService.addUser(userRequest).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Kullanıcı başarıyla eklendi", Toast.LENGTH_SHORT).show()
                    binding.emailEditText.text.clear() // E-posta alanını temizle
                    fetchUsers() // Kullanıcıları tekrar yükle
                } else {
                    Toast.makeText(context, "Kullanıcı eklenemedi", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(context, "Hata: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddUserConfirmationDialog(email: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Kullanıcı Ekle")
            .setMessage("Bu kullanıcıyı eklemek istediğinizden emin misiniz?")
            .setPositiveButton("Evet") { dialog, _ ->
                addUser(email)
                dialog.dismiss()
            }
            .setNegativeButton("Hayır") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteConfirmationDialog(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Kullanıcı Sil")
            .setMessage("Bu kullanıcıyı silmek istediğinizden emin misiniz?")
            .setPositiveButton("Evet") { dialog, _ ->
                deleteUser(user)
                dialog.dismiss()
            }
            .setNegativeButton("Hayır") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteUser(user: User) {
        apiService.deleteUser(user.id).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Kullanıcı başarıyla silindi", Toast.LENGTH_SHORT).show()
                    fetchUsers() // Kullanıcıları tekrar yükle
                } else {
                    Toast.makeText(context, "Kullanıcı silinemedi", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(context, "Hata: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUserRole(context: Context): String? {
        // Master key
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // Encrypted SharedPreferences
        val sharedPref = EncryptedSharedPreferences.create(
            context,
            "sharedPrefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPref.getString("USER_ROLE", null)
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
