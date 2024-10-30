package com.example.tictactoe.models
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_results")
data class GameResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // You can use LocalDate if you want to store it as an object
    val winner: String, // Include username if the winner is Human
    val difficulty: String,
    val mode: String // Add mode if necessary
)