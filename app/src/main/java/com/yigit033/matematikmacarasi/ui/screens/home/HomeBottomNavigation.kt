package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class HomeTab(val index: Int, val title: String, val icon: ImageVector) {
    HOME(0, "Ana Sayfa", Icons.Filled.Home),
    GAMES(1, "Oyunlar", Icons.Filled.SportsEsports),
    LEARN(2, "Öğren", Icons.Filled.AutoStories),
    PROFILE(3, "Profil", Icons.Filled.Person)
}

/**
 * Onaylı Referansa Sadık Alt Navigasyon Çubuğu (Sprint H2)
 *
 * Referans: reference/HOME_APPROVED_REFERENCE.png
 * - Kompakt koyu mavi taban (#08172E)
 * - Parlak mavi seçili buton (parlak mavi daire + beyaz ikon)
 * - Sistem gezinme çubuğu boşluğunu (navigationBarsPadding) otomatik korur
 */
@Composable
fun HomeBottomNavigationBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                spotColor = Color(0xFF021B3A).copy(alpha = 0.5f)
            )
            .background(HomeColors.BottomNavBackground)
            .border(
                width = 1.dp,
                color = HomeColors.BottomNavBorder,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) Color.White else HomeColors.BottomNavUnselected,
                    label = "tab_icon_tint"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else HomeColors.BottomNavUnselected,
                    label = "tab_text_color"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTabSelected(tab)
                        }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(HomeColors.BottomNavSelected, CircleShape)
                                        .border(2.dp, HomeColors.BottomNavSelectedGlow, CircleShape)
                                        .shadow(4.dp, CircleShape, spotColor = HomeColors.BottomNavSelectedGlow)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = tab.title,
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
