package com.example.tictactoe.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "multi_player_game_results")
data class GameResultMultiplayer(
@PrimaryKey(autoGenerate = true) val id: Long = 0,
val date: String, // You can use LocalDate if you want to store it as an object
val username1: String,
val username2: String,
val winner: String, // Add mode if necessary
)
