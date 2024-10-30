package com.example.tictactoe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoe.data.GameState

class MultiplayerTicTacToeGameActivity: AppCompatActivity() {

    private var gameState = GameState()

    // Other UI elements and variables...

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI and start listening for Bluetooth messages
        listenForBluetoothMessages()
    }

    private fun listenForBluetoothMessages() {
        // Listen for incoming messages to update game state
        // This will need to be set up in your Bluetooth controller class
    }
}