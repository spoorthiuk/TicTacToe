package com.example.tictactoe.data

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tictactoe.R
import com.example.tictactoe.models.LeaderboardEntry

class LeaderboardAdapter(private var entries: List<LeaderboardEntry>) :
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvWinner: TextView = itemView.findViewById(R.id.tvWinner)
        val tvUser: TextView = itemView.findViewById(R.id.tvUser)
        val tvDifficulty: TextView = itemView.findViewById(R.id.tvDifficulty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvDate.text = entry.date
        holder.tvWinner.text = entry.winner
        holder.tvUser.text = entry.username
        holder.tvDifficulty.text = entry.difficulty
    }

    override fun getItemCount(): Int = entries.size
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newEntries: List<LeaderboardEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}
