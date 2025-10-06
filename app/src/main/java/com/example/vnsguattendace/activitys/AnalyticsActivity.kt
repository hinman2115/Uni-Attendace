package com.example.vnsguattendace.activitys

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vnsguattendace.R
import com.google.android.material.appbar.MaterialToolbar

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var tvTotalStudents: TextView
    private lateinit var tvPresentCount: TextView
    private lateinit var tvAbsentCount: TextView
    private lateinit var tvPresentPercentage: TextView
    private lateinit var tvAbsentPercentage: TextView
    private lateinit var tvAttendanceDate: TextView
    private lateinit var recyclerViewLowAttendance: RecyclerView
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        tvTotalStudents = findViewById(R.id.tvTotalStudents)
        tvPresentCount = findViewById(R.id.tvPresentCount)
        tvAbsentCount = findViewById(R.id.tvAbsentCount)
        tvPresentPercentage = findViewById(R.id.tvPresentPercentage)
        tvAbsentPercentage = findViewById(R.id.tvAbsentPercentage)
        tvAttendanceDate = findViewById(R.id.tvAttendanceDate)
        recyclerViewLowAttendance = findViewById(R.id.recyclerViewLowAttendance)

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Attendance Analytics"

        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Get data from intent
        val total = intent.getIntExtra("TOTAL", 0)
        val present = intent.getIntExtra("PRESENT", 0)
        val absent = intent.getIntExtra("ABSENT", 0)
        val date = intent.getStringExtra("DATE") ?: "Unknown Date"

        // Calculate percentages
        val presentPercentage = if (total > 0) (present * 100.0 / total) else 0.0
        val absentPercentage = if (total > 0) (absent * 100.0 / total) else 0.0

        // Display analytics
        tvTotalStudents.text = total.toString()
        tvPresentCount.text = present.toString()
        tvAbsentCount.text = absent.toString()
        tvPresentPercentage.text = String.format("%.1f%%", presentPercentage)
        tvAbsentPercentage.text = String.format("%.1f%%", absentPercentage)
        tvAttendanceDate.text = "Analytics for: $date"

        // Setup RecyclerView for low attendance warning (future feature)
        recyclerViewLowAttendance.layoutManager = LinearLayoutManager(this)
    }
}