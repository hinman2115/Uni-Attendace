package com.example.vnsguattendace

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var studentAdapter: StudentAdeptor
    private lateinit var btnLoadExcel: MaterialButton
    private lateinit var btnSaveAttendance: MaterialButton
    private lateinit var tvDate: TextView
    private lateinit var tvStudentCount: TextView
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var lottieAnimation: LottieAnimationView
    private lateinit var tvLoadingMessage: TextView
    private lateinit var tvLoadingSubtext: TextView
    private val studentList = mutableListOf<Student>()

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                loadStudentsFromExcel(uri)
            }
        }
    }

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                saveAttendanceToExcel(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize all views
        btnLoadExcel = findViewById(R.id.btnLoadExcel)
        btnSaveAttendance = findViewById(R.id.btnSaveAttendance)
        recyclerView = findViewById(R.id.recyclerViewStudents)
        tvDate = findViewById(R.id.tvDate)
        tvStudentCount = findViewById(R.id.tvStudentCount)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        lottieAnimation = findViewById(R.id.lottieAnimation)
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage)
        tvLoadingSubtext = findViewById(R.id.tvLoadingSubtext)

        // Setup RecyclerView
        studentAdapter = StudentAdeptor(studentList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = studentAdapter

        // Initialize UI
        updateDateDisplay()
        updateStudentCount()

        // Set button click listeners
        btnLoadExcel.setOnClickListener {
            openFilePicker()
        }

        btnSaveAttendance.setOnClickListener {
            createFileSaver()
        }
    }

    // Show loading animation
    private fun showLoading(message: String, subtext: String = "Please wait") {
        tvLoadingMessage.text = message
        tvLoadingSubtext.text = subtext
        loadingOverlay.visibility = View.VISIBLE
        lottieAnimation.playAnimation()

        // Disable buttons during loading
        btnLoadExcel.isEnabled = false
        btnSaveAttendance.isEnabled = false
    }

    // Hide loading animation - THIS MAKES IT DISAPPEAR
    private fun hideLoading() {
        loadingOverlay.visibility = View.GONE
        lottieAnimation.cancelAnimation()

        // Re-enable buttons
        btnLoadExcel.isEnabled = true
        btnSaveAttendance.isEnabled = studentList.isNotEmpty()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        tvDate.text = dateFormat.format(Date())
    }

    private fun updateStudentCount() {
        val count = studentList.size
        tvStudentCount.text = if (count == 1) {
            "1 Student"
        } else {
            "$count Students"
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }
        openDocumentLauncher.launch(intent)
    }

    private fun loadStudentsFromExcel(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            // SHOW loading animation on Main thread
            withContext(Dispatchers.Main) {
                showLoading("Loading students...", "Reading Excel file")
            }

            try {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)

                val tempStudentList = mutableListOf<Student>()

                // Read all students from Excel
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i)
                    val rollNumberCell = row?.getCell(0)
                    val nameCell = row?.getCell(1)

                    if (rollNumberCell != null && nameCell != null) {
                        val rollNumber = when (rollNumberCell.cellType) {
                            CellType.NUMERIC -> rollNumberCell.numericCellValue.toInt().toString()
                            else -> rollNumberCell.stringCellValue
                        }
                        val name = nameCell.stringCellValue
                        tempStudentList.add(Student(rollNumber, name))
                    }
                }

                workbook.close()
                inputStream?.close()

                // HIDE loading animation and update UI
                withContext(Dispatchers.Main) {
                    hideLoading() // Animation disappears here

                    studentList.clear()
                    studentList.addAll(tempStudentList)
                    studentAdapter.notifyDataSetChanged()
                    updateStudentCount()
                    btnSaveAttendance.isEnabled = studentList.isNotEmpty()

                    Toast.makeText(
                        this@MainActivity,
                        "✓ Loaded ${studentList.size} students successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    hideLoading() // Hide animation on error too
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun createFileSaver() {
        val timeStampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timeStamp = timeStampFormat.format(Date())
        val fileName = "Attendance_$timeStamp.xlsx"

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        createDocumentLauncher.launch(intent)
    }

    private fun saveAttendanceToExcel(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            // SHOW loading animation
            withContext(Dispatchers.Main) {
                showLoading("Saving attendance...", "Creating Excel file")
            }

            try {
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Attendance")

                // Create header row
                val headerRow = sheet.createRow(0)
                headerRow.createCell(0).setCellValue("Roll Number")
                headerRow.createCell(1).setCellValue("Name")
                headerRow.createCell(2).setCellValue("Attendance Status")
                headerRow.createCell(3).setCellValue("Date")

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayDate = dateFormat.format(Date())
                val studentsToSave = studentAdapter.getStudents()

                val columnWidths = intArrayOf(15, 20, 20, 15)

                // Populate data rows
                studentsToSave.forEachIndexed { index, student ->
                    val row = sheet.createRow(index + 1)
                    row.createCell(0).setCellValue(student.rollnumber)
                    row.createCell(1).setCellValue(student.name)
                    row.createCell(2).setCellValue(student.status)
                    row.createCell(3).setCellValue(todayDate)

                    columnWidths[0] = maxOf(columnWidths[0], student.rollnumber.length)
                    columnWidths[1] = maxOf(columnWidths[1], student.name.length)
                    columnWidths[2] = maxOf(columnWidths[2], student.status.length)
                    columnWidths[3] = maxOf(columnWidths[3], todayDate.length)
                }

                // Set column widths
                sheet.setColumnWidth(0, (columnWidths[0] + 2) * 256)
                sheet.setColumnWidth(1, (columnWidths[1] + 2) * 256)
                sheet.setColumnWidth(2, (columnWidths[2] + 2) * 256)
                sheet.setColumnWidth(3, (columnWidths[3] + 2) * 256)

                // Write to file
                val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
                workbook.write(outputStream)
                outputStream?.close()
                workbook.close()

                // HIDE loading animation
                withContext(Dispatchers.Main) {
                    hideLoading() // Animation disappears here

                    Toast.makeText(
                        this@MainActivity,
                        "✓ Attendance saved successfully!",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    hideLoading() // Hide animation on error
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
