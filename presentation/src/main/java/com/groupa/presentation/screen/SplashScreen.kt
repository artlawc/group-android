package com.groupa.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.R
import com.groupa.coreui.theme.CheckoutBlue
import com.groupa.coreui.theme.White
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(1_200)
        onTimeout()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CheckoutBlue),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.splash_logo),
            contentDescription = "DealSpot logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(128.dp),
        )
        Text(
            text = "DealSpot",
            color = White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.8).sp,
        )
    }
}
