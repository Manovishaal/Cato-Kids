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

    // ---------------- creator tools (teacher / school / admin only) ----------------
    const val CREATOR_HUB      = "creator/hub"
    const val CREATOR_HOMEWORK = "creator/homework"
    const val CREATOR_ACTIVITY = "creator/activity"
    const val CREATOR_COURSE   = "creator/course"
    const val CREATOR_GAME     = "creator/game"
    const val CREATOR_SUBMISSIONS = "creator/submissions/{assignmentId}"

    // ---------------- student: assignments, character, shop ----------------
    const val STUDENT_ASSIGNMENTS = "student/assignments"
    const val ASSIGNMENT_DETAIL   = "student/assignment/{assignmentId}"
    const val CHARACTER_CREATOR   = "student/character"
    const val SHOP                = "student/shop"

    fun login(role: String) = "login/$role"
    fun register(role: String) = "register/$role"
    fun subject(subject: String) = "student/subject/$subject"
    fun lesson(lessonId: String) = "student/lesson/$lessonId"
    fun result(lessonId: String, score: Int, correct: Int, total: Int, stars: Int, seconds: Int) =
        "student/result/$lessonId/$score/$correct/$total/$stars/$seconds"
    fun classDetail(classId: String) = "teacher/class/$classId"
    fun studentReport(studentId: String) = "report/$studentId"
    fun creatorSubmissions(assignmentId: String) = "creator/submissions/$assignmentId"
    fun assignmentDetail(assignmentId: String) = "student/assignment/$assignmentId"
}
