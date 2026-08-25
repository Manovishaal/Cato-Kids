package com.catokids.app.ui.navigation

object Routes {
    const val SPLASH   = "splash"
    const val ROLE     = "role"
    const val LOGIN    = "login/{role}"
    const val REGISTER = "register/{role}"
    const val FORGOT   = "forgot"

    const val STUDENT_HOME = "student/home"
    const val SUBJECT      = "student/subject/{subject}"
    const val LESSON       = "student/lesson/{lessonId}"
    const val RESULT       = "student/result/{lessonId}/{score}/{correct}/{total}/{stars}/{seconds}"
    const val REWARDS      = "student/rewards"

    const val TEACHER_HOME   = "teacher/home"
    const val CLASS_DETAIL   = "teacher/class/{classId}"
    const val STUDENT_REPORT = "report/{studentId}"

    const val PARENT_HOME = "parent/home"
    const val ADMIN_HOME  = "admin/home"
    const val SCHOOL_HOME = "school/home"

    const val PROFILE  = "profile"
    const val SETTINGS = "settings"

    fun login(role: String) = "login/$role"
    fun register(role: String) = "register/$role"
    fun subject(subject: String) = "student/subject/$subject"
    fun lesson(lessonId: String) = "student/lesson/$lessonId"
    fun result(lessonId: String, score: Int, correct: Int, total: Int, stars: Int, seconds: Int) =
        "student/result/$lessonId/$score/$correct/$total/$stars/$seconds"
    fun classDetail(classId: String) = "teacher/class/$classId"
    fun studentReport(studentId: String) = "report/$studentId"
}
