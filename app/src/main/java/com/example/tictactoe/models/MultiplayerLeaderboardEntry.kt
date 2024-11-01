package com.example.tictactoe.models

data class MultiplayerLeaderboardEntry(
    /**
     * Data class for Multiplayer player Leaderboard
     */
    val date: String,
    val winner: String,
    val username1: String,
    val username2: String
)
