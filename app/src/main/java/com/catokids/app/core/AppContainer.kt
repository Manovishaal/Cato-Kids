package com.catokids.app.core

import android.content.Context
import com.catokids.app.data.local.AppPreferences
import com.catokids.app.data.repository.AssignmentRepository
import com.catokids.app.data.repository.AuthRepository
import com.catokids.app.data.repository.AvatarRepository
import com.catokids.app.data.repository.ContentRepository
import com.catokids.app.data.repository.CurriculumSyncRepository
import com.catokids.app.data.repository.ProgressRepository
import com.catokids.app.data.repository.RosterRepository

/** Hand-rolled dependency container — no annotation processors, no build-time magic. */
class AppContainer(context: Context) {
    val preferences: AppPreferences by lazy { AppPreferences(context.applicationContext) }
    val auth: AuthRepository by lazy { AuthRepository(preferences) }
    val progress: ProgressRepository by lazy { ProgressRepository(preferences, auth) }
    val roster: RosterRepository by lazy { RosterRepository(auth) }
    val curriculumSync: CurriculumSyncRepository by lazy { CurriculumSyncRepository(auth) }
    val assignments: AssignmentRepository by lazy { AssignmentRepository(auth) }
    val content: ContentRepository by lazy { ContentRepository(auth) }
    val avatar: AvatarRepository by lazy { AvatarRepository(preferences, auth) }
    val speech: SpeechEngine by lazy { SpeechEngine(context.applicationContext) }
    val sounds: SoundEffects by lazy { SoundEffects(context.applicationContext) }
}
