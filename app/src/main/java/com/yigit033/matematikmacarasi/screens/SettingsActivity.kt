package com.yigit033.matematikmacarasi.screens

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
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
        val audio=runCatching { AudioService.getInstance(applicationContext) }
            .onFailure { Log.e("SettingsActivity","Audio service unavailable",it) }
            .getOrNull()
        setContent {
            MatematikMacerasiTheme {
                if(audio!=null) {
                    androidx.compose.foundation.layout.Box(
                        modifier=Modifier.fillMaxSize().safeDrawingPadding()
                    ) {
                        SettingsScreen(onBackClick={finish()},audioService=audio)
                    }
                } else {
                    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(22.dp),
                        horizontalAlignment=Alignment.CenterHorizontally,
                        verticalArrangement=Arrangement.Center) {
                        Text("Ses ve uygulama ayarları şu anda yüklenemiyor.")
                        Button(onClick={finish()},modifier=Modifier.padding(top=16.dp)) {
                            Text("Profile Dön")
                        }
                    }
                }
            }
        }
    }
}
