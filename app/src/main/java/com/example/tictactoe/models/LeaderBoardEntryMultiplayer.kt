package com.example.tictactoe.models

data class LeaderBoardEntryMultiplayer(
    /**
     * Data class for Single player Leaderboard
     */
    val date: String,
    val winner: String,
    val username1: String,
    val username2: String
)