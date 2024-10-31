package com.example.tictactoe

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PlayerNameSelectionActivity:AppCompatActivity() {
    /**
     * This class file implement the player name
     * input display and as a launcher for the
     * Human VS Human on same device gameplay
     */
    private lateinit var startGameButton: Button
    private lateinit var player1:EditText
    private lateinit var player2:EditText
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_players_name)

        startGameButton = findViewById(R.id.startGameButton)
        player1 = findViewById(R.id.player1Name)
        player2 = findViewById(R.id.player2Name)

        val player1Name = player1.text
        val player2Name = player2.text

        startGameButton.setOnClickListener {
            if(player1Name.isEmpty())
            {
                Toast.makeText(this,"Please Enter Player 1 name",Toast.LENGTH_SHORT).show()
            }
            if(player2Name.isEmpty())
            {
                Toast.makeText(this,"Please Enter Player 2 name",Toast.LENGTH_SHORT).show()
            }
            else
            {
                val intent = Intent(this,HumanVsHumanGamePlayActivity::class.java)
                intent.putExtra("Player1Name",player1Name.toString())
                intent.putExtra("Player2Name",player2Name.toString())
                startActivity(intent)
            }
        }

    }
}