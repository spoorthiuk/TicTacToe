package com.example.tictactoe.data

data class GameState(
    var board: Array<Array<String>> = Array(3) { Array(3) { " " } },
    var turn: String = "1",
    var winner: String = " ",
    var draw: Boolean = false,
    var connectionEstablished: Boolean = true,
    var reset: Boolean = false
)
