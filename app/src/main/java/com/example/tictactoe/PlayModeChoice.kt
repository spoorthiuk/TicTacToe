package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tictactoe.dao.AppDatabase

class PlayModeChoice : AppCompatActivity() {
    /**
     * This class enables the player to navigate to the
     * various game modes.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play_mode_choice)

        // Get the Single Player button by ID and set an OnClickListener
        val singlePlayerButton = findViewById<Button>(R.id.singlePlayerButton)
        singlePlayerButton.setOnClickListener {
            val singlePlayerIntent = Intent(this, SinglePlayerActivity::class.java)
            startActivity(singlePlayerIntent)
        }

        // Get the Multiplayer button by ID and set an OnClickListener
        val multiplayerButton = findViewById<Button>(R.id.multiplayerButton)
        multiplayerButton.setOnClickListener {
            val multiplayerIntent = Intent(this, MultiplayerActivity::class.java)
            startActivity(multiplayerIntent)
        }

    }
}