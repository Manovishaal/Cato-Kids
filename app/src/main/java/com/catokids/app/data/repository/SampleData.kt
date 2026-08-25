package com.catokids.app.data.repository

import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.data.model.*

/**
 * A small, believable demo school. Used in offline explore mode and whenever a real
 * account has no roster yet, so the grown-up dashboards always have something to show.
 */
internal object SampleData {

    val school = School(
        id = "sample-school",
        name = "Mother Goose Primary",
        code = "MGP-001",
        city = "Chennai",
        address = "12 Garden Road",
        phone = "+91 44 4000 1234",
        email = "hello@mothergoose.school",
    )

    val classes = listOf(
        ClassRoom("c-prekg", "Sunflower",  Grade.PREKG, school.id, "t1", "2026", 18),
        ClassRoom("c-lkg",   "Bluebell",   Grade.LKG,   school.id, "t1", "2026", 22),
        ClassRoom("c-ukg",   "Marigold",   Grade.UKG,   school.id, "t1", "2026", 20),
    )

    private data class Seed(val name: String, val grade: Grade, val done: Int, val avg: Int, val last: String)

    private val seeds = listOf(
        Seed("Aarav Kumar",    Grade.LKG,   14, 88, "Today"),
        Seed("Diya Sharma",    Grade.LKG,   17, 94, "Today"),
        Seed("Ishaan Patel",   Grade.LKG,    9, 71, "Yesterday"),
        Seed("Meera Nair",     Grade.UKG,   21, 91, "Today"),
        Seed("Rohan Das",      Grade.UKG,   12, 66, "3 days ago"),
        Seed("Ananya Iyer",    Grade.PREKG, 11, 85, "Today"),
        Seed("Vihaan Reddy",   Grade.PREKG,  6, 58, "5 days ago"),
        Seed("Sara Fernandes", Grade.UKG,   18, 79, "Yesterday"),
        Seed("Kabir Singh",    Grade.LKG,    4, 62, "A week ago"),
        Seed("Tara Menon",     Grade.PREKG, 15, 90, "Today"),
    )

    val allStudents: List<StudentSummary> by lazy {
        seeds.mapIndexed { i, s ->
            StudentSummary(
                profile = Profile(
                    id = "sample-student-$i",
                    role = Role.STUDENT,
                    fullName = s.name,
                    grade = s.grade,
                    schoolId = school.id,
                    stars = s.done * 2,
                    coins = s.done * 12,
                    streakDays = (i % 5) + 1,
                ),
                lessonsCompleted = s.done,
                totalLessons = CatoCurriculum.countFor(s.grade),
                stars = s.done * 2,
                averageScore = s.avg,
                lastActiveLabel = s.last,
            )
        }
    }

    fun studentsFor(classId: String): List<StudentSummary> {
        val grade = classes.firstOrNull { it.id == classId }?.grade
        return if (grade == null) allStudents else allStudents.filter { it.profile.grade == grade }
    }

    val platformCounts: Map<Role, Int> = mapOf(
        Role.STUDENT to 248,
        Role.TEACHER to 14,
        Role.PARENT  to 206,
        Role.SCHOOL  to 3,
        Role.ADMIN   to 2,
    )
}
