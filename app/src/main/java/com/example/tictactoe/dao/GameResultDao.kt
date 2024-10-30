package com.example.tictactoe.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tictactoe.models.GameResult

@Dao
interface GameResultDao {
    @Insert
    suspend fun insertGameResult(gameResult: GameResult): Long // Ensure this is GameResult
}