package com.tbmyo.otomasyon
//Giriş arayüzü
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tbmyo.otomasyon.databinding.FragmentLoginBinding
import com.tbmyo.otomasyon.network.ApiClient
import com.tbmyo.otomasyon.network.ApiService
import com.tbmyo.otomasyon.network.LoginRequest
import com.tbmyo.otomasyon.network.LoginResponse
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private var isPasswordVisible: Boolean = false
    private lateinit var progressDialog: ProgressDialog

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Lütfen bekleyiniz...")
        progressDialog.setCancelable(false)

        binding.button.setOnClickListener {
            val email = binding.editTextTextEmailAddress2.text.toString()
            val password = binding.editTextTextPassword.text.toString()

            if (email.isEmpty()) {
                binding.editTextTextEmailAddress2.error = "E-posta alanı boş olamaz"
            } else if (password.isEmpty()) {
                binding.editTextTextPassword.error = "Şifre alanı boş olamaz"
            } else {
                progressDialog.show()
                login(email, password)
            }
        }

        binding.forgotPasswordTextView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        binding.editTextTextPassword.setOnTouchListener { v, event ->
            val drawableRight = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.editTextTextPassword.right - binding.editTextTextPassword.compoundDrawables[drawableRight].bounds.width())) {
                    togglePasswordVisibility(binding.editTextTextPassword)
                    return@setOnTouchListener true
                }
            }
            false
        }

        return binding.root
    }

    private fun togglePasswordVisibility(passwordEditText: EditText) {
        if (isPasswordVisible) {
            passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0)
        } else {
            passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility, 0)
        }
        isPasswordVisible = !isPasswordVisible
        passwordEditText.setSelection(passwordEditText.text.length)
    }

    private fun login(email: String, password: String) {
        val apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        val loginRequest = LoginRequest(email, password)
        val call = apiService.login(loginRequest)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    // API yanıtını loglayalım
                    println("Login Response: ${loginResponse?.token}, Role: ${loginResponse?.role}")

                    saveUserDetails(loginResponse?.token, loginResponse?.role)
                    Toast.makeText(activity, "Giriş başarılı", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_deviceControlFragment)
                } else {
                    binding.editTextTextEmailAddress2.error = "Geçersiz kullanıcı adı veya şifre"
                    Toast.makeText(activity, "Giriş başarısız", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(activity, "Hata: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserDetails(token: String?, role: String?) {
        // Master key oluşturulması
        val masterKey = MasterKey.Builder(requireContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // EncryptedSharedPreferences kullanımı
        val encryptedSharedPref = EncryptedSharedPreferences.create(
            requireContext(),
            "sharedPrefs", //dosya ismi
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        with(encryptedSharedPref.edit()) {
            putString("TOKEN", token)
            putString("USER_ROLE", role)
            apply()
        }
    }
}
