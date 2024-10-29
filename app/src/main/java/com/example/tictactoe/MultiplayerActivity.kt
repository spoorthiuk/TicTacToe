package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MultiplayerActivity : AppCompatActivity() {
    private lateinit var singleDeviceButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.multiplayer_options)

        singleDeviceButton = findViewById(R.id.singleDeviceButton)
        val multipleDeviceButton: Button = findViewById(R.id.mutipleDeviceButton)

        singleDeviceButton.setOnClickListener {
            val intent = Intent(this,PlayerNameSelectionActivity::class.java)
            startActivity(intent)
        }

        multipleDeviceButton.setOnClickListener {
            // Start the MultiplayerActivity when the button is clicked
            val intent = Intent(this, MultipleDeviceGameLauncher::class.java)
            startActivity(intent)
        }
    }
}