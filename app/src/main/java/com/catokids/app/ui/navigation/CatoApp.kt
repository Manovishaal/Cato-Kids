package com.catokids.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.catokids.app.core.AppContainer
import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.data.model.Grade
import com.catokids.app.data.model.Role
import com.catokids.app.data.model.SubjectId
import com.catokids.app.ui.auth.*
import com.catokids.app.ui.dashboard.*
import com.catokids.app.ui.games.GameHostScreen
import com.catokids.app.ui.games.GameResultScreen
import com.catokids.app.ui.games.GameViewModel
import com.catokids.app.ui.student.*
import kotlinx.coroutines.launch

@Composable
fun CatoApp(container: AppContainer) {
    val nav = rememberNavController()
    val authVm: AuthViewModel = viewModel(factory = AuthViewModel.factory(container))
    val authState by authVm.state.collectAsState()
    val isDemo by container.auth.isDemo.collectAsState()
    val scope = rememberCoroutineScope()

    val soundOn by container.preferences.soundOn.collectAsState(initial = true)
    LaunchedEffect(soundOn) {
        container.speech.setEnabled(soundOn)
        container.sounds.setEnabled(soundOn)
    }

    // Route to the right home whenever the signed-in role changes.
    LaunchedEffect(authState.profile?.id, authState.profile?.role, authState.restored) {
        if (!authState.restored) return@LaunchedEffect
        val profile = authState.profile
        val target = when (profile?.role) {
            Role.STUDENT -> Routes.STUDENT_HOME
            Role.TEACHER -> Routes.TEACHER_HOME
            Role.PARENT  -> Routes.PARENT_HOME
            Role.ADMIN   -> Routes.ADMIN_HOME
            Role.SCHOOL  -> Routes.SCHOOL_HOME
            null         -> Routes.ROLE
        }
        val current = nav.currentDestination?.route
        if (current == Routes.SPLASH) return@LaunchedEffect
        if (profile == null && current != null && current.startsWith("student")) {
            nav.navigateTop(Routes.ROLE)
        } else if (profile != null && current in setOf(Routes.ROLE, Routes.LOGIN, Routes.REGISTER, Routes.FORGOT, null)) {
            nav.navigateTop(target)
        }
    }

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = nav,
            startDestination = Routes.SPLASH,
            // Going deeper slides in from the right and pushes the old screen back;
            // coming back reverses it, so the child always feels which way they moved.
            enterTransition = { fadeIn(tween(240)) + slideInHorizontally(tween(320)) { it / 7 } },
            exitTransition = { fadeOut(tween(160)) + scaleOut(tween(280), targetScale = 0.96f) },
            popEnterTransition = { fadeIn(tween(240)) + scaleIn(tween(320), initialScale = 0.96f) },
            popExitTransition = { fadeOut(tween(160)) + slideOutHorizontally(tween(280)) { it / 7 } },
        ) {

            composable(Routes.SPLASH) {
                SplashScreen(onFinished = {
                    val profile = authState.profile
                    val target = when (profile?.role) {
                        Role.STUDENT -> Routes.STUDENT_HOME
                        Role.TEACHER -> Routes.TEACHER_HOME
                        Role.PARENT  -> Routes.PARENT_HOME
                        Role.ADMIN   -> Routes.ADMIN_HOME
                        Role.SCHOOL  -> Routes.SCHOOL_HOME
                        null         -> Routes.ROLE
                    }
                    nav.navigateTop(target)
                })
            }

            // ---------------- auth ----------------

            composable(Routes.ROLE) {
                RoleSelectScreen(
                    backendConfigured = authVm.backendConfigured,
                    onPick = { role -> authVm.clearMessages(); nav.navigate(Routes.login(role.wire)) },
                    onExplore = { role -> authVm.exploreOffline(role, Grade.LKG) },
                )
            }

            composable(
                Routes.LOGIN,
                arguments = listOf(navArgument("role") { type = NavType.StringType }),
            ) { entry ->
                val role = Role.fromWire(entry.arguments?.getString("role"))
                LoginScreen(
                    role = role,
                    state = authState,
                    onSignIn = { e, p -> authVm.signIn(e, p) },
                    onRegister = { authVm.clearMessages(); nav.navigate(Routes.register(role.wire)) },
                    onForgot = { authVm.clearMessages(); nav.navigate(Routes.FORGOT) },
                    onBack = { nav.popBackStack() },
                    onExplore = { authVm.exploreOffline(role, if (role == Role.STUDENT) Grade.LKG else null) },
                )
            }

            composable(
                Routes.REGISTER,
                arguments = listOf(navArgument("role") { type = NavType.StringType }),
            ) { entry ->
                val role = Role.fromWire(entry.arguments?.getString("role"))
                RegisterScreen(
                    role = role,
                    state = authState,
                    onRegister = { name, email, pass, confirm, grade, phone ->
                        authVm.register(name, email, pass, confirm, role, grade, phone)
                    },
                    onBack = { nav.popBackStack() },
                )
            }

            composable(Routes.FORGOT) {
                ForgotPasswordScreen(
                    state = authState,
                    onSend = { authVm.sendReset(it) },
                    onBack = { nav.popBackStack() },
                )
            }

            // ---------------- student ----------------

            composable(Routes.STUDENT_HOME) {
                val vm: StudentViewModel = viewModel(factory = StudentViewModel.factory(container))
                val s by vm.state.collectAsState()
                StudentHomeScreen(
                    state = s,
                    onOpenSubject = { nav.navigate(Routes.subject(it.name)) },
                    onOpenLesson = { nav.navigate(Routes.lesson(it.id)) },
                    onOpenProfile = { nav.navigate(Routes.PROFILE) },
                    onOpenRewards = { nav.navigate(Routes.REWARDS) },
                    onChangeGrade = { vm.setGrade(it) },
                )
            }

            composable(
                Routes.SUBJECT,
                arguments = listOf(navArgument("subject") { type = NavType.StringType }),
            ) { entry ->
                val vm: StudentViewModel = viewModel(factory = StudentViewModel.factory(container))
                val s by vm.state.collectAsState()
                val subject = runCatching {
                    SubjectId.valueOf(entry.arguments?.getString("subject") ?: "")
                }.getOrDefault(SubjectId.LETTER_LAND)
                SubjectScreen(
                    subject = subject,
                    lessons = CatoCurriculum.forGradeAndSubject(s.grade, subject),
                    state = s,
                    onOpenLesson = { nav.navigate(Routes.lesson(it.id)) },
                    onBack = { nav.popBackStack() },
                )
            }

            composable(Routes.REWARDS) {
                val vm: StudentViewModel = viewModel(factory = StudentViewModel.factory(container))
                val s by vm.state.collectAsState()
                RewardsScreen(state = s, onBack = { nav.popBackStack() })
            }

            // ---------------- games ----------------

            composable(
                Routes.LESSON,
                arguments = listOf(navArgument("lessonId") { type = NavType.StringType }),
            ) { entry ->
                val lessonId = entry.arguments?.getString("lessonId").orEmpty()
                val vm: GameViewModel = viewModel(
                    key = "game-$lessonId",
                    factory = GameViewModel.factory(container, lessonId),
                )
                val s by vm.state.collectAsState()

                LaunchedEffect(s.finished) {
                    if (s.finished) {
                        nav.navigate(
                            Routes.result(lessonId, s.score, s.correct, s.totalRounds, s.stars, s.elapsedSeconds)
                        ) { popUpTo(Routes.LESSON) { inclusive = true } }
                    }
                }

                GameHostScreen(
                    state = s,
                    onSubmit = vm::submit,
                    onNext = vm::next,
                    onRetry = vm::retryRound,
                    onSpeak = vm::speakCurrentPrompt,
                    onTap = vm::tapSound,
                    onExit = { nav.popBackStack() },
                )
            }

            composable(
                Routes.RESULT,
                arguments = listOf(
                    navArgument("lessonId") { type = NavType.StringType },
                    navArgument("score")   { type = NavType.IntType },
                    navArgument("correct") { type = NavType.IntType },
                    navArgument("total")   { type = NavType.IntType },
                    navArgument("stars")   { type = NavType.IntType },
                    navArgument("seconds") { type = NavType.IntType },
                ),
            ) { entry ->
                val args = entry.arguments
                val lessonId = args?.getString("lessonId").orEmpty()
                GameResultScreen(
                    lessonTitle = CatoCurriculum.lesson(lessonId)?.title ?: "Lesson",
                    score = args?.getInt("score") ?: 0,
                    correct = args?.getInt("correct") ?: 0,
                    total = args?.getInt("total") ?: 0,
                    stars = args?.getInt("stars") ?: 0,
                    seconds = args?.getInt("seconds") ?: 0,
                    onPlayAgain = {
                        nav.navigate(Routes.lesson(lessonId)) {
                            popUpTo(Routes.RESULT) { inclusive = true }
                        }
                    },
                    onKeepLearning = { nav.navigateTop(Routes.STUDENT_HOME) },
                )
            }

            // ---------------- grown-ups ----------------

            composable(Routes.TEACHER_HOME) {
                val vm: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(container))
                val s by vm.state.collectAsState()
                TeacherHomeScreen(
                    state = s,
                    onOpenClass = { nav.navigate(Routes.classDetail(it)) },
                    onOpenStudent = { nav.navigate(Routes.studentReport(it)) },
                    onOpenProfile = { nav.navigate(Routes.PROFILE) },
                )
            }

            composable(
                Routes.CLASS_DETAIL,
                arguments = listOf(navArgument("classId") { type = NavType.StringType }),
            ) { entry ->
                val vm: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(container))
                val s by vm.state.collectAsState()
                val classId = entry.arguments?.getString("classId").orEmpty()
                ClassDetailScreen(
                    className = s.classes.firstOrNull { it.id == classId }?.name ?: "Class",
                    students = vm.studentsIn(classId),
                    onOpenStudent = { nav.navigate(Routes.studentReport(it)) },
                    onBack = { nav.popBackStack() },
                )
            }

            composable(
                Routes.STUDENT_REPORT,
                arguments = listOf(navArgument("studentId") { type = NavType.StringType }),
            ) { entry ->
                val vm: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(container))
                val studentId = entry.arguments?.getString("studentId").orEmpty()
                val student = vm.student(studentId)
                StudentReportScreen(
                    student = student,
                    breakdown = student?.let { vm.subjectBreakdownFor(it) } ?: emptyMap(),
                    onBack = { nav.popBackStack() },
                )
            }

            composable(Routes.PARENT_HOME) {
                val vm: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(container))
                val s by vm.state.collectAsState()
                ParentHomeScreen(
                    state = s,
                    onOpenChild = { nav.navigate(Routes.studentReport(it)) },
                    onOpenProfile = { nav.navigate(Routes.PROFILE) },
                )
            }

            composable(Routes.SCHOOL_HOME) {
                val vm: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(container))
                val s by vm.state.collectAsState()
                SchoolHomeScreen(
                    state = s,
                    onOpenClass = { nav.navigate(Routes.classDetail(it)) },
                    onOpenStudent = { nav.navigate(Routes.studentReport(it)) },
                    onOpenProfile = { nav.navigate(Routes.PROFILE) },
                )
            }

            composable(Routes.ADMIN_HOME) {
                val vm: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(container))
                val s by vm.state.collectAsState()
                AdminHomeScreen(
                    state = s,
                    onOpenStudent = { nav.navigate(Routes.studentReport(it)) },
                    onOpenProfile = { nav.navigate(Routes.PROFILE) },
                )
            }

            // ---------------- profile ----------------

            composable(Routes.PROFILE) {
                ProfileScreen(
                    profile = authState.profile,
                    isDemo = isDemo,
                    soundOn = soundOn,
                    onSoundChange = { on -> scope.launch { container.preferences.setSound(on) } },
                    onSave = { name, phone, grade ->
                        scope.launch { container.auth.updateProfile(name, phone, grade) }
                    },
                    onSignOut = {
                        authVm.signOut()
                        nav.navigateTop(Routes.ROLE)
                    },
                    onBack = { nav.popBackStack() },
                )
            }
        }
    }
}

/** Navigate and clear the back stack so Back never returns to a signed-out screen. */
private fun NavHostController.navigateTop(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { inclusive = true }
        launchSingleTop = true
    }
}
