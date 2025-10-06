package com.example.vnsguattendace.service

import android.content.Context
import com.example.vnsguattendace.model.AttendanceSession
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("AttendanceSessions", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveSession(session: AttendanceSession) {
        val sessions = getAllSessions().toMutableList()
        sessions.add(0, session) // Add to beginning

        val json = gson.toJson(sessions)
        prefs.edit().putString("sessions", json).apply()
    }

    fun getAllSessions(): List<AttendanceSession> {
        val json = prefs.getString("sessions", null) ?: return emptyList()
        val type = object : TypeToken<List<AttendanceSession>>() {}.type
        return gson.fromJson(json, type)
    }

    fun deleteSession(session: AttendanceSession) {
        val sessions = getAllSessions().toMutableList()
        sessions.remove(session)

        val json = gson.toJson(sessions)
        prefs.edit().putString("sessions", json).apply()
    }

    fun clearAllSessions() {
        prefs.edit().clear().apply()
    }
}
