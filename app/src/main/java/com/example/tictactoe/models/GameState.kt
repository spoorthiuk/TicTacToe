package com.example.tictactoe.models
import kotlinx.serialization.Serializable

data class GameState(
    val board: Array<Array<String>> = arrayOf(
        arrayOf(" ", " ", " "),
        arrayOf(" ", " ", " ")
    ),
    var turn: String = "0",
    val winner: String = " ",
    val draw: Boolean = false,
    val connectionEstablished: Boolean = true,
    val reset: Boolean = false
)

data class Metadata(
    val choices: List<PlayerChoice>,
    val miniGame: MiniGame
)

data class PlayerChoice(
    val id: String,
    val name: String
)

data class MiniGame(
    val player1Choice: String,
    val player2Choice: String
)

data class FullGameState(
    val gameState: GameState,
    val metadata: Metadata
)


