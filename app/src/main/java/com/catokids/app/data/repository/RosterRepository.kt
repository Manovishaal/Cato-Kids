package com.catokids.app.data.repository

import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.data.model.*
import com.catokids.app.data.remote.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

/**
 * Everything the grown-up roles need: classes, rosters, children and schools.
 *
 * When the app runs in offline explore mode (or the signed-in account has no data
 * yet) these calls fall back to a small, believable sample school so that Teacher,
 * Parent, School and Administrator screens are never blank.
 */
class RosterRepository(private val auth: AuthRepository) {

    private val online: Boolean
        get() = SupabaseProvider.isConfigured && !auth.isDemo.value

    // ---------- classes ----------

    suspend fun classesForTeacher(teacherId: String): List<ClassRoom> {
        if (!online) return SampleData.classes
        return runCatching {
            val classes = SupabaseProvider.client.from("classes")
                .select { filter { eq("teacher_id", teacherId) }; order("name", Order.ASCENDING) }
                .decodeList<ClassDto>()
                .map { it.toDomain() }
            classes.map { it.copy(studentCount = countStudents(it.id)) }
        }.getOrDefault(emptyList()).ifEmpty { SampleData.classes }
    }

    suspend fun classesForSchool(schoolId: String): List<ClassRoom> {
        if (!online) return SampleData.classes
        return runCatching {
            SupabaseProvider.client.from("classes")
                .select { filter { eq("school_id", schoolId) }; order("grade", Order.ASCENDING) }
                .decodeList<ClassDto>()
                .map { it.toDomain().copy(studentCount = countStudents(it.id)) }
        }.getOrDefault(emptyList()).ifEmpty { SampleData.classes }
    }

    private suspend fun countStudents(classId: String): Int = runCatching {
        SupabaseProvider.client.from("class_students")
            .select { filter { eq("class_id", classId) } }
            .decodeList<ClassStudentDto>().size
    }.getOrDefault(0)

    // ---------- students ----------

    suspend fun studentsInClass(classId: String): List<StudentSummary> {
        if (!online) return SampleData.studentsFor(classId)
        return runCatching {
            val ids = SupabaseProvider.client.from("class_students")
                .select { filter { eq("class_id", classId) } }
                .decodeList<ClassStudentDto>().map { it.studentId }
            if (ids.isEmpty()) return SampleData.studentsFor(classId)
            profilesFor(ids).map { it.toSummary() }
        }.getOrDefault(SampleData.studentsFor(classId))
    }

    suspend fun studentsInSchool(schoolId: String): List<StudentSummary> {
        if (!online) return SampleData.allStudents
        return runCatching {
            SupabaseProvider.client.from("profiles")
                .select {
                    filter { eq("school_id", schoolId); eq("role", "student") }
                    order("full_name", Order.ASCENDING)
                }
                .decodeList<ProfileDto>()
                .map { it.toDomain().toSummary() }
        }.getOrDefault(emptyList()).ifEmpty { SampleData.allStudents }
    }

    suspend fun childrenOf(parentId: String): List<StudentSummary> {
        if (!online) return SampleData.allStudents.take(2)
        return runCatching {
            val ids = SupabaseProvider.client.from("parent_children")
                .select { filter { eq("parent_id", parentId) } }
                .decodeList<ParentChildDto>().map { it.studentId }
            if (ids.isEmpty()) return SampleData.allStudents.take(2)
            profilesFor(ids).map { it.toSummary() }
        }.getOrDefault(SampleData.allStudents.take(2))
    }

    private suspend fun profilesFor(ids: List<String>): List<Profile> = runCatching {
        SupabaseProvider.client.from("profiles")
            .select { filter { isIn("id", ids) } }
            .decodeList<ProfileDto>()
            .map { it.toDomain() }
    }.getOrDefault(emptyList())

    /** Reads real progress rows for a set of students and folds them into summaries. */
    suspend fun enrichWithProgress(students: List<StudentSummary>): List<StudentSummary> {
        if (!online || students.isEmpty()) return students
        return runCatching {
            val ids = students.map { it.profile.id }
            val rows = SupabaseProvider.client.from("lesson_progress")
                .select { filter { isIn("student_id", ids) } }
                .decodeList<LessonProgressDto>()
                .map { it.toDomain() }
                .groupBy { it.studentId }

            students.map { s ->
                val mine = rows[s.profile.id].orEmpty()
                val total = CatoCurriculum.countFor(s.profile.grade ?: Grade.LKG)
                s.copy(
                    lessonsCompleted = mine.count { it.completed },
                    totalLessons = total,
                    stars = mine.sumOf { it.stars },
                    averageScore = if (mine.isEmpty()) 0 else mine.sumOf { it.bestScore } / mine.size,
                )
            }
        }.getOrDefault(students)
    }

    // ---------- schools ----------

    suspend fun school(schoolId: String?): School? {
        if (!online || schoolId == null) return SampleData.school
        return runCatching {
            SupabaseProvider.client.from("schools")
                .select { filter { eq("id", schoolId) } }
                .decodeSingleOrNull<SchoolDto>()?.toDomain()
        }.getOrNull() ?: SampleData.school
    }

    suspend fun allSchools(): List<School> {
        if (!online) return listOf(SampleData.school)
        return runCatching {
            SupabaseProvider.client.from("schools")
                .select { order("name", Order.ASCENDING) }
                .decodeList<SchoolDto>().map { it.toDomain() }
        }.getOrDefault(emptyList()).ifEmpty { listOf(SampleData.school) }
    }

    suspend fun platformCounts(): Map<Role, Int> {
        if (!online) return SampleData.platformCounts
        return runCatching {
            val all = SupabaseProvider.client.from("profiles")
                .select { }
                .decodeList<ProfileDto>()
            Role.entries.associateWith { r -> all.count { Role.fromWire(it.role) == r } }
        }.getOrDefault(SampleData.platformCounts)
    }

    private fun Profile.toSummary() = StudentSummary(
        profile = this,
        totalLessons = CatoCurriculum.countFor(grade ?: Grade.LKG),
        stars = stars,
    )
}
