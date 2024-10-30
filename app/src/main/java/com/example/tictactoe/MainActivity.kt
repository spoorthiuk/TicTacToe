package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var button2 = findViewById<Button>(R.id.button2)
        /* ai button navigation
        var button1 = findViewById<Button>(R.id.button)

        button1.setOnClickListener {
            // val intent1 = Intent (this, ) | for AI
            startActivity(intent1)
        }
        */
        // multiplayer button navigation
        button2.setOnClickListener {
            val intent1 = Intent (this, MultiplayerActivity::class.java)
            startActivity(intent1)
        }
    }
}