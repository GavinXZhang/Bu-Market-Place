package com.example.bumarketplace

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.text.font.FontFamily
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // Splash Screen import
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants

import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell

import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.navigation.compose.currentBackStackEntryAsState

import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController



class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navController: NavHostController

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "GoogleSignIn"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()

        setContent {
            navController = rememberNavController() // Initialize navController
            BuMarketPlaceTheme {
                Scaffold(
                    bottomBar = {
                        // Show the bottom navigation bar only when not on the login screen
                        val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
                        if (currentDestination != "login") {
                            NavigationBar(navController = navController)
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = if (firebaseAuth.currentUser != null) "home" else "login"
                    ) {
                        composable("login") {
                            LoginScreen(
                                onGoogleSignInClicked = { signInWithGoogle() }
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                userName = "Gavin",
                                onLogoutClicked = { logout() } // Directly pass logout()
                            )
                        }
                        composable("profile") { ProfileScreen() } // Ensure ProfileScreen is defined
                        composable("search") { SearchScreen() }   // Ensure SearchScreen is defined
                        composable("inbox") { InboxScreen() }     // Ensure InboxScreen is defined
                        composable("selling") { SellingScreen() }
                    }
                }
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
                            navController.navigate("home")
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", authTask.exception)
                        }
                    }
            } catch (e: Exception) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun logout() {
        firebaseAuth.signOut() // Sign out from Firebase
        googleSignInClient.signOut() // Sign out from Google account
        navController.navigate("login") {
            popUpTo("home") { inclusive = true } // Clear the back stack
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
                    Spacer(modifier = Modifier.height(16.dp))
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
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { onGoogleSignInClicked() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF570303))
                    ) {
                        Text(
                            text = "Sign-Up with BU Google",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )
    }


    // class for Navigation Items
    data class BottomNavigationItem(
        val title: String,
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector,
    )

    @Composable
    fun BottomNavigationGraph(
        navController: NavHostController,
        paddingValues: PaddingValues
    ) {
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") { HomeScreen(userName = "Gavin", onLogoutClicked = {}) }
                composable("profile") { ProfileScreen() }
                composable("search") { SearchScreen() }
                composable("inbox") { InboxScreen() }
                composable("selling") { SellingScreen() }
            }
        }
    }



@Composable
    fun NavigationBar(navController: NavController) {
        // Icons for tabs
        val tabItems = listOf(
            BottomNavigationItem(
                title = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home
            ),

            BottomNavigationItem(
                title = "Profile",
                selectedIcon = Icons.Filled.AccountCircle,
                unselectedIcon = Icons.Outlined.AccountCircle
            ),

            BottomNavigationItem(
                title = "Search",
                selectedIcon = Icons.Filled.Search,
                unselectedIcon = Icons.Outlined.Search
            ),

            BottomNavigationItem(
                title = "Inbox",
                selectedIcon = Icons.Filled.Inbox,
                unselectedIcon = Icons.Outlined.Inbox
            ),

            BottomNavigationItem(
                title = "Selling",
                selectedIcon = Icons.Filled.Sell,
                unselectedIcon = Icons.Outlined.Sell
            )
        )
        // Tab Navigation
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val routes = listOf("home", "profile", "search", "inbox", "selling")

        androidx.compose.material3.NavigationBar {
            tabItems.forEachIndexed { index, bottomNavigationItem ->
                NavigationBarItem(
                    selected = index == selectedTabIndex,
                    onClick = {
                        selectedTabIndex = index
                        navController.navigate(routes[index])
                    },
                    label = {
                        Text(text = bottomNavigationItem.title)
                    },
                    icon = {
                        Icon(
                            imageVector = if (index == selectedTabIndex) {
                                bottomNavigationItem.selectedIcon
                            } else {
                                bottomNavigationItem.unselectedIcon
                            },
                            contentDescription = bottomNavigationItem.title
                        )
                    },
                    alwaysShowLabel = true
                )
            }
        }
    }

@Composable
fun HomeScreen(userName: String, onLogoutClicked: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome, $userName!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onLogoutClicked,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(text = "Logout", color = Color.White)
        }
    }
}




@Composable
    fun ProfileScreen() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Profile Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }

    @Composable
    fun SearchScreen() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Search Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }

    @Composable
    fun InboxScreen() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Inbox Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }

    @Composable
    fun SellingScreen() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Selling Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun LoginScreenPreview() {
        BuMarketPlaceTheme {
            LoginScreen(onGoogleSignInClicked = {})
        }
    }
