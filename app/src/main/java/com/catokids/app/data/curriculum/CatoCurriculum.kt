package com.catokids.app.data.curriculum

import com.catokids.app.data.model.Grade
import com.catokids.app.data.model.Lesson
import com.catokids.app.data.model.SubjectId

/**
 * The whole Cato Kids syllabus, bundled with the app so a child can play with no
 * network at all. The same rows are mirrored into Supabase (`subjects`, `lessons`)
 * so teachers can assign lessons and reports can join on `lesson_id`.
 */
object CatoCurriculum {

    val all: List<Lesson> by lazy {
        PreKgCurriculum.all + LkgCurriculum.all + UkgCurriculum.all
    }

    private val byId: Map<String, Lesson> by lazy { all.associateBy { it.id } }

    fun lesson(id: String): Lesson? = byId[id]

    fun forGrade(grade: Grade): List<Lesson> = all.filter { it.grade == grade }

    fun forGradeAndSubject(grade: Grade, subject: SubjectId): List<Lesson> =
        all.filter { it.grade == grade && it.subject == subject }.sortedBy { it.order }

    fun subjectsFor(grade: Grade): List<SubjectId> =
        SubjectId.entries.filter { s -> all.any { it.grade == grade && it.subject == s } }

    fun countFor(grade: Grade): Int = forGrade(grade).size

    fun countFor(grade: Grade, subject: SubjectId): Int = forGradeAndSubject(grade, subject).size
}
