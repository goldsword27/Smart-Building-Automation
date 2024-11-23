package com.tbmyo.otomasyon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gece modu devre dışı
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setContentView(R.layout.activity_main)
    }

    //başarıyla giriş yapılıp cihazkontrol arayüzüne girince ana ekran cihaz kontrol gibi davranır.
    //Geri tuşuna basınca tekrar login ekranına dönmez.
    override fun onBackPressed() {

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment

        if (currentFragment is DeviceControlFragment) {

            showExitConfirmationDialog()
        } else {
            super.onBackPressed()
        }
    }
//Uygulamadan çıkmak için sorulan soru
    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setMessage("Uygulamadan çıkmak istiyor musunuz?")
            .setPositiveButton("Evet") { dialog, which ->
                finishAffinity() // Uygulamayı kapat
            }
            .setNegativeButton("Hayır", null)
            .show()
    }
}
