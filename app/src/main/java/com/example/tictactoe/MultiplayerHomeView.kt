package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MultiplayerHomeView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.multiplayer)
        val multiplayerButton: Button = findViewById(R.id.mutiplayerButton)
        multiplayerButton.setOnClickListener {
            val intent = Intent(this, MultiplayerActivity::class.java)
            startActivity(intent)
        }
    }
}