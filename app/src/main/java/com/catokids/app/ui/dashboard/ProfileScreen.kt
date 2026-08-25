package com.catokids.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.catokids.app.data.model.Grade
import com.catokids.app.data.model.Profile
import com.catokids.app.data.model.Role
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette
import com.catokids.app.ui.theme.LocalRoleColors

@Composable
fun ProfileScreen(
    profile: Profile?,
    isDemo: Boolean,
    soundOn: Boolean,
    onSoundChange: (Boolean) -> Unit,
    onSave: (name: String, phone: String, grade: Grade?) -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit,
) {
    var name by rememberSaveable(profile?.id) { mutableStateOf(profile?.fullName.orEmpty()) }
    var phone by rememberSaveable(profile?.id) { mutableStateOf(profile?.phone.orEmpty()) }
    var grade by rememberSaveable(profile?.id) { mutableStateOf(profile?.grade) }
    var saved by remember { mutableStateOf(false) }
    val colors = LocalRoleColors.current

    CatoBackdrop {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(40.dp))
            CatoTopBar(title = "My profile", onBack = onBack)

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Avatar(profile?.initials ?: "?", size = 88.dp)
                Spacer(Modifier.height(12.dp))
                Text(profile?.fullName.orEmpty(), style = MaterialTheme.typography.headlineSmall, color = CatoPalette.Ink)
                EmojiText(
                    "${profile?.role?.emoji ?: ""} ${profile?.role?.label ?: ""}" +
                        (profile?.grade?.let { " · ${it.label}" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CatoPalette.InkSoft,
                )
                if (profile?.email != null) {
                    Text(profile.email, style = MaterialTheme.typography.bodySmall, color = CatoPalette.InkSoft)
                }

                if (profile?.role == Role.STUDENT) {
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatPill("⭐", "${profile.stars}", "stars")
                        StatPill("🪙", "${profile.coins}", "coins", color = CatoPalette.CoralSoft)
                        StatPill("🔥", "${profile.streakDays}", "streak", color = CatoPalette.TealSoft)
                    }
                }

                if (isDemo) {
                    Spacer(Modifier.height(16.dp))
                    InfoBanner(
                        emoji = "📴",
                        text = "You're exploring offline. Progress is saved on this device only — create an account to sync it.",
                        color = CatoPalette.AmberSoft,
                    )
                }

                Spacer(Modifier.height(24.dp))
            }

            CatoCard(
                Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Details", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
                    Spacer(Modifier.height(14.dp))
                    CatoTextField(name, { name = it; saved = false }, "Name", leading = "🙂")
                    Spacer(Modifier.height(12.dp))
                    CatoTextField(phone, { phone = it; saved = false }, "Phone", leading = "📞")

                    if (profile?.role == Role.STUDENT) {
                        Spacer(Modifier.height(16.dp))
                        Text("Class", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Grade.entries.forEach { g ->
                                com.catokids.app.ui.auth.GradeChip(
                                    grade = g,
                                    selected = grade == g,
                                    color = colors.primary,
                                    modifier = Modifier.weight(1f),
                                    onClick = { grade = g; saved = false },
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    CatoButton(
                        text = if (saved) "Saved ✓" else "Save changes",
                        onClick = { onSave(name, phone, grade); saved = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            CatoCard(
                Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Settings", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        EmojiArt("🔊", size = 26.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Sound and speech", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                            Text(
                                "Read prompts aloud and play feedback sounds",
                                style = MaterialTheme.typography.bodySmall,
                                color = CatoPalette.InkSoft,
                            )
                        }
                        Switch(
                            checked = soundOn,
                            onCheckedChange = onSoundChange,
                            colors = SwitchDefaults.colors(checkedTrackColor = colors.primary),
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White),
            ) {
                CatoOutlineButton(
                    text = "Sign out",
                    leading = "👋",
                    onClick = onSignOut,
                    color = CatoPalette.Error,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Cato Kids · Mother Goose Learning",
                style = MaterialTheme.typography.bodySmall,
                color = CatoPalette.InkSoft,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}
