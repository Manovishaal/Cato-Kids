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

    // ---------- creator tools (offline explore mode) ----------

    val demoGames: List<CustomGame> by lazy {
        listOf(
            CustomGame(
                id = "demo-game-colours",
                createdBy = "sample-teacher",
                schoolId = school.id,
                title = "Rainbow Colours Quiz",
                description = "A quick quiz Ms. Iyer built for circle time.",
                subject = SubjectId.KNOW_MY_WORLD,
                grade = Grade.LKG,
                gameType = GameType.QUIZ,
                content = LessonContent(
                    intro = "Let's name some colours!",
                    rounds = listOf(
                        GameRound(
                            id = "r1",
                            prompt = "What colour is the sky?",
                            target = "Blue",
                            options = listOf(
                                Option("Blue", "🔵", correct = true),
                                Option("Red", "🔴"),
                                Option("Green", "🟢"),
                            ),
                        ),
                        GameRound(
                            id = "r2",
                            prompt = "What colour is grass?",
                            target = "Green",
                            options = listOf(
                                Option("Green", "🟢", correct = true),
                                Option("Blue", "🔵"),
                                Option("Yellow", "🟡"),
                            ),
                        ),
                    ),
                ),
            ),
        )
    }

    val demoCourses: List<ExtraCourse> by lazy {
        listOf(
            ExtraCourse(
                id = "demo-course-animals",
                createdBy = "sample-teacher",
                schoolId = school.id,
                title = "Amazing Animals",
                description = "An elective bundle for children who finished Know My World early.",
                coverEmoji = "🦁",
                subject = SubjectId.KNOW_MY_WORLD,
                grade = Grade.LKG,
                lessonIds = CatoCurriculum.forGradeAndSubject(Grade.LKG, SubjectId.KNOW_MY_WORLD)
                    .take(3).map { it.id },
            ),
        )
    }

    val demoActivities: List<Activity> = listOf(
        Activity(
            id = "demo-activity-crown",
            createdBy = "sample-teacher",
            schoolId = school.id,
            title = "Build a paper crown",
            instructions = "Cut, colour and staple a paper crown, then wear it for one whole day!",
            activityType = ActivityType.CRAFT,
            grade = Grade.LKG,
            pointsReward = 15,
        ),
        Activity(
            id = "demo-activity-hunt",
            createdBy = "sample-teacher",
            schoolId = school.id,
            title = "Nature scavenger hunt",
            instructions = "Find something red, something round and something that flies. Draw what you found.",
            activityType = ActivityType.OUTDOOR,
            grade = Grade.LKG,
            pointsReward = 20,
        ),
    )

    val demoAssignments: List<Assignment> by lazy {
        listOf(
            Assignment(
                id = "demo-hw-name",
                classId = "c-lkg",
                lessonId = null,
                assignedBy = "sample-teacher",
                dueDate = null,
                note = null,
                title = "Write your name 5 times",
                type = AssignmentType.HOMEWORK,
                instructions = "Practise your best handwriting on the lines in your workbook, then tell me how it went!",
                pointsReward = 15,
                requiresSubmission = true,
            ),
            Assignment(
                id = "demo-hw-game",
                classId = "c-lkg",
                lessonId = null,
                assignedBy = "sample-teacher",
                dueDate = null,
                note = null,
                title = demoGames.first().title,
                type = AssignmentType.HOMEWORK,
                instructions = "Play the colours quiz Ms. Iyer made for us.",
                pointsReward = 10,
                requiresSubmission = false,
                customGameId = demoGames.first().id,
            ),
            Assignment(
                id = "demo-activity-assign",
                classId = "c-lkg",
                lessonId = null,
                assignedBy = "sample-teacher",
                dueDate = null,
                note = null,
                title = demoActivities[1].title,
                type = AssignmentType.ACTIVITY,
                instructions = demoActivities[1].instructions,
                pointsReward = demoActivities[1].pointsReward,
                requiresSubmission = true,
                activityId = demoActivities[1].id,
            ),
            Assignment(
                id = "demo-course-assign",
                classId = "c-lkg",
                lessonId = null,
                assignedBy = "sample-teacher",
                dueDate = null,
                note = null,
                title = demoCourses.first().title,
                type = AssignmentType.COURSE,
                instructions = demoCourses.first().description,
                pointsReward = 0,
                requiresSubmission = false,
                courseId = demoCourses.first().id,
            ),
        )
    }

    val demoSubmissions: List<AssignmentSubmission> = listOf(
        AssignmentSubmission(
            id = "demo-submission-1",
            assignmentId = "demo-activity-assign",
            studentId = "demo-student",
            answerText = "I found a red leaf, a round pebble and a butterfly!",
            status = SubmissionStatus.REVIEWED,
            score = 95,
            teacherFeedback = "Lovely observations, well done!",
        ),
    )
}
