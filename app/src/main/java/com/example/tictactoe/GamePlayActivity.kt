package com.example.tictactoe

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoe.dao.AppDatabase
import com.example.tictactoe.models.GameResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class GamePlayActivity:AppCompatActivity() {
    private lateinit var gridBoxes : Array<Array<ImageView>>
    private lateinit var statusTextView:TextView
    private lateinit var settingsButton:ImageButton
    private var selectedDifficulty = "Hard"
    private lateinit var db: AppDatabase
    private lateinit var playerIndicator: ImageView
    private lateinit var aiIndicator: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = AppDatabase.getDatabase(this)
        selectedDifficulty = intent.getStringExtra("DIFFICULTY") ?: "Hard"

        setContentView(R.layout.game_play3)
        statusTextView = findViewById(R.id.statusTextView)
        gridBoxes = arrayOf(
            arrayOf(findViewById(R.id.box1), findViewById(R.id.box2), findViewById(R.id.box3)),
            arrayOf(findViewById(R.id.box5),findViewById(R.id.box6),findViewById(R.id.box4)),
            arrayOf(findViewById(R.id.box9),findViewById(R.id.box7),findViewById(R.id.box8))
        )
        initializeBoard()
        settingsButton = findViewById(R.id.icon_button)
        settingsButton.setOnClickListener{
            showDifficultyDialog()
        }
        playerIndicator = findViewById(R.id.player)
        aiIndicator = findViewById(R.id.ai)

    }

    private fun initializeBoard()
    {
        for (i in 0..2 )
        {
            for (j in 0..2)
            {
                gridBoxes[i][j].tag = "grid_box"
                gridBoxes[i][j].tag = "grid_box"
                gridBoxes[i][j].setOnClickListener{
                    if(gridBoxes[i][j].tag == "grid_box")
                    {
                        makeMove(i,j,"Human")
                        if(!isGameOver())
                        {
                            val aiMove = getAIMove(gridBoxes)
                            makeMove(aiMove.first,aiMove.second,"AI")
                        } else{
                            checkGameStatus()
                            // Remove onClickListeners on gridBoxes
                            for (i in 0..2) {
                                for (j in 0..2) {
                                    gridBoxes[i][j].setOnClickListener(null)
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private fun checkGameStatus()
    {
        val status = getGameStatus()
        when (status)
        {
            "won" -> showStatusMessage("You won!")
            "lost" -> showStatusMessage("You lost!")
            "draw" -> showStatusMessage("Draw!")
        }
    }

    private fun showStatusMessage(message: String)
    {
        statusTextView.text = message
        statusTextView.visibility = TextView.VISIBLE
    }

    private fun getGameStatus(): String {
        val gameStatus: String
        val username = intent.getStringExtra("USERNAME")

        // Determine the game status
        gameStatus = when {
            checkWin("Human") -> {
                if (username != null) {
                    saveGameResult("Human", username)
                } // Save the result to the database
                "won"
            }
            checkWin("AI") -> {
                if (username != null) {
                    saveGameResult("AI", username)
                } // Save the result to the database
                "lost"
            }
            else -> {
                if (username != null) {
                    saveGameResult("Draw", username)
                } // You might need to define how to handle a draw
                "draw"
            }
        }

        return gameStatus
    }

    private fun isGameOver(): Boolean
    {
        return checkWin("Human") || checkWin("AI") || getAvailableMoves(gridBoxes).isEmpty()
    }

    private fun checkWin(player: String): Boolean
    {
        for (i in 0..2)
        {
            if (gridBoxes[i][0].tag == player && gridBoxes[i][1].tag == player && gridBoxes[i][2].tag == player) return true
            if (gridBoxes[0][i].tag == player && gridBoxes[1][i].tag == player && gridBoxes[2][i].tag == player) return true
        }
        if (gridBoxes[0][0].tag == player && gridBoxes[1][1].tag == player && gridBoxes[2][2].tag == player) return true
        if (gridBoxes[0][2].tag == player && gridBoxes[1][1].tag == player && gridBoxes[2][0].tag == player) return true
        return false
    }

    private fun getAIMove(gridBoxes: Array<Array<ImageView>>): Pair<Int,Int>
    {
        return when (selectedDifficulty) {
            "Easy" -> getRandomMove(gridBoxes)
            "Medium" -> getMediumMove(gridBoxes)
            "Hard" -> findBestMove(gridBoxes) // Uses minimax (already implemented)
            else -> findBestMove(gridBoxes)
        }
    }

    private fun getRandomMove(gridBoxes: Array<Array<ImageView>>): Pair<Int, Int>
    {
        val listOfMoves:List<Pair<Int,Int>> = getAvailableMoves(gridBoxes)
        return listOfMoves.random()
    }

    private fun getMediumMove(gridBoxes: Array<Array<ImageView>>): Pair<Int, Int>
    {
        return if ((0..1).random() == 0) getRandomMove(gridBoxes)  else findBestMove(gridBoxes)
    }

    private fun findBestMove(gridBoxes: Array<Array<ImageView>>): Pair<Int, Int>
    {
        var bestScore = Int.MIN_VALUE
        var bestMove = Pair(-1, -1)

        for (move in getAvailableMoves(gridBoxes))
        {
            gridBoxes[move.first][move.second].tag = "AI"
            val score = minimax(gridBoxes, 0, false, Int.MIN_VALUE, Int.MAX_VALUE)
            gridBoxes[move.first][move.second].tag = "grid_box"

            if (score > bestScore)
            {
                bestScore = score
                bestMove = move
            }
        }
        return bestMove
    }

    private fun minimax(gridBoxes: Array<Array<ImageView>>, depth: Int, isMaximizing: Boolean, alpha: Int, beta: Int): Int
    {
        if (checkWin("AI")) return 10 - depth
        if (checkWin("Human")) return depth - 10
        if (getAvailableMoves(gridBoxes).isEmpty()) return 0
        var bestScore = if (isMaximizing) Int.MIN_VALUE else Int.MAX_VALUE
        var a = alpha
        var b = beta
        for (move in getAvailableMoves(gridBoxes))
        {
            gridBoxes[move.first][move.second].tag = if (isMaximizing) "AI" else "Human"
            val score = minimax(gridBoxes, depth + 1, !isMaximizing, a, b)
            gridBoxes[move.first][move.second].tag = "grid_box"

            if (isMaximizing)
            {
                bestScore = maxOf(bestScore, score)
                a = maxOf(a, score)
            } else
            {
                bestScore = minOf(bestScore, score)
                b = minOf(b, score)
            }
            if (b <= a) break
        }
        return bestScore
    }


    private fun getAvailableMoves(gridBoxes: Array<Array<ImageView>>): List<Pair<Int, Int>>
    {
        val availableMoves = mutableListOf<Pair<Int, Int>>()
        for(i in 0..2)
        {
            for (j in 0..2)
            {
                if(gridBoxes[i][j].tag == "grid_box")
                {
                    availableMoves.add(Pair(i,j))
                }
            }
        }
        return availableMoves
    }

    private fun makeMove(i: Int, j: Int, player: String)
    {
        gridBoxes[i][j].tag = player
        if (player == "Human")  gridBoxes[i][j].setImageResource(R.drawable.cross) else gridBoxes[i][j].setImageResource(R.drawable.circle)
        gridBoxes[i][j].isClickable = false
    }


    private fun showDifficultyDialog() {
        val options = arrayOf("Easy", "Medium", "Hard", "Reset")
        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setItems(options) { _, which ->
                when (which) {
                    in 0..2 -> {
                        selectedDifficulty = options[which]
                        resetGame()
                    }
                    // Update difficulty
                    3 -> resetGame() // Reset the game
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun resetGame() {
        for (i in 0..2) {
            for (j in 0..2) {
                gridBoxes[i][j].tag = "grid_box"
                gridBoxes[i][j].setImageResource(0) // Clear the images
                gridBoxes[i][j].isClickable = true // Make the boxes clickable again
            }
        }
        statusTextView.text = "" // Clear the status message
        statusTextView.visibility = TextView.GONE // Hide the status text view
    }

    private fun saveGameResult(winner: String, username: String) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val difficulty = selectedDifficulty
        val mode = "Single Player"
        val gameResult = GameResult(date = currentDate, username = username, winner = winner, difficulty = difficulty, mode = mode)

        // Step 3: Insert the game result in the database using a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.gameResultDao().insertGameResult(gameResult)
                // Provide feedback to the user (on the main thread)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@GamePlayActivity, "Game result saved!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle any errors
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@GamePlayActivity, "Error saving result: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}