package com.yigit033.matematikmacarasi.components

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yigit033.matematikmacarasi.models.Avatar
import com.yigit033.matematikmacarasi.services.Avatar3Customization

/** Shared player portrait; updates across screens when a look is customized. */
@Composable
fun Avatar3PlayerPortrait(
    avatar: Avatar,
    size: Dp = 80.dp,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    val context = LocalContext.current
    val style = remember(context) { Avatar3Customization(context) }
    var revision by remember(context) { mutableStateOf(0) }
    DisposableEffect(context) {
        val prefs = context.getSharedPreferences("avatar3_customization_v1", Context.MODE_PRIVATE)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            revision += 1
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val look = remember(avatar.id, revision) { style.selectedLook(avatar.id) }
    val accessory = remember(avatar.id, revision) { style.selectedAccessory(avatar.id) }
    val effect = remember(avatar.id, revision) { style.selectedEffect(avatar.id) }
    Avatar3CharacterStage(
        avatar = avatar,
        lookId = look,
        accessory = accessory,
        effect = effect,
        size = size,
        modifier = modifier,
        animate = animate
    )
}