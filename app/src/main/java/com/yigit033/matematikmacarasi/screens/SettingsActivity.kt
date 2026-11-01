package com.yigit033.matematikmacarasi.screens

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.core.view.WindowCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yigit033.matematikmacarasi.services.AudioService
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme

/**
 * Single SettingsScreen host. Don't artificially delay or duplicate loading state:
 * music/notifications/Premium are loaded by the existing SettingsScreen itself.
 */
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window,false)
        val audio=runCatching { AudioService.getInstance(applicationContext) }
            .onFailure { Log.e("SettingsActivity","Audio service unavailable",it) }
            .getOrNull()
        setContent {
            MatematikMacerasiTheme {
                val view=LocalView.current
                SideEffect {
                    window.statusBarColor=android.graphics.Color.TRANSPARENT
                    window.navigationBarColor=android.graphics.Color.TRANSPARENT
                    WindowCompat.getInsetsController(window,view)
                        .isAppearanceLightStatusBars=false
                    WindowCompat.getInsetsController(window,view)
                        .isAppearanceLightNavigationBars=false
                }
                if(audio!=null) {
                    androidx.compose.foundation.layout.Box(
                        modifier=Modifier.fillMaxSize()
                            .background(Color(0xFF0D3556)).safeDrawingPadding()
                    ) {
                        SettingsScreen(onBackClick={finish()},audioService=audio)
                    }
                } else {
                    Column(Modifier.fillMaxSize()
                        .background(Color(0xFF0D3556)).safeDrawingPadding().padding(22.dp),
                        horizontalAlignment=Alignment.CenterHorizontally,
                        verticalArrangement=Arrangement.Center) {
                        Text("Ses ve uygulama ayarları şu anda yüklenemiyor.",
                            color=Color.White)
                        Button(onClick={finish()},modifier=Modifier.padding(top=16.dp)) {
                            Text("Profile Dön")
                        }
                    }
                }
            }
        }
    }
}
