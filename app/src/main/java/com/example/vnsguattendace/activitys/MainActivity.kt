package com.example.vnsguattendace.activitys

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vnsguattendace.model.AttendanceSession
import com.example.vnsguattendace.R
import com.example.vnsguattendace.service.SessionManager
import com.example.vnsguattendace.service.StudentAdeptor
import com.example.vnsguattendace.model.Student
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // Summary card views
    private lateinit var totalStudentsText: TextView
    private lateinit var presentStudentsText: TextView
    private lateinit var absentStudentsText: TextView

    // Buttons
    private lateinit var btnLoadExcel: MaterialButton
    private lateinit var btnSaveAttendance: MaterialButton
    private lateinit var btnViewAnalytics: MaterialButton
    private lateinit var btnViewHistory: MaterialButton

    // Other views
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvDate: TextView
    private lateinit var tvStudentCount: TextView
    private lateinit var loadingOverlay: View

    private val studentList = mutableListOf<Student>()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Initialize views
        initializeViews()

        // Set current date
        setCurrentDate()

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Setup button listeners
        setupButtonListeners()
    }

    private fun initializeViews() {
        // Summary card
        totalStudentsText = findViewById(R.id.totalStudentsText)
        presentStudentsText = findViewById(R.id.presentStudentsText)
        absentStudentsText = findViewById(R.id.absentStudentsText)

        // Buttons
        btnLoadExcel = findViewById(R.id.btnLoadExcel)
        btnSaveAttendance = findViewById(R.id.btnSaveAttendance)
        btnViewAnalytics = findViewById(R.id.btnViewAnalytics)
        btnViewHistory = findViewById(R.id.btnViewHistory)

        // Other views
        recyclerView = findViewById(R.id.recyclerViewStudents)
        tvDate = findViewById(R.id.tvDate)
        tvStudentCount = findViewById(R.id.tvStudentCount)
        loadingOverlay = findViewById(R.id.loadingOverlay)
    }

    private fun setCurrentDate() {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        tvDate.text = dateFormat.format(Date())
    }

    private fun setupButtonListeners() {
        btnLoadExcel.setOnClickListener {
            openFilePicker()
        }

        btnSaveAttendance.setOnClickListener {
            showSaveDialog()
        }

        btnViewAnalytics.setOnClickListener {
            openAnalytics()
        }

        btnViewHistory.setOnClickListener {
            openHistory()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                loadExcelFile(uri)
            }
        }
    }

    private fun loadExcelFile(uri: Uri) {
        loadingOverlay.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)

                studentList.clear()

                for (i in 1 until sheet.physicalNumberOfRows) {
                    val row = sheet.getRow(i) ?: continue

                    val rollNumber = when (row.getCell(0)?.cellType) {
                        CellType.NUMERIC -> row.getCell(0).numericCellValue.toInt().toString()
                        else -> row.getCell(0)?.stringCellValue ?: ""
                    }

                    val name = row.getCell(1)?.stringCellValue ?: ""

                    if (rollNumber.isNotEmpty() && name.isNotEmpty()) {
                        studentList.add(Student(rollNumber, name, "Not Marked"))
                    }
                }

                withContext(Dispatchers.Main) {
                    loadingOverlay.visibility = View.GONE

                    if (studentList.isNotEmpty()) {
                        val adapter = StudentAdeptor(studentList) {
                            updateSummaryCard()
                        }
                        recyclerView.adapter = adapter

                        tvStudentCount.text = "${studentList.size} students"
                        btnSaveAttendance.isEnabled = true
                        btnViewAnalytics.isEnabled = true

                        updateSummaryCard()

                        Toast.makeText(
                            this@MainActivity,
                            "✓ Loaded ${studentList.size} students successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "✗ No valid student data found",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                inputStream?.close()
                workbook.close()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingOverlay.visibility = View.GONE
                    Toast.makeText(
                        this@MainActivity,
                        "✗ Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun updateSummaryCard() {
        val total = studentList.size
        val present = studentList.count { it.status == "Present" }
        val absent = studentList.count { it.status == "Absent" }

        totalStudentsText.text = total.toString()
        presentStudentsText.text = present.toString()
        absentStudentsText.text = absent.toString()
    }

    private fun showSaveDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Save Attendance Session")
        builder.setMessage("Enter course/class name for this attendance session")

        // Create input field
        val input = EditText(this)
        input.hint = "e.g., Computer Science 101"

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(50, 20, 50, 20)
        input.layoutParams = params
        container.addView(input)

        builder.setView(container)

        builder.setPositiveButton("Save") { dialog, _ ->
            val courseName = input.text.toString().trim()
            if (courseName.isEmpty()) {
                Toast.makeText(this, "⚠ Please enter a course name", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            saveAttendanceSession(courseName)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun saveAttendanceSession(courseName: String) {
        loadingOverlay.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create timestamped filename
                val timestamp = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
                val fileName = "Attendance_${courseName.replace(" ", "_")}_${dateFormat.format(
                    Date(
                        timestamp
                    )
                )}.xlsx"

                // Count attendance
                val present = studentList.count { it.status == "Present" }
                val absent = studentList.count { it.status == "Absent" }

                // Create session object
                val session = AttendanceSession(
                    courseName = courseName,
                    date = SimpleDateFormat(
                        "dd MMM yyyy, hh:mm a",
                        Locale.getDefault()
                    ).format(Date(timestamp)),
                    timestamp = timestamp,
                    totalStudents = studentList.size,
                    presentCount = present,
                    absentCount = absent,
                    students = studentList.toList(),
                    fileName = fileName
                )

                // Save session to SharedPreferences
                sessionManager.saveSession(session)

                // Export to Excel
                exportToExcel(fileName)

                withContext(Dispatchers.Main) {
                    loadingOverlay.visibility = View.GONE
                    Toast.makeText(
                        this@MainActivity,
                        "✓ Session saved: $courseName",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingOverlay.visibility = View.GONE
                    Toast.makeText(
                        this@MainActivity,
                        "✗ Error saving: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun exportToExcel(fileName: String) {
        try {
            // Create workbook
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Attendance")

            // Create header row
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("Roll Number")
            headerRow.createCell(1).setCellValue("Name")
            headerRow.createCell(2).setCellValue("Status")

            // Add student data
            studentList.forEachIndexed { index, student ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(student.rollnumber)
                row.createCell(1).setCellValue(student.name)
                row.createCell(2).setCellValue(student.status)
            }

            // Save to Downloads folder
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            val file = File(downloadsDir, fileName)

            val outputStream = FileOutputStream(file)
            workbook.write(outputStream)
            outputStream.close()
            workbook.close()

        } catch (e: Exception) {
            throw Exception("Excel export failed: ${e.message}")
        }
    }

    private fun openAnalytics() {
        val total = studentList.size
        val present = studentList.count { it.status == "Present" }
        val absent = studentList.count { it.status == "Absent" }
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val intent = Intent(this, AnalyticsActivity::class.java).apply {
            putExtra("TOTAL", total)
            putExtra("PRESENT", present)
            putExtra("ABSENT", absent)
            putExtra("DATE", currentDate)
        }
        startActivity(intent)
    }

    private fun openHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 100
    }
}