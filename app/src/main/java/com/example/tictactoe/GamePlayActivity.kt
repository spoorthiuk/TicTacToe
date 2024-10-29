package com.example.tictactoe

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class GamePlayActivity:AppCompatActivity() {
    private lateinit var gridBoxes : Array<Array<ImageView>>
    private lateinit var statusTextView:TextView
    private lateinit var settingsButton:ImageButton
    private var selectedDifficulty = "Hard"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        } else checkGameStatus()

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

    private fun getGameStatus(): String
    {
        return if(checkWin("Human"))
            "won"
        else if (checkWin("AI"))
            "lost"
        else "draw"
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

    private fun showDifficultyDialog()
    {
        val difficulties = arrayOf("Easy","Medium","Hard")
        AlertDialog.Builder(this).setTitle("Choose Difficulty Level").setItems(difficulties)
        {_,which -> selectedDifficulty = difficulties[which]
        }.setNegativeButton("Cancel"){ dialog,_ -> dialog.dismiss() }.show()
    }
}