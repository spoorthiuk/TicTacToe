package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private var board = Array(3) { CharArray(3) { ' ' } }
    private val HUMAN = 'X'
    private val AI = 'O'
    private lateinit var buttons: Array<Array<Button>>
    private var difficulty: String = "Hard" // Default difficulty
    val handler = Handler(Looper.getMainLooper())
    private lateinit var statusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttons = arrayOf(
            arrayOf(
                findViewById(R.id.button00),
                findViewById(R.id.button01),
                findViewById(R.id.button02)
            ),
            arrayOf(
                findViewById(R.id.button10),
                findViewById(R.id.button11),
                findViewById(R.id.button12)
            ),
            arrayOf(
                findViewById(R.id.button20),
                findViewById(R.id.button21),
                findViewById(R.id.button22)
            )
        )
        setupDifficultySpinner()

        initBoard()

        findViewById<Button>(R.id.restartButton).setOnClickListener {
            resetBoard()
            statusTextView.visibility = TextView.GONE
        }

        statusTextView = findViewById(R.id.gameStatusTextView)

        val button = findViewById<Button>(R.id.buttonToHumanVsHumanGameplay)

        button.setOnClickListener {
            val intent = Intent(this, MultiplayerActivity::class.java)
            startActivity(intent)
        }

    }

    // Set up the difficulty spinner
    private fun setupDifficultySpinner() {
        val spinner: Spinner = findViewById(R.id.difficulty_spinner)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.difficulty_levels,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Listen for user selection
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                difficulty = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // Easy: Random move
    private fun getRandomMove(board: Array<CharArray>): Pair<Int, Int> {
        val availableMoves = getAvailableMoves(board)
        return availableMoves.random() // Choose a random move
    }

    // Medium: 50% chance of random, 50% chance of optimal
    private fun getMediumMove(board: Array<CharArray>): Pair<Int, Int> {
        return if ((0..1).random() == 0) {
            getRandomMove(board) // 50% chance of random move
        } else {
            findBestMove(board)  // 50% chance of optimal move (minimax)
        }
    }

    // Function to get the AI move based on the selected difficulty
    private fun getAIMove(board: Array<CharArray>): Pair<Int, Int> {
        return when (difficulty) {
            "Easy" -> getRandomMove(board)
            "Medium" -> getMediumMove(board)
            "Hard" -> findBestMove(board) // Uses minimax (already implemented)
            else -> findBestMove(board)
        }
    }


    private fun initBoard() {
        for (i in 0..2) {
            for (j in 0..2) {
                buttons[i][j].text = ""
                buttons[i][j].setOnClickListener {
                    if (buttons[i][j].text == "")
                    {
                        makeMove(i, j, HUMAN)
                        if (!isGameOver()) {
                            val aiMove = getAIMove(board)
                            handler.postDelayed({
                                makeMove(aiMove.first, aiMove.second, AI)
                            }, 1000)
                        }
                        else
                        {
                            checkGameStatus()
                        }
                    }
                }
            }
        }
    }

    //Set the bord for New Game
    private fun resetBoard() {
        board = Array(3) { CharArray(3) { ' ' } }
        initBoard()
    }

    //Execute the best move
    private fun makeMove(row: Int, col: Int, player: Char) {
        board[row][col] = player
        buttons[row][col].text = player.toString()
        buttons[row][col].isClickable = false
    }

    //Check if Human or AI won or if there are no moves left
    private fun isGameOver(): Boolean {
        return checkWin(HUMAN) || checkWin(AI) || getAvailableMoves(board).isEmpty()
    }

    // Check all possible win states
    private fun checkWin(player: Char): Boolean {
        for (i in 0..2) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) return true
            if (board[0][i] == player && board[1][i] == player && board[2][i] == player) return true
        }
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) return true
        if (board[0][2] == player && board[1][1] == player && board[2][0] == player) return true
        return false
    }

    // Minimax with Alpha-Beta Pruning
    private fun minimax(board: Array<CharArray>, depth: Int, isMaximizing: Boolean, alpha: Int, beta: Int): Int
    {
        if (checkWin(AI)) return 10 - depth
        if (checkWin(HUMAN)) return depth - 10
        if (getAvailableMoves(board).isEmpty()) return 0

        var bestScore = if (isMaximizing) Int.MIN_VALUE else Int.MAX_VALUE
        var a = alpha
        var b = beta

        for (move in getAvailableMoves(board)) {
            board[move.first][move.second] = if (isMaximizing) AI else HUMAN
            val score = minimax(board, depth + 1, !isMaximizing, a, b)
            board[move.first][move.second] = ' '

            if (isMaximizing) {
                bestScore = maxOf(bestScore, score)
                a = maxOf(a, score)
            } else {
                bestScore = minOf(bestScore, score)
                b = minOf(b, score)
            }

            if (b <= a) break
        }
        return bestScore
    }

    private fun findBestMove(board: Array<CharArray>): Pair<Int, Int>
    {
        var bestScore = Int.MIN_VALUE
        var bestMove = Pair(-1, -1)

        for (move in getAvailableMoves(board)) {
            board[move.first][move.second] = AI
            val score = minimax(board, 0, false, Int.MIN_VALUE, Int.MAX_VALUE)
            board[move.first][move.second] = ' '

            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        return bestMove
    }

    //Find all possible moves
    private fun getAvailableMoves(board: Array<CharArray>): List<Pair<Int, Int>>
    {
        val moves = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == ' ') {
                    moves.add(Pair(i, j))
                }
            }
        }
        return moves
    }

    // Update the UI based on the Game Status
    private fun checkGameStatus()
    {
        val status = getGameStatus()
        when (status) {
            "won" -> showStatusMessage("You won!")
            "lost" -> showStatusMessage("You lost!")
            "draw" -> showStatusMessage("Draw!")
        }
    }

    // Update the TextView with the message and make it visible
    private fun showStatusMessage(message: String) {
        statusTextView.text = message
        statusTextView.visibility = TextView.VISIBLE
    }

    private fun getGameStatus(): String {
        if(checkWin(HUMAN)) return "won"
        else if (checkWin(AI)) return "lost"
        else return "draw"
    }

}
