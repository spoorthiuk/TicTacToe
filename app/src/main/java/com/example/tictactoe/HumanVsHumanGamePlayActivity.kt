package com.example.tictactoe

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class HumanVsHumanGamePlayActivity: AppCompatActivity()
{
    private lateinit var gridBoxes:Array<Array<ImageView>>
    private lateinit var player1Name:TextView
    private lateinit var player2Name:TextView
    private var firstPlayerName:String = null.toString()
    private lateinit var currentPlayer:String
    private var hasPlayerWon = false
    private lateinit var settingsButton:ImageButton
    private lateinit var gameStatusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.human_vs_human_gameplay)
        player1Name = findViewById(R.id.humanPlayer1)
        player2Name = findViewById(R.id.humanPlayer2)
        settingsButton = findViewById(R.id.icon_button)
        gameStatusTextView = findViewById(R.id.statusTextView)
        player1Name.text = intent.getStringExtra("Player1Name").toString()
        player2Name.text = intent.getStringExtra("Player2Name").toString()

        gridBoxes = arrayOf(
            arrayOf(findViewById(R.id.box1), findViewById(R.id.box2), findViewById(R.id.box3)),
            arrayOf(findViewById(R.id.box5),findViewById(R.id.box6),findViewById(R.id.box4)),
            arrayOf(findViewById(R.id.box9),findViewById(R.id.box7),findViewById(R.id.box8)))

        showWhoGoesFirstDialog(player1Name.text.toString(),player2Name.text.toString())

        initializeBoard()
        settingsButton.setOnClickListener {
            showWhoGoesFirstDialog(player1Name.text.toString(),player2Name.text.toString())
        }
    }

        private fun showWhoGoesFirstDialog(player1Name: String, player2Name: String)
        {
            val players = arrayOf(player1Name,player2Name,"Reset")
            AlertDialog.Builder(this).setTitle("Who Goes First").setItems(players)
            { _,which ->
                when(which){
                   in 0..1->{
                       firstPlayerName = players[which]
                   }
                    2 ->{
                        hasPlayerWon = false
                        resetGame()
                    }

                }
                currentPlayer = firstPlayerName
            }
                .setNegativeButton("Cancel"){ dialog,_ -> dialog.dismiss() }.show()
        }

    private fun resetGame()
    {
        for (i in 0..2)
        {
            for (j in 0..2)
            {
                gridBoxes[i][j].tag = "grid_box"
                gridBoxes[i][j].setImageResource(R.drawable.grid_box)
                gridBoxes[i][j].isClickable = true
                gameStatusTextView.text = ""
            }
        }
    }

    private fun initializeBoard()
    {
        for (i in 0..2)
        {
            for (j in 0..2)
            {
                gridBoxes[i][j].tag = "grid_box"
                gridBoxes[i][j].setOnClickListener{
                    if (hasPlayerWon) return@setOnClickListener
                    makeMove(i,j,gridBoxes)
                    if(checkWin(currentPlayer))
                    {
                        hasPlayerWon = true
                        gameStatusTextView.text = "${currentPlayer} Wins!"
                        return@setOnClickListener
                    }
                    if(currentPlayer == player1Name.text.toString())
                        currentPlayer = player2Name.text.toString()
                    else
                        currentPlayer = player1Name.text.toString()
                }
            }
            if (hasPlayerWon){
                break
            }
        }
    }

    private fun makeMove(i: Int, j: Int, gridBoxes: Array<Array<ImageView>>)
    {
        if(currentPlayer == player1Name.text.toString() && gridBoxes[i][j].tag == "grid_box")
        {
            gridBoxes[i][j].setImageResource(R.drawable.cross)
            gridBoxes[i][j].tag = currentPlayer
        }
        if(currentPlayer == player2Name.text.toString() && gridBoxes[i][j].tag == "grid_box")
        {
            gridBoxes[i][j].setImageResource(R.drawable.circle)
            gridBoxes[i][j].tag = currentPlayer
        }
        gridBoxes[i][j].isClickable = false
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
}