package com.example.tictactoe

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SinglePlayerActivity : AppCompatActivity() {
    /**
     * This activity lets the user enter their username and
     * open the leaderboard
     */
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_single_player)
        val startGameButton = findViewById<Button>(R.id.startGameButton)
        val playerNameEditText = findViewById<EditText>(R.id.playerNameEditText)
        val difficultySpinner = findViewById<Spinner>(R.id.difficulty_spinner)
        startGameButton.setOnClickListener {
            val username = playerNameEditText.text.toString()
            val selectedDifficulty = difficultySpinner.selectedItem.toString()
            val singlePlayerIntent = Intent(this, MainActivity::class.java).apply {
                putExtra("USERNAME", username)
                putExtra("DIFFICULTY", selectedDifficulty)
            }
            startActivity(singlePlayerIntent)
        }
        val leaderboardButton = findViewById<Button>(R.id.leaderboardButton)
        leaderboardButton.setOnClickListener {
            // Open LeaderBoardActivity
            val intent = Intent(this, LeaderboardView::class.java)
            intent.putExtra("GameMode","Singleplayer")
            startActivity(intent)
        }
    }
}