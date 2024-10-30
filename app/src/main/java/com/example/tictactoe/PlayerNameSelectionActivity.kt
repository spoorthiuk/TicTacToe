package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PlayerNameSelectionActivity:AppCompatActivity() {
    private lateinit var submitButton: Button
    private lateinit var player1:EditText
    private lateinit var player2:EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_players_name)

        submitButton = findViewById(R.id.submitPlayerNamesButton)
        player1 = findViewById(R.id.player1Name)
        player2 = findViewById(R.id.player2Name)

        val player1Name = player1.text
        val player2Name = player2.text

        submitButton.setOnClickListener {
//            if(player1Name.isEmpty() || player2Name.isEmpty()){
//                Toast.makeText(this,"Please Enter Player names",Toast.LENGTH_SHORT).show()
//            }
//            else{
                val intent = Intent(this,GamePlayActivity::class.java)
                startActivity(intent)
            //}
        }

    }
}