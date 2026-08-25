package com.catokids.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Role(val wire: String, val label: String, val emoji: String, val blurb: String) {
    @SerialName("student") STUDENT("student", "Student",       "🧒", "Play, learn and collect stars"),
    @SerialName("teacher") TEACHER("teacher", "Teacher",       "👩‍🏫", "Run your class and track progress"),
    @SerialName("parent")  PARENT ("parent",  "Parent",        "🫂", "Follow your child's learning"),
    @SerialName("admin")   ADMIN  ("admin",   "Administrator", "🛠️", "Manage users, schools and content"),
    @SerialName("school")  SCHOOL ("school",  "School",        "🏫", "See the whole school at a glance");

    companion object {
        fun fromWire(value: String?): Role =
            entries.firstOrNull { it.wire.equals(value, ignoreCase = true) } ?: STUDENT
    }
}

@Serializable
enum class Grade(val wire: String, val label: String, val longLabel: String, val level: Int, val emoji: String) {
    @SerialName("PREKG") PREKG("PREKG", "Pre-KG", "Pre-Kindergarten", 1, "🐣"),
    @SerialName("LKG")   LKG  ("LKG",   "LKG",    "Lower Kindergarten", 2, "🐤"),
    @SerialName("UKG")   UKG  ("UKG",   "UKG",    "Upper Kindergarten", 3, "🦉");

    companion object {
        fun fromWire(value: String?): Grade? =
            entries.firstOrNull { it.wire.equals(value, ignoreCase = true) }
    }
}
