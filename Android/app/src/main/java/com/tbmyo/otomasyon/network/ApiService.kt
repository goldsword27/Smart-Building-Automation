package com.tbmyo.otomasyon.network
//Retrofit ile kullanılan HTTP metodlarını belirtir.

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName

data class ForgotPasswordEmailRequest(val email: String)
data class ForgotPasswordEmailResponse(val message: String)
data class ForgotPasswordRequest(val email: String)
data class ForgotPasswordResponse(val message: String)
data class LoginResponse(val token: String, val role: String)
data class ResetPasswordRequest(val email: String, val code: String, val newPassword: String)
data class LoginRequest(val email: String, val password: String)
data class ResetPasswordResponse(val message: String)
data class VerifyCodeRequest(val email: String, val code: String)
data class VerifyCodeResponse(val message: String)
data class UserRequest(val email: String)
data class UserResponse(val message: String)
data class User(val id: Int, val email: String, val password: String, val role: String)
data class Log(val id: Int, val user: String, val device: String, val action: String, val timestamp: String)
data class DeviceControlRequest(val status: String, val value: String)
data class DeviceResponse(val success: Boolean, val message: String)
data class DeviceStatusResponse(val devices: List<Device>)
data class Device(
    val id: Int,
    val name: String,
    val type: String,
    var status: String,
    val value: String = "",
    val powerConsumption: String = "",
    val temperature: String = "",
    val lightLevel: String = "",
    val smokeLevel: String = ""
)




interface ApiService {

    @POST("devices/control/{id}")
    fun controlDevice(@Path("id") deviceId: Int, @Body request: DeviceControlRequest): Call<DeviceResponse>

    @GET("devices/status")
    fun getDeviceStatuses(): Call<DeviceStatusResponse>

    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("auth/forgot-password")
    fun forgotPassword(@Body forgotPasswordRequest: ForgotPasswordEmailRequest): Call<ForgotPasswordEmailResponse>

    @POST("auth/verify-code")
    fun verifyCode(@Body verifyCodeRequest: VerifyCodeRequest): Call<VerifyCodeResponse>

    @POST("auth/reset-password")
    fun resetPassword(@Body resetPasswordRequest: ResetPasswordRequest): Call<ResetPasswordResponse>

    @POST("auth/add-user")
    fun addUser(@Body userRequest: UserRequest): Call<UserResponse>

    @GET("auth/users")
    fun getUsers(): Call<List<User>>

    @DELETE("auth/delete-user/{id}")
    fun deleteUser(@Path("id") id: Int): Call<UserResponse>

    @GET("logs")
    fun getLogs(): Call<List<Log>>
}
