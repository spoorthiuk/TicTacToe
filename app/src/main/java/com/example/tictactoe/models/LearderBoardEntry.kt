package com.example.tictactoe.models

data class LeaderboardEntry(
    /**
     * Data class for Single player Leaderboard
     */
    val date: String,
    val winner: String,
    val username: String,
    val difficulty: String
)