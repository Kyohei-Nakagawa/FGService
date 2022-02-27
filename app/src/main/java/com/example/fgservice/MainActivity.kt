package com.example.fgservice

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.fgservice.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener{
            Log.d("debug", "onClick")
            val serviceIntent = Intent(this, ForegroundService::class.java)
            Log.d("debug", "makeIntent")
            startForegroundService(serviceIntent)
            Log.d("debug", "endListener")
        }

    }
}
