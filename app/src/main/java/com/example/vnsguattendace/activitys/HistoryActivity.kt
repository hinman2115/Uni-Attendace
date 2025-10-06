package com.example.vnsguattendace.activitys

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vnsguattendace.model.AttendanceSession
import com.example.vnsguattendace.R
import com.example.vnsguattendace.service.SessionHistoryAdapter
import com.example.vnsguattendace.service.SessionManager
import com.google.android.material.appbar.MaterialToolbar

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: SessionHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerViewHistory)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Attendance History"

        toolbar.setNavigationOnClickListener {
            finish()
        }

        sessionManager = SessionManager(this)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadSessions()
    }

    private fun loadSessions() {
        val sessions = sessionManager.getAllSessions()

        if (sessions.isEmpty()) {
            Toast.makeText(this, "No saved sessions yet", Toast.LENGTH_SHORT).show()
        }

        adapter = SessionHistoryAdapter(sessions.toMutableList()) { session, action ->
            when (action) {
                "view" -> viewSessionDetails(session)
                "delete" -> deleteSession(session)
            }
        }
        recyclerView.adapter = adapter
    }

    private fun viewSessionDetails(session: AttendanceSession) {
        val message = """
            Course: ${session.courseName}
            Date: ${session.date}
            Total Students: ${session.totalStudents}
            Present: ${session.presentCount}
            Absent: ${session.absentCount}
            File: ${session.fileName}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Session Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun deleteSession(session: AttendanceSession) {
        AlertDialog.Builder(this)
            .setTitle("Delete Session?")
            .setMessage("Are you sure you want to delete ${session.courseName}?")
            .setPositiveButton("Delete") { _, _ ->
                sessionManager.deleteSession(session)
                loadSessions() // Reload
                Toast.makeText(this, "Session deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}