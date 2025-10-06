package com.example.vnsguattendace.service

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vnsguattendace.R
import com.example.vnsguattendace.model.AttendanceSession
import com.google.android.material.button.MaterialButton

class SessionHistoryAdapter(
    private val sessions: MutableList<AttendanceSession>,
    private val onAction: (AttendanceSession, String) -> Unit
) : RecyclerView.Adapter<SessionHistoryAdapter.SessionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount() = sessions.size

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val courseName: TextView = itemView.findViewById(R.id.tvCourseName)
        private val date: TextView = itemView.findViewById(R.id.tvDate)
        private val stats: TextView = itemView.findViewById(R.id.tvStats)
        private val btnView: MaterialButton = itemView.findViewById(R.id.btnView)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)

        fun bind(session: AttendanceSession) {
            courseName.text = session.courseName
            date.text = session.date
            stats.text = "Total: ${session.totalStudents} | Present: ${session.presentCount} | Absent: ${session.absentCount}"

            btnView.setOnClickListener {
                onAction(session, "view")
            }

            btnDelete.setOnClickListener {
                onAction(session, "delete")
            }
        }
    }
}
