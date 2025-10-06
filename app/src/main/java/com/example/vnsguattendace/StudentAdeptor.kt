package com.example.vnsguattendace

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class StudentAdeptor(private val studentList: List<Student>):
    RecyclerView.Adapter<StudentAdeptor.StudentViewHolder>() {

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

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val tvRollNumber: TextView = itemView.findViewById(R.id.tvRollNumber)
        private val tvInitial: TextView = itemView.findViewById(R.id.tvInitial)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val statusBadge: MaterialCardView = itemView.findViewById(R.id.statusBadge)
        private val btnPresent: MaterialButton = itemView.findViewById(R.id.btnPresent)
        private val btnAbsent: MaterialButton = itemView.findViewById(R.id.btnAbsent)

        fun bind(student: Student) {
            tvStudentName.text = student.name
            tvRollNumber.text = "Roll: ${student.rollnumber}"
            tvInitial.text = student.name.firstOrNull()?.toString()?.uppercase() ?: "S"

            updateAttendanceUI(student.status)

            btnPresent.setOnClickListener {
                student.status = "Present"
                updateAttendanceUI("Present")
            }

            btnAbsent.setOnClickListener {
                student.status = "Absent"
                updateAttendanceUI("Absent")
            }
        }

        private fun updateAttendanceUI(status: String) {
            tvStatus.text = status

            if (status == "Present") {
                statusBadge.setCardBackgroundColor(Color.parseColor("#4CAF50"))
                btnPresent.isEnabled = false
                btnAbsent.isEnabled = true
            } else {
                statusBadge.setCardBackgroundColor(Color.parseColor("#F44336"))
                btnPresent.isEnabled = true
                btnAbsent.isEnabled = false
            }
        }
    }
}
