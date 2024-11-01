package com.example.tictactoe

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoe.models.FullGameState
import com.example.tictactoe.models.GameState
import com.example.tictactoe.models.Metadata
import com.example.tictactoe.models.MiniGame
import com.example.tictactoe.models.PlayerChoice
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.Executors

class GamePlayAcrossDevicesActivity : AppCompatActivity() {

    companion object {
        var connectedSocket: BluetoothSocket? = null
        var isServer: Boolean = false // true if this device started the server
    }

    private lateinit var gridBoxes: Array<Array<ImageView>>
    private lateinit var statusTextView: TextView
    private var isPlayerTurn = isServer // Server player starts first by default
    private val executor = Executors.newSingleThreadExecutor()
    private var playerSymbol = "X"
    private var opponentSymbol = "O"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.human_vs_human_gameplay)

        statusTextView = findViewById(R.id.statusTextView)
        gridBoxes = arrayOf(
            arrayOf(findViewById(R.id.box1), findViewById(R.id.box2), findViewById(R.id.box3)),
            arrayOf(findViewById(R.id.box5), findViewById(R.id.box6), findViewById(R.id.box4)),
            arrayOf(findViewById(R.id.box9), findViewById(R.id.box7), findViewById(R.id.box8))
        )


        connectedSocket?.let { socket ->
            Toast.makeText(this, "Socket connected: $socket", Toast.LENGTH_LONG).show()

            if (isServer) {
                // Server sends the initial game state
                isPlayerTurn = true
                Toast.makeText(this, "Your turn", Toast.LENGTH_LONG).show()
                sendInitialGameState(socket, isServer = true)
            } else {
                // Client waits to receive the initial game state from the server
                receiveGameState(socket)
            }
            setBoxClickListeners()
        }
    }

    private fun setBoxClickListeners() {
        for (i in gridBoxes.indices) {
            for (j in gridBoxes[i].indices) {
                gridBoxes[i][j].setOnClickListener {
                    Log.d("GamePlay", "gridBoxes[i][j].tag: "+gridBoxes[i][j].tag)
                    if (isPlayerTurn && (gridBoxes[i][j].tag == null || gridBoxes[i][j].tag == " ")) {
                        Log.d("GamePlay", "Player tapped box: [$i][$j]") // Log click event
                        makeMove(i, j, playerSymbol)
                        isPlayerTurn = false
                        sendGameState(getCurrentGameState())
                    } else {
                        Log.d("GamePlay", "Not player’s turn or box already marked.")
                    }
                }
            }
        }
    }


    private fun makeMove(row: Int, col: Int, symbol: String) {
        gridBoxes[row][col].tag = symbol
        Toast.makeText(this, "GRID IMAGE"+symbol,Toast.LENGTH_SHORT).show()
        gridBoxes[row][col].setImageResource(if (symbol == "X") R.drawable.cross else R.drawable.circle)
        gridBoxes[row][col].isClickable = false

        val fullGameState = getCurrentGameState()
        updateBoard(fullGameState)
        checkGameStatus(fullGameState)
    }

    private fun checkGameStatus(fullGameState: FullGameState) {
        if (fullGameState.gameState.winner.isNotBlank()) {
            showGameEndDialog("${fullGameState.gameState.winner} won!")
        } else if (fullGameState.gameState.draw) {
            showGameEndDialog("It's a draw!")
        }
    }

    private fun getCurrentGameState(): FullGameState {
        val board = Array(3) { row ->
            Array(3) { col ->
                gridBoxes[row][col].tag?.toString() ?: " "
            }
        }
        val winner = checkWinner(board)
        val draw = checkDraw(board)

        val gameState = GameState(
            board = board,
            turn = if (isPlayerTurn) "1" else "0",
            winner = winner,
            draw = draw,
            connectionEstablished = true,
            reset = false
        )
        val metadata = Metadata(
            choices = listOf(
                PlayerChoice("player1", "Player 1 MAC Address"),
                PlayerChoice("player2", "Player 2 MAC Address")
            ),
            miniGame = MiniGame("Player 1 MAC Address", "Player 2 MAC Address")
        )
        return FullGameState(gameState, metadata)
    }

    private fun checkWinner(board: Array<Array<String>>): String {
        val lines = listOf(
            listOf(board[0][0], board[0][1], board[0][2]),
            listOf(board[1][0], board[1][1], board[1][2]),
            listOf(board[2][0], board[2][1], board[2][2]),
            listOf(board[0][0], board[1][0], board[2][0]),
            listOf(board[0][1], board[1][1], board[2][1]),
            listOf(board[0][2], board[1][2], board[2][2]),
            listOf(board[0][0], board[1][1], board[2][2]),
            listOf(board[0][2], board[1][1], board[2][0])
        )
        for (line in lines) {
            if (line.all { it == playerSymbol }) return "Player"
            if (line.all { it == opponentSymbol }) return "Opponent"
        }
        return ""
    }

    private fun checkDraw(board: Array<Array<String>>): Boolean {
        return board.all { row -> row.all { it != " " } }
    }

    private fun sendInitialGameState(socket: BluetoothSocket, isServer: Boolean) {
        val initialGameState = getCurrentGameState().apply {
            gameState.turn = if (isServer) "0" else "1"  // "0" if server starts, "1" for client
        }
        sendGameState(initialGameState)
    }

    private fun sendGameState(fullGameState: FullGameState) {
        val socket = connectedSocket
        if (socket == null || !socket.isConnected) {
            Log.e("GamePlay", "Socket is not connected. Unable to send data.")
            runOnUiThread {
                Toast.makeText(this, "Connection lost. Unable to send data.", Toast.LENGTH_SHORT).show()
            }
            return
        }
        fullGameState.gameState.turn = if (isServer) "1" else "0"
        val jsonString = fullGameState.toJson().toString()
        executor.execute {
            try {
                Log.d("GamePlay", "Attempting to send game state: $jsonString")
                socket.outputStream.write(jsonString.toByteArray())
                Log.d("GamePlay", "Game state sent successfully.")
            } catch (e: IOException) {
                Log.e("GamePlay", "Error sending data through socket", e)
                handleConnectionLoss()
            }
        }
    }


    private fun receiveGameState(socket: BluetoothSocket) {
        executor.execute {
            try {
                val inputStream = socket.inputStream
                val buffer = ByteArray(2048)
                while (socket.isConnected) {
                    Log.d("GamePlay", "Waiting to read data...")
                    val bytes = inputStream.read(buffer)
                    if (bytes > 0) {
                        val jsonData = String(buffer, 0, bytes)
                        Log.d("GamePlay", "Data received: $jsonData")
                        val receivedGameState = JSONObject(jsonData).toFullGameState()
                        runOnUiThread {
                            updateBoard(receivedGameState)
                        }
                    } else {
                        Log.e("GamePlay", "Read returned -1, socket might be closed.")
                        handleConnectionLoss()
                        break
                    }
                }
            } catch (e: IOException) {
                Log.e("GamePlay", "Error reading data from socket", e)
                handleConnectionLoss()
            }
        }
    }


    private fun handleConnectionLoss() {
        runOnUiThread {
            Toast.makeText(this, "Connection lost. Returning to main menu.", Toast.LENGTH_SHORT).show()
            finish()  // Go back to the main activity or handle it as needed
        }
    }


    private fun resetGameOnConnectionLoss() {
        // Handle the connection loss (e.g., go back to main menu, reset game state, etc.)
        Toast.makeText(this, "Connection lost. Returning to main menu.", Toast.LENGTH_SHORT).show()
        finish()  // Or navigate back to the main activity
    }


    private fun updateBoard(fullGameState: FullGameState) {
        val gameState = fullGameState.gameState
        for (i in 0..2) {
            for (j in 0..2) {
                val cell = gameState.board[i][j]
                gridBoxes[i][j].tag = cell
                gridBoxes[i][j].setImageResource(
                    when (cell) {
                        playerSymbol -> R.drawable.cross
                        opponentSymbol -> R.drawable.circle
                        else -> 0
                    }
                )
                gridBoxes[i][j].isClickable = cell == " " && isPlayerTurn // Make cells clickable only if empty and it's the player's turn
            }
        }
        // Update turn based on the received game state
        isPlayerTurn = (gameState.turn == "0" && isServer) || (gameState.turn == "1" && !isServer)
        if (isPlayerTurn) {
            Toast.makeText(this, "It's your turn!", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showGameEndDialog(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> resetGame() }
            .show()
    }

    private fun resetGame() {
        for (i in gridBoxes.indices) {
            for (j in gridBoxes[i].indices) {
                gridBoxes[i][j].tag = "grid_box"
                gridBoxes[i][j].setImageResource(0)
                gridBoxes[i][j].isClickable = true
            }
        }
        statusTextView.text = ""
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            connectedSocket?.close()
            Log.d("GamePlay", "Socket closed successfully in onDestroy.")
        } catch (e: IOException) {
            Log.e("GamePlay", "Error closing socket in onDestroy", e)
        }
        executor.shutdown()
    }

    // Convert FullGameState to JSON using org.json
    fun FullGameState.toJson(): JSONObject {
        val gameStateJson = JSONObject().apply {
            put("board", JSONArray(gameState.board.map { JSONArray(it.toList()) }))
            put("turn", gameState.turn)
            put("winner", gameState.winner)
            put("draw", gameState.draw)
            put("connectionEstablished", gameState.connectionEstablished)
            put("reset", gameState.reset)
        }

        val choicesJson = JSONArray(metadata.choices.map {
            JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
            }
        })

        val miniGameJson = JSONObject().apply {
            put("player1Choice", metadata.miniGame.player1Choice)
            put("player2Choice", metadata.miniGame.player2Choice)
        }

        val metadataJson = JSONObject().apply {
            put("choices", choicesJson)
            put("miniGame", miniGameJson)
        }

        return JSONObject().apply {
            put("gameState", gameStateJson)
            put("metadata", metadataJson)
        }
    }

    // Convert JSON to FullGameState using org.json
    fun JSONObject.toFullGameState(): FullGameState {
        val gameStateJson = getJSONObject("gameState")
        val boardArray = gameStateJson.getJSONArray("board")
        val board = Array(3) { row ->
            Array(3) { col -> boardArray.getJSONArray(row).getString(col) }
        }

        val gameState = GameState(
            board = board,
            turn = gameStateJson.getString("turn"),
            winner = gameStateJson.getString("winner"),
            draw = gameStateJson.getBoolean("draw"),
            connectionEstablished = gameStateJson.getBoolean("connectionEstablished"),
            reset = gameStateJson.getBoolean("reset")
        )

        val metadataJson = getJSONObject("metadata")
        val choicesArray = metadataJson.getJSONArray("choices")
        val choices = List(choicesArray.length()) { i ->
            val choiceJson = choicesArray.getJSONObject(i)
            PlayerChoice(
                id = choiceJson.getString("id"),
                name = choiceJson.getString("name")
            )
        }

        val miniGameJson = metadataJson.getJSONObject("miniGame")
        val miniGame = MiniGame(
            player1Choice = miniGameJson.getString("player1Choice"),
            player2Choice = miniGameJson.getString("player2Choice")
        )

        val metadata = Metadata(
            choices = choices,
            miniGame = miniGame
        )

        return FullGameState(
            gameState = gameState,
            metadata = metadata
        )
    }
}
