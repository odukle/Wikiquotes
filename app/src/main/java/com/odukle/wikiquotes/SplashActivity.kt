package com.odukle.wikiquotes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    companion object {
        lateinit var instance: SplashActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        instance = this

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}