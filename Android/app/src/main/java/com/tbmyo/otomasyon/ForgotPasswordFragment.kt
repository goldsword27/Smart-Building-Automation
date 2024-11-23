package com.tbmyo.otomasyon
//Şifremi unuttum arayüzü
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tbmyo.otomasyon.databinding.FragmentForgotPasswordBinding
import com.tbmyo.otomasyon.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordFragment : Fragment() {
    private lateinit var binding: FragmentForgotPasswordBinding
    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)

        binding.backArrow.setOnClickListener {
            findNavController().navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
        }

        binding.sendButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            if (email.isEmpty()) {
                binding.emailEditText.error = "E-posta alanı boş olamaz"
            } else {
                sendVerificationCode(email)
                binding.loadingTextView.visibility = View.VISIBLE
            }
        }

        binding.verifyButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val code = getEnteredCode()
            if (email.isEmpty() || code.length != 6) {
                Toast.makeText(activity, "Geçerli bir kod girin", Toast.LENGTH_SHORT).show()
            } else {
                verifyCode(email, code)
            }
        }

        binding.changePasswordButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val code = getEnteredCode()
            val newPassword = binding.newPasswordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(activity, "Şifre alanlarını doldurun", Toast.LENGTH_SHORT).show()
            } else if (newPassword != confirmPassword) {
                Toast.makeText(activity, "Şifreler eşleşmiyor", Toast.LENGTH_SHORT).show()
            } else {
                resetPassword(email, code, newPassword)
            }
        }

        setUpCodeInput()
        return binding.root
    }

    private fun setUpCodeInput() {
        val editTexts = listOf(
            binding.codeEditText1,
            binding.codeEditText2,
            binding.codeEditText3,
            binding.codeEditText4,
            binding.codeEditText5,
            binding.codeEditText6
        )

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        if (i < editTexts.size - 1) {
                            editTexts[i + 1].requestFocus()
                        }
                    } else if (s?.length == 0) {
                        if (i > 0) {
                            editTexts[i - 1].requestFocus()
                        }
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun sendVerificationCode(email: String) {
        val apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        val forgotPasswordRequest = ForgotPasswordEmailRequest(email)
        val call = apiService.forgotPassword(forgotPasswordRequest)

        call.enqueue(object : Callback<ForgotPasswordEmailResponse> {
            override fun onResponse(call: Call<ForgotPasswordEmailResponse>, response: Response<ForgotPasswordEmailResponse>) {
                binding.loadingTextView.visibility = View.GONE
                if (response.isSuccessful) {
                    val forgotPasswordResponse = response.body()
                    Toast.makeText(activity, "${forgotPasswordResponse?.message}", Toast.LENGTH_SHORT).show()
                    binding.codeInputLayout.visibility = View.VISIBLE
                    binding.verifyButton.visibility = View.VISIBLE
                    binding.timerTextView.visibility = View.VISIBLE
                    startCountDownTimer()
                } else {
                    binding.emailEditText.error = "E-posta bulunamadı"
                }
            }

            override fun onFailure(call: Call<ForgotPasswordEmailResponse>, t: Throwable) {
                binding.loadingTextView.visibility = View.GONE
                Toast.makeText(activity, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun verifyCode(email: String, code: String) {
        val apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        val verifyCodeRequest = VerifyCodeRequest(email, code)
        val call = apiService.verifyCode(verifyCodeRequest)

        call.enqueue(object : Callback<VerifyCodeResponse> {
            override fun onResponse(call: Call<VerifyCodeResponse>, response: Response<VerifyCodeResponse>) {
                if (response.isSuccessful) {
                    binding.newPasswordEditText.visibility = View.VISIBLE
                    binding.confirmPasswordEditText.visibility = View.VISIBLE
                    binding.changePasswordButton.visibility = View.VISIBLE
                    binding.codeInputLayout.visibility = View.GONE
                    binding.verifyButton.visibility = View.GONE
                    binding.timerTextView.visibility = View.GONE
                    binding.sifrekriter.visibility = View.VISIBLE
                    countDownTimer?.cancel()
                } else {
                    Toast.makeText(activity, "Geçersiz doğrulama kodu", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VerifyCodeResponse>, t: Throwable) {
                Toast.makeText(activity, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun resetPassword(email: String, code: String, newPassword: String) {
        val apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        val resetPasswordRequest = ResetPasswordRequest(email, code, newPassword)
        val call = apiService.resetPassword(resetPasswordRequest)

        call.enqueue(object : Callback<ResetPasswordResponse> {
            override fun onResponse(call: Call<ResetPasswordResponse>, response: Response<ResetPasswordResponse>) {
                if (response.isSuccessful) {
                    val resetPasswordResponse = response.body()
                    Toast.makeText(activity, "${resetPasswordResponse?.message}", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
                } else {
                    Toast.makeText(activity, "Şifre sıfırlama başarısız", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResetPasswordResponse>, t: Throwable) {
                Toast.makeText(activity, "Hata: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getEnteredCode(): String {
        return binding.codeEditText1.text.toString() +
                binding.codeEditText2.text.toString() +
                binding.codeEditText3.text.toString() +
                binding.codeEditText4.text.toString() +
                binding.codeEditText5.text.toString() +
                binding.codeEditText6.text.toString()
    }

    private fun startCountDownTimer() {
        countDownTimer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (isAdded) {
                    val minutes = (millisUntilFinished / 1000) / 60
                    val seconds = (millisUntilFinished / 1000) % 60
                    binding.timerTextView.text = String.format("%02d:%02d", minutes, seconds)
                }
            }

            override fun onFinish() {
                if (isAdded) {
                    binding.timerTextView.text = "00:00"
                    Toast.makeText(activity, "Kod süresi doldu, lütfen tekrar gönderin", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp() // Bir önceki fragmana geri dön
                }
            }
        }.start()
    }

    override fun onPause() {
        super.onPause()
        countDownTimer?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}