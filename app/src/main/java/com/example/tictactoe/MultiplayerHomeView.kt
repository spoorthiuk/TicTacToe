package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MultiplayerHomeView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.multiplayer)
        val multiplayerButton: Button = findViewById(R.id.mutiplayerButton)
        multiplayerButton.setOnClickListener {
            val intent = Intent(this, MultiplayerActivity::class.java)
            startActivity(intent)
        }
    }
}