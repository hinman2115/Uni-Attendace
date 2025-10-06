package com.example.vnsguattendace.service

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vnsguattendace.R
import com.example.vnsguattendace.model.Student
import com.google.android.material.button.MaterialButton

class StudentAdeptor(
    private val studentList: List<Student>,
    private val onStatusChanged: () -> Unit // NEW: Callback for updates
): RecyclerView.Adapter<StudentAdeptor.StudentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = studentList[position]
        holder.bind(student)
    }

    override fun getItemCount() = studentList.size

    fun getStudents(): List<Student> = studentList

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rollNumber: TextView = itemView.findViewById(R.id.tvRollNumber)
        private val name: TextView = itemView.findViewById(R.id.tvStudentName)
        private val statusBadge: TextView = itemView.findViewById(R.id.tvStatus)
        private val presentButton: MaterialButton = itemView.findViewById(R.id.btnPresent)
        private val absentButton: MaterialButton = itemView.findViewById(R.id.btnAbsent)

        fun bind(student: Student) {
            rollNumber.text = student.rollnumber
            name.text = student.name
            statusBadge.text = student.status

            // Set initial status badge color
            when (student.status) {
                "Present" -> {
                    statusBadge.setBackgroundColor(Color.parseColor("#4CAF50"))
                    statusBadge.setTextColor(Color.WHITE)
                    presentButton.isEnabled = false
                    absentButton.isEnabled = false
                }
                "Absent" -> {
                    statusBadge.setBackgroundColor(Color.parseColor("#F44336"))
                    statusBadge.setTextColor(Color.WHITE)
                    presentButton.isEnabled = false
                    absentButton.isEnabled = false
                }
                "Not Marked" -> {  // NEW: Default state with enabled buttons
                    statusBadge.setBackgroundColor(Color.parseColor("#FF9800")) // Orange
                    statusBadge.setTextColor(Color.WHITE)
                    presentButton.isEnabled = true  // KEEP ENABLED
                    absentButton.isEnabled = true
                }
                    else -> {
                    statusBadge.setBackgroundColor(Color.parseColor("#9E9E9E"))
                    statusBadge.setTextColor(Color.WHITE)
                }
            }

            // Present button click
            presentButton.setOnClickListener {
                student.status = "Present"
                statusBadge.text = "Present"
                statusBadge.setBackgroundColor(Color.parseColor("#4CAF50"))
                statusBadge.setTextColor(Color.WHITE)
                presentButton.isEnabled = false
                absentButton.isEnabled = true
                onStatusChanged() // NOTIFY SUMMARY CARD
            }

            // Absent button click
            absentButton.setOnClickListener {
                student.status = "Absent"
                statusBadge.text = "Absent"
                statusBadge.setBackgroundColor(Color.parseColor("#F44336"))
                statusBadge.setTextColor(Color.WHITE)
                presentButton.isEnabled = true
                absentButton.isEnabled = false
                onStatusChanged() // NOTIFY SUMMARY CARD
            }
        }
    }
}
