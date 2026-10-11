package com.dulcosto.app

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay

@Composable
fun EbyzomSplash(
    appName: String,
    tagline: String,
    logo: Painter,
    paper: Color,
    ink: Color,
    inkSoft: Color,
    onFinished: () -> Unit,
) {
    var brand by remember { mutableStateOf(true) }
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !brand
        controller.isAppearanceLightNavigationBars = !brand
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = if (brand) {
            android.graphics.Color.BLACK
        } else {
            android.graphics.Color.parseColor("#E7EEF2")
        }
    }
    if (brand) {
        LaunchedEffect(Unit) {
            delay(1600)
            brand = false
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { brand = false },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painterResource(R.drawable.ebyzom_logo),
                contentDescription = "EBYZOM",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                contentScale = ContentScale.Fit,
            )
        }
    } else {
        LaunchedEffect(Unit) {
            delay(1500)
            onFinished()
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(paper)
                .statusBarsPadding()
                .clickable(onClick = onFinished),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(logo, contentDescription = appName, modifier = Modifier.size(176.dp))
                Spacer(Modifier.height(8.dp))
                Text(appName, color = ink, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                Text(
                    tagline,
                    color = inkSoft,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                )
            }
        }
    }
}

@Composable
fun MainSystemBars() {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = true
        controller.isAppearanceLightNavigationBars = true
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.parseColor("#F7F8F4")
    }
}
