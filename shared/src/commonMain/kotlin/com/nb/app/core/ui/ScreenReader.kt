package com.nb.app.core.ui

import androidx.compose.runtime.Composable

/** Whether TalkBack (Android) or VoiceOver (iOS) is on. */
@Composable
expect fun isScreenReaderOn(): Boolean
