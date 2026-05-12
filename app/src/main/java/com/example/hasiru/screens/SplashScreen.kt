package com.example.hasiru.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hasiru.R
import com.example.hasiru.ui.theme.HasiruTheme
import com.example.hasiru.ui.theme.Green80
import com.example.hasiru.ui.theme.Green40
import com.example.hasiru.ui.theme.LeafGreenLight
import com.example.hasiru.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit = {}  // will navigate away later
) {
    val isPreview = LocalInspectionMode.current
    var startAnimation by remember { mutableStateOf(isPreview) }
    
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = LinearOutSlowInEasing),
        label = "Alpha"
    )
    
    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.7f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "Scale"
    )
    
    val offsetYAnim = animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 40.dp,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "Offset"
    )

    LaunchedEffect(key1 = true) {
        if (!isPreview) {
            startAnimation = true
            delay(3000)
            onSplashFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(White, LeafGreenLight, Green80.copy(alpha = 0.3f)),
                    center = androidx.compose.ui.geometry.Offset.Unspecified,
                    radius = Float.POSITIVE_INFINITY
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Advanced decorative background with "glassmorphism" inspired blurred elements
        Box(modifier = Modifier.fillMaxSize()) {
            // Soft animated glow Top Left
            Surface(
                modifier = Modifier
                    .size(500.dp)
                    .offset(x = (-150).dp, y = (-100).dp)
                    .blur(100.dp)
                    .alpha(if (startAnimation) 0.4f else 0.1f),
                shape = CircleShape,
                color = Green80
            ) {}

            // Soft animated glow Bottom Right
            Surface(
                modifier = Modifier
                    .size(600.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 150.dp, y = 150.dp)
                    .blur(120.dp)
                    .alpha(if (startAnimation) 0.3f else 0.1f),
                shape = CircleShape,
                color = Green40.copy(alpha = 0.5f)
            ) {}

            // Center highlight
            Surface(
                modifier = Modifier
                    .size(400.dp)
                    .align(Alignment.Center)
                    .blur(150.dp)
                    .alpha(0.2f),
                shape = CircleShape,
                color = White
            ) {}
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alphaAnim.value)
                .offset(y = offsetYAnim.value)
        ) {
            Surface(
                modifier = Modifier
                    .size(520.dp)
                    .scale(scaleAnim.value),
                shape = CircleShape,
                color = Color.Transparent,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Hasiru Logo",
                        modifier = Modifier.size(900.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "GREEN CITY INITIATIVE",
                style = MaterialTheme.typography.labelLarge,
                color = Green40.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alphaAnim.value)
            )
        }
        
        Text(
            text = "Empowering Nature",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .alpha(0.8f),
            style = MaterialTheme.typography.labelMedium,
            color = Green40,
            letterSpacing = 2.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    HasiruTheme {
        SplashScreen()
    }
}