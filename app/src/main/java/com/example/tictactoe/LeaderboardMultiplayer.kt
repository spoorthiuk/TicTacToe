package com.example.tictactoe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tictactoe.dao.AppDatabase
import com.example.tictactoe.dao.GameResultDao
import com.example.tictactoe.data.LeaderboardAdapter
import com.example.tictactoe.data.LeaderboardMultiplayerAdapter
import com.example.tictactoe.models.LeaderBoardEntryMultiplayer
import com.example.tictactoe.models.LeaderboardEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LeaderboardMultiplayer : AppCompatActivity() {
    /**
     * This class implements the leaderboard to display past games
     * in single player mode
     */
    private lateinit var leaderboardAdapter: LeaderboardMultiplayerAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplaye_leaderboard_view)

        val recyclerView = findViewById<RecyclerView>(R.id.rvLeaderboard)
        recyclerView.layoutManager = LinearLayoutManager(this)
        leaderboardAdapter = LeaderboardMultiplayerAdapter(emptyList())
        recyclerView.adapter = leaderboardAdapter

        // Initialize the database and DAO
        db = AppDatabase.getDatabase(this)

        // Fetch data in a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            val gameResults = db.gameResultDao().getMultiplayerGameResults()
            val leaderboardEntries = gameResults.map { gameResult ->
                LeaderBoardEntryMultiplayer(gameResult.date, gameResult.winner, gameResult.username1, gameResult.username2)
            }

            // Update the RecyclerView on the main thread
            withContext(Dispatchers.Main) {
                leaderboardAdapter.updateData(leaderboardEntries)
            }
        }
    }
}
