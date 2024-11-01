package com.example.tictactoe.data

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tictactoe.R
import com.example.tictactoe.models.LeaderBoardEntryMultiplayer

class LeaderboardMultiplayerAdapter(private var entries: List<LeaderBoardEntryMultiplayer>) :
    RecyclerView.Adapter<LeaderboardMultiplayerAdapter.LeaderboardMultiplayerViewHolder>() {

    class LeaderboardMultiplayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvWinner: TextView = itemView.findViewById(R.id.tvWinner)
        val tvUser1: TextView = itemView.findViewById(R.id.tvUser1)
        val tvUser2: TextView = itemView.findViewById(R.id.tvUser2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardMultiplayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard_multiplayer, parent, false)
        return LeaderboardMultiplayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardMultiplayerViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvDate.text = entry.date
        holder.tvWinner.text = entry.winner
        holder.tvUser1.text = entry.username1
        holder.tvUser2.text = entry.username2
    }

    override fun getItemCount(): Int = entries.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newEntries: List<LeaderBoardEntryMultiplayer>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}
