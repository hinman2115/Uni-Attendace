package com.example.vnsguattendace.model

import java.io.Serializable

data class AttendanceSession(
    val courseName: String,
    val date: String,
    val timestamp: Long,
    val totalStudents: Int,
    val presentCount: Int,
    val absentCount: Int,
    val students: List<Student>,
    val fileName: String
) : Serializable