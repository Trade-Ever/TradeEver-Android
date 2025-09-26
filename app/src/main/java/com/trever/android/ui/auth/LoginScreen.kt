package com.trever.android.ui.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator

import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.trever.android.R
import com.trever.android.ui.theme.G_100
import com.trever.android.ui.theme.G_200
import com.trever.android.ui.theme.backgroundColor

@Composable
fun LoginScreen(viewModel: AuthViewModel, onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val loginState by viewModel.loginState.collectAsState()
    val profileComplete by viewModel.profileComplete.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        viewModel.handleGoogleSignInResult(task)
    }

    LaunchedEffect(loginState, profileComplete) {
        if (loginState is AuthViewModel.LoginState.Success) {
            onLoginSuccess()
        }
    }

    val cs = MaterialTheme.colorScheme



    Scaffold(
        backgroundColor = cs.backgroundColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            Image(
                painter = painterResource(id = R.drawable.trever_purple),
                contentDescription = "Trever Logo",
                modifier = Modifier
                    .width(129.dp)
                    .height(47.dp)
                    .align(Alignment.Center)
            )

            Button(
                border = BorderStroke(1.dp, cs.G_100),
                onClick = { launcher.launch(viewModel.getGoogleSignInIntent()) },
                colors = ButtonDefaults.buttonColors(backgroundColor = cs.backgroundColor),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.elevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-180).dp)

                    .padding(start = 24.dp, end = 24.dp, bottom = 0.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google_logo),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Google 계정으로 로그인",
                    color = cs.onSurfaceVariant
                )
            }

            when (val state = loginState) {
                is AuthViewModel.LoginState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AuthViewModel.LoginState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
}}}