package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MultiplayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.multiplayer_options)
        val mutipleDeviceButton: Button = findViewById(R.id.mutipleDeviceButton)
        mutipleDeviceButton.setOnClickListener {
            // Start the MultiplayerActivity when the button is clicked
            val intent = Intent(this, MultipleDeviceGameLauncher::class.java)
            startActivity(intent)
        }
    }
}