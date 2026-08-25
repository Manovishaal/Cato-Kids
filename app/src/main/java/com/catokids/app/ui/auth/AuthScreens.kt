package com.catokids.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.catokids.app.data.model.Grade
import com.catokids.app.data.model.Role
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette
import com.catokids.app.ui.theme.roleColorsFor
import kotlinx.coroutines.delay

// ------------------------------------------------------------------ splash

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        delay(700)
        onFinished()
    }

    CatoBackdrop(top = CatoPalette.TealSoft, bottom = CatoPalette.Canvas) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CatoKidsLogo(
                modifier = Modifier
                    .size(190.dp)
                    .scale(scale.value),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Cato Kids",
                style = MaterialTheme.typography.displayLarge,
                color = CatoPalette.BrandBlue,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Letter Land · Number Land · Know My World",
                style = MaterialTheme.typography.bodySmall,
                color = CatoPalette.InkSoft,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(36.dp))
            MotherGooseLogo(
                modifier = Modifier
                    .height(100.dp)
                    .scale(scale.value),
            )
        }
    }
}

// ------------------------------------------------------------------ role picker

@Composable
fun RoleSelectScreen(
    onPick: (Role) -> Unit,
    onExplore: (Role) -> Unit,
    backendConfigured: Boolean,
) {
    CatoBackdrop(top = CatoPalette.CoralSoft) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 56.dp, bottom = 32.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CatoKidsLogo(modifier = Modifier.size(76.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Welcome to", style = MaterialTheme.typography.bodyMedium, color = CatoPalette.InkSoft)
                    Text("Cato Kids", style = MaterialTheme.typography.displaySmall, color = CatoPalette.BrandBlue)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Who's using the app?", style = MaterialTheme.typography.headlineSmall, color = CatoPalette.Ink)
            Spacer(Modifier.height(4.dp))
            Text(
                "Pick your role to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = CatoPalette.InkSoft,
            )
            Spacer(Modifier.height(18.dp))

            Role.entries.forEachIndexed { index, role ->
                PopIn(delayMillis = stagger(index, step = 70)) {
                    RoleCard(role = role, onClick = { onPick(role) })
                }
                Spacer(Modifier.height(12.dp))
            }

            if (!backendConfigured) {
                Spacer(Modifier.height(6.dp))
                InfoBanner(
                    emoji = "📴",
                    text = "No backend configured in this build — everything runs offline on this device.",
                    color = CatoPalette.AmberSoft,
                )
            }

            Spacer(Modifier.height(18.dp))
            CatoOutlineButton(
                text = "Explore without an account",
                leading = "👀",
                onClick = { onExplore(Role.STUDENT) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Explore mode works offline and saves progress on this device only.",
                style = MaterialTheme.typography.bodySmall,
                color = CatoPalette.InkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RoleCard(role: Role, onClick: () -> Unit) {
    val colors = roleColorsFor(role)
    CatoCard(
        onClick = onClick,
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.soft),
                contentAlignment = Alignment.Center,
            ) {
                EmojiArt(role.emoji, size = 36.dp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(role.label, style = MaterialTheme.typography.titleLarge, color = CatoPalette.Ink)
                Text(role.blurb, style = MaterialTheme.typography.bodySmall, color = CatoPalette.InkSoft)
            }
            Box(
                Modifier
                    .size(34.dp)
                    .pulse(maxScale = 1.10f, periodMillis = 1600)
                    .clip(CircleShape)
                    .background(colors.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text("→", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// ------------------------------------------------------------------ login

@Composable
fun LoginScreen(
    role: Role,
    state: AuthUiState,
    onSignIn: (String, String) -> Unit,
    onRegister: () -> Unit,
    onForgot: () -> Unit,
    onBack: () -> Unit,
    onExplore: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val colors = roleColorsFor(role)

    CatoBackdrop(top = colors.soft) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            CatoTopBar(title = "Sign in", subtitle = "${role.emoji}  ${role.label}", onBack = onBack)
            Spacer(Modifier.height(4.dp))

            Column(Modifier.padding(horizontal = 22.dp)) {
                CatoKidsLogo(modifier = Modifier.size(104.dp))
                Spacer(Modifier.height(10.dp))
                Text("Hello again!", style = MaterialTheme.typography.displaySmall, color = CatoPalette.Ink)
                Text(
                    "Sign in to keep learning.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CatoPalette.InkSoft,
                )
                Spacer(Modifier.height(22.dp))

                CatoTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    leading = "✉️",
                    keyboardType = KeyboardType.Email,
                )
                Spacer(Modifier.height(14.dp))
                CatoTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    leading = "🔒",
                    isPassword = true,
                )

                AnimatedVisibility(state.error != null) {
                    Column {
                        Spacer(Modifier.height(12.dp))
                        InfoBanner("⚠️", state.error.orEmpty(), color = CatoPalette.ErrorSoft)
                    }
                }
                AnimatedVisibility(state.info != null) {
                    Column {
                        Spacer(Modifier.height(12.dp))
                        InfoBanner("✅", state.info.orEmpty(), color = CatoPalette.SuccessSoft.copy(alpha = 0.5f))
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onForgot) {
                        Text("Forgot password?", color = colors.primary, style = MaterialTheme.typography.labelMedium)
                    }
                }

                CatoButton(
                    text = "Sign in",
                    leading = "🚀",
                    onClick = { onSignIn(email, password) },
                    loading = state.loading,
                    color = colors.primary,
                    emphasise = email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                CatoOutlineButton(
                    text = "Create a new account",
                    onClick = onRegister,
                    color = colors.primary,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(18.dp))
                TextButton(onClick = onExplore, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "or explore offline as ${role.label.lowercase()}",
                        color = CatoPalette.InkSoft,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

// ------------------------------------------------------------------ register

@Composable
fun RegisterScreen(
    role: Role,
    state: AuthUiState,
    onRegister: (name: String, email: String, password: String, confirm: String, grade: Grade?, phone: String) -> Unit,
    onBack: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var grade by rememberSaveable { mutableStateOf<Grade?>(null) }
    val colors = roleColorsFor(role)

    CatoBackdrop(top = colors.soft) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            CatoTopBar(title = "Create account", subtitle = "${role.emoji}  ${role.label}", onBack = onBack)

            Column(Modifier.padding(horizontal = 22.dp)) {
                Text(
                    when (role) {
                        Role.STUDENT -> "Let's set up your learning adventure"
                        Role.TEACHER -> "Set up your teacher account"
                        Role.PARENT  -> "Follow your child's progress"
                        Role.ADMIN   -> "Administrator access"
                        Role.SCHOOL  -> "Register your school"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = CatoPalette.Ink,
                )
                Spacer(Modifier.height(18.dp))

                CatoTextField(name, { name = it }, if (role == Role.SCHOOL) "School name" else "Full name", leading = "🙂")
                Spacer(Modifier.height(12.dp))
                CatoTextField(email, { email = it }, "Email", leading = "✉️", keyboardType = KeyboardType.Email)
                Spacer(Modifier.height(12.dp))
                CatoTextField(phone, { phone = it }, "Phone (optional)", leading = "📞", keyboardType = KeyboardType.Phone)

                if (role == Role.STUDENT) {
                    Spacer(Modifier.height(18.dp))
                    Text("Which class?", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Grade.entries.forEach { g ->
                            GradeChip(
                                grade = g,
                                selected = grade == g,
                                color = colors.primary,
                                modifier = Modifier.weight(1f),
                                onClick = { grade = g },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                CatoTextField(password, { password = it }, "Password", leading = "🔒", isPassword = true)
                Spacer(Modifier.height(12.dp))
                CatoTextField(confirm, { confirm = it }, "Confirm password", leading = "🔒", isPassword = true)

                AnimatedVisibility(state.error != null) {
                    Column {
                        Spacer(Modifier.height(12.dp))
                        InfoBanner("⚠️", state.error.orEmpty(), color = CatoPalette.ErrorSoft)
                    }
                }

                Spacer(Modifier.height(20.dp))
                CatoButton(
                    text = "Create account",
                    leading = "✨",
                    onClick = { onRegister(name, email, password, confirm, grade, phone) },
                    loading = state.loading,
                    color = colors.primary,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "By continuing you agree that a grown-up has set up this account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CatoPalette.InkSoft,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
fun GradeChip(
    grade: Grade,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) color else Color.White)
            .border(2.dp, if (selected) color else CatoPalette.Line, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmojiArt(grade.emoji, size = 30.dp)
        Spacer(Modifier.height(4.dp))
        Text(
            grade.label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (selected) Color.White else CatoPalette.Ink,
        )
    }
}

// ------------------------------------------------------------------ forgot

@Composable
fun ForgotPasswordScreen(
    state: AuthUiState,
    onSend: (String) -> Unit,
    onBack: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }

    CatoBackdrop {
        Column(Modifier.fillMaxSize()) {
            CatoTopBar(title = "Reset password", onBack = onBack)
            Column(Modifier.padding(22.dp)) {
                GooseCharacter(modifier = Modifier.size(104.dp))
                Spacer(Modifier.height(12.dp))
                Text("Forgot your password?", style = MaterialTheme.typography.headlineSmall, color = CatoPalette.Ink)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Enter your email and we'll send a link to set a new one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CatoPalette.InkSoft,
                )
                Spacer(Modifier.height(20.dp))
                CatoTextField(email, { email = it }, "Email", leading = "✉️", keyboardType = KeyboardType.Email)

                AnimatedVisibility(state.error != null) {
                    Column { Spacer(Modifier.height(12.dp)); InfoBanner("⚠️", state.error.orEmpty(), color = CatoPalette.ErrorSoft) }
                }
                AnimatedVisibility(state.info != null) {
                    Column { Spacer(Modifier.height(12.dp)); InfoBanner("✅", state.info.orEmpty(), color = CatoPalette.TealSoft) }
                }

                Spacer(Modifier.height(20.dp))
                CatoButton(
                    text = "Send reset link",
                    onClick = { onSend(email) },
                    loading = state.loading,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
