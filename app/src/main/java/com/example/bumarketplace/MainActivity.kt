package com.example.bumarketplace

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bumarketplace.ui.theme.BuMarketPlaceTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource


class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "GoogleSignIn"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // From google-services.json
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()

        setContent {
            BuMarketPlaceTheme {
                LoginScreen(onGoogleSignInClicked = { signInWithGoogle() })
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(Exception::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { authTask ->
                        if (authTask.isSuccessful) {
                            Log.d(TAG, "signInWithCredential:success")
                            val user = firebaseAuth.currentUser
                            // Navigate to the main part of your app
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", authTask.exception)
                        }
                    }
            } catch (e: Exception) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }
}

@Composable
fun LoginScreen(onGoogleSignInClicked: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Add your logo here
                Image(
                    painter = painterResource(id = R.drawable.logobu), // Replace 'your_logo' with your image file name
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp) // Adjust size as needed
                )
                Spacer(modifier = Modifier.height(16.dp)) // Space between the logo and the text
                Text(
                    text = "Discover BUMarket",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Connect with BU students to buy and sell",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { onGoogleSignInClicked() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Sign-Up with BU Google",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    BuMarketPlaceTheme {
        LoginScreen(onGoogleSignInClicked = {})
    }
}
