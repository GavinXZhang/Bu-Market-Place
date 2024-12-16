package com.example.bumarketplace

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.background

// Everything here is for camera implmentation
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyRow
import coil.compose.rememberImagePainter
import androidx.compose.material.icons.filled.CameraAlt
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation


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
                        startDestination = if (firebaseAuth.currentUser != null) "home/{userName}" else "login"
                    ) {
                        composable("login") {
                            LoginScreen(
                                onGoogleSignInClicked = { signInWithGoogle() }
                            )
                        }
                        composable("home/{userName}") { backStackEntry ->
                            val userName = backStackEntry.arguments?.getString("userName") ?: "Guest"
                            HomeScreen(
                                userName = userName,
                                onLogoutClicked = { logout() }
                            )
                        }
                        composable("profile") { ProfileScreen() }
                        composable("search") { SearchScreen() }
                        composable("inbox") { InboxScreen() }
                        composable("selling") { SellingScreen(navController) }
                        composable("full_selling_screen") { FullSellingScreen() }
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
                            val user = firebaseAuth.currentUser
                            val userName = user?.displayName ?: "Guest" // Get user name
                            navController.navigate("home/$userName") // Pass user name to HomeScreen
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
            composable("selling") { SellingScreen(navController) }
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
fun SellingScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Selling Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("full_selling_screen") }, // Navigate to Full Selling Screen
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text("List an Item", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun FullSellingScreen() {
    val maxImages = 5 // Set the maximum number of images
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showWarning by remember { mutableStateOf(false) } // Warning for max limit

    // Define the photo picker activity result contract for multiple images
    val pickImages = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            val totalImages = selectedImageUris.size + uris.size
            if (totalImages <= maxImages) {
                selectedImageUris = selectedImageUris + uris
                showWarning = false
            } else {
                val remainingSlots = maxImages - selectedImageUris.size
                selectedImageUris = selectedImageUris + uris.take(remainingSlots)
                showWarning = true
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Photo Section
        SectionHeader(title = "Photos")
        Column {
            if (showWarning) {
                Text(
                    text = "You can only select up to $maxImages images.",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            LazyRow {
                itemsIndexed(selectedImageUris) { index, uri ->
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .padding(end = 8.dp)
                            .background(Color.Gray, shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize()
                        )

                        // "X" Button for removing the image
                        IconButton(
                            onClick = {
                                selectedImageUris = selectedImageUris.toMutableList().apply {
                                    removeAt(index)
                                }
                            },
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopEnd)
                                .background(Color.Red, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Image",
                                tint = Color.White
                            )
                        }

                        // Number indicator for the image
                        Text(
                            text = "${index + 1}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .background(
                                    Color.Black.copy(alpha = 0.6f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(4.dp)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (selectedImageUris.size < maxImages) {
                    IconButton(onClick = { pickImages.launch("image/*") }) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Pick Images",
                            tint = Color.White,
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.DarkGray, shape = RoundedCornerShape(8.dp))
                        )
                    }
                } else {
                    Text(
                        text = "Image limit reached.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        TitleSection()

        // Category Section
        CategorySection()

        // Condition Section
        ConditionSection()

        // Item Quantity Section
        QuantitySection()

        // Description Section
        DescriptionSection()

        // Pricing Section
        PricingSection()

        // Shipping Section
        AddressSection()

        // Return Section
        ReturnSection()

        // Payment Section
        PaymentSection()


        // Preferences Section
        SectionHeader(title = "Preferences")
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Payment Methods: Payments managed by eBay")
            Text("Handling Time: 2 business days")
            Text("Item Location: United States, 10026 (New York, New York)")
            Text("Return Policy: No returns accepted unless not as described")
        }

        // Final Buttons
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { /* Handle listing item */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("List your item")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { /* Handle preview */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("Preview")
        }
    }
}


@Composable
fun TitleSection() {
    var titleText by remember { mutableStateOf("") } // Blank initial value
    val maxChars = 80 // Character limit

    Column(modifier = Modifier.padding(8.dp)) {
        // Section Header
        SectionHeader(title = "Title")

        // Text Field for the title
        TextField(
            value = titleText,
            onValueChange = {
                if (it.length <= maxChars) { // Limit the input to 80 characters
                    titleText = it
                }
            },
            placeholder = { Text("Enter a title for your item") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Character Count Display
        Text(
            text = "${titleText.length} / $maxChars characters",
            style = MaterialTheme.typography.bodySmall,
            color = if (titleText.length >= maxChars) Color.Red else Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Needed if you want to do ExposedDropdownMenuBox
@Composable
fun CategorySection() {
    val options = listOf("Course Materials & Supplies", "Student Life & Misc.") // List of options
    var expanded by remember { mutableStateOf(false) } // State to manage menu visibility
    var selectedOption by remember { mutableStateOf(options[0]) } // Default selected option

    Column(modifier = Modifier.padding(8.dp)) {
        // Section Header
        SectionHeader(title = "Category")

        // Exposed Dropdown Menu
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true, // Prevent manual input
                label = { Text("Select Category") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedOption = option // Update the selected option
                            expanded = false // Close the menu
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Needed if you want to do ExposedDropdownMenuBox
@Composable
fun ConditionSection() {
    val options = listOf("Brand New ", "Like New", "Very Good", "Good", "Acceptable" ) // List of options
    var expanded by remember { mutableStateOf(false) } // State to manage menu visibility
    var selectedOption by remember { mutableStateOf(options[0]) } // Default selected option

    Column(modifier = Modifier.padding(8.dp)) {
        // Section Header
        SectionHeader(title = "Condition")

        // Exposed Dropdown Menu
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true, // Prevent manual input
                label = { Text("Select Condition") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedOption = option // Update the selected option
                            expanded = false // Close the menu
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuantitySection() {
    var quantity by remember { mutableStateOf("") } // State to hold the input value
    var isError by remember { mutableStateOf(false) } // State to manage input validation error

    Column(modifier = Modifier.padding(8.dp)) {
        // Section Header
        SectionHeader(title = "Quantity")

        // Input Field for Quantity
        OutlinedTextField(
            value = quantity,
            onValueChange = { newValue ->
                // Check if the input is a valid positive integer
                if (newValue.all { it.isDigit() }) {
                    quantity = newValue
                    isError = false
                } else {
                    isError = true // Mark as error for invalid input
                }
            },
            label = { Text("Enter Quantity") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number, // Show number-only keyboard
                imeAction = ImeAction.Done
            ),
            isError = isError, // Highlight the field if there's an error
            singleLine = true,
            supportingText = {
                if (isError) {
                    Text("Please enter a valid positive whole number.", color = Color.Red)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Display the currently entered quantity (optional)
        if (quantity.isNotEmpty() && !isError) {
            Text(
                text = "Current Quantity: $quantity",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun DescriptionSection() {
    val maxChars = 500 // Maximum character limit
    var description by remember { mutableStateOf("") } // State to store the input text
    val remainingChars = maxChars - description.length // Remaining characters

    Column(modifier = Modifier.padding(8.dp)) {
        // Section Header
        SectionHeader(title = "Description")

        // TextField with character limit
        OutlinedTextField(
            value = description,
            onValueChange = { newValue ->
                if (newValue.length <= maxChars) { // Ensure input stays within the limit
                    description = newValue
                }
            },
            label = { Text("Enter description (max $maxChars characters)") },
            singleLine = false, // Allows multiline input
            maxLines = 5, // Set maximum visible lines
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Adjust height for better input space
        )

        // Character count feedback
        Text(
            text = "Remaining characters: $remainingChars",
            style = MaterialTheme.typography.bodySmall,
            color = if (remainingChars < 0) Color.Red else Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun PricingSection() {
    var price by remember { mutableStateOf("") } // State to hold the input value
    var isError by remember { mutableStateOf(false) } // State to handle validation errors

    Column(modifier = Modifier.padding(8.dp)) {
        // Section Header
        SectionHeader(title = "Pricing")

        // Row for "Price" label and input field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Price: ",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = price,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        // Accept only digits (no decimals, letters, or negatives)
                        price = newValue
                        isError = false
                    } else if (newValue.isEmpty()) {
                        price = ""
                        isError = false
                    } else {
                        isError = true
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number, // Show number-only keyboard
                    imeAction = ImeAction.Done
                ),
                isError = isError,
                label = { Text("Enter Price") },
                singleLine = true,
                modifier = Modifier.width(150.dp)
            )
        }

        // Error message for invalid input
        if (isError) {
            Text(
                text = "Please enter a valid positive whole number.",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressSection() {
    // List of predefined options
    val options = listOf(
        "Warren Towers", "Bay State Road", "South Campus",
        "Myles", "Hojo", "The Towers", "StuVi1", "StuVi2", "Off Campus"
    )
    var expanded by remember { mutableStateOf(false) } // Dropdown state
    var selectedOption by remember { mutableStateOf(options[0]) } // Default selection

    // State for the "Off Campus" address input
    var offCampusAddress by remember { mutableStateOf("") }
    val characterLimit = 50

    Column(modifier = Modifier.padding(8.dp)) {
        // Section Header
        SectionHeader(title = "Address")

        // Exposed Dropdown Menu
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            // Dropdown Trigger
            TextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Address") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            // Dropdown Options
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedOption = option // Update the selected option
                            if (option != "Off Campus") offCampusAddress = "" // Reset Off Campus input
                            expanded = false // Close the menu
                        }
                    )
                }
            }
        }

        // Show TextField for "Off Campus" selection
        if (selectedOption == "Off Campus") {
            Spacer(modifier = Modifier.height(8.dp)) // Add spacing
            TextField(
                value = offCampusAddress,
                onValueChange = { newInput ->
                    // Ensure input doesn't exceed character limit
                    if (newInput.length <= characterLimit) {
                        offCampusAddress = newInput
                    }
                },
                label = { Text("Enter Off Campus Address") },
                placeholder = { Text("e.g., 123 Main St, Boston") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Character count indicator
            Text(
                text = "${offCampusAddress.length} / $characterLimit characters",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnSection() {
    // List of predefined options
    val options = listOf("Yes Returns", "No Returns")
    var expanded by remember { mutableStateOf(false) } // Dropdown state
    var selectedOption by remember { mutableStateOf(options[0]) } // Default selection

    Column(modifier = Modifier.padding(8.dp)) {
        // Section Header
        SectionHeader(title = "30 Day Return Policy")

        // Exposed Dropdown Menu
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            // Dropdown Trigger
            TextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Return Policy") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            // Dropdown Options
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedOption = option // Update the selected option
                            expanded = false // Close the menu
                        }
                    )
                }
            }
        }

        // Display selected option (optional)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Selected Policy: $selectedOption",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSection() {
    var cardNumber by remember { mutableStateOf(TextFieldValue()) }
    var cardHolderName by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf(TextFieldValue()) }
    var cvv by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Payment Card Header
        Text(
            text = "Payment Card Details",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF17479E))
                .padding(16.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Payment Card Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFF17479E), shape = RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "CARD HOLDER", color = Color.White, fontSize = 12.sp)
                Text(
                    text = cardHolderName.ifBlank { "Make it Easy" },
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (cardNumber.text.isBlank()) "XXXX XXXX XXXX XXXX"
                    else cardNumber.text,
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card Holder Name Input
        OutlinedTextField(
            value = cardHolderName,
            onValueChange = { cardHolderName = it },
            label = { Text("Card Holder Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card Number Input
        OutlinedTextField(
            value = cardNumber,
            onValueChange = { newValue ->
                cardNumber = formatCardNumberWithCaret(newValue)
            },
            label = { Text("Card Number") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Expiry Date Input
        OutlinedTextField(
            value = expiryDate,
            onValueChange = { newValue ->
                expiryDate = formatExpiryDateWithCaret(newValue)
            },
            label = { Text("Expiry Date (MM/YY)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // CVV Input
        OutlinedTextField(
            value = cvv,
            onValueChange = {
                if (it.length <= 3 && it.all { char -> char.isDigit() }) {
                    cvv = it
                }
            },
            label = { Text("CVV") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Formats the card number with spaces while maintaining the caret position.
 */
fun formatCardNumberWithCaret(input: TextFieldValue): TextFieldValue {
    val digitsOnly = input.text.filter { it.isDigit() }.take(16) // Keep digits only (max 16)
    val formatted = digitsOnly.chunked(4).joinToString(" ") // Add spaces every 4 digits

    // Calculate new caret position
    val newCursorPosition = input.selection.start + formatted.count { it == ' ' } - input.text.count { it == ' ' }

    return TextFieldValue(
        text = formatted,
        selection = TextRange(newCursorPosition.coerceAtMost(formatted.length))
    )
}

/**
 * Formats the expiry date with a slash (MM/YY) while maintaining the caret position.
 */
fun formatExpiryDateWithCaret(input: TextFieldValue): TextFieldValue {
    val digitsOnly = input.text.filter { it.isDigit() }.take(4) // Max 4 digits (MMYY)
    val formatted = when {
        digitsOnly.length <= 2 -> digitsOnly
        else -> "${digitsOnly.substring(0, 2)}/${digitsOnly.substring(2)}"
    }

    // Calculate new caret position
    val newCursorPosition = if (input.selection.start <= 2) {
        input.selection.start
    } else {
        input.selection.start + 1
    }

    return TextFieldValue(
        text = formatted,
        selection = TextRange(newCursorPosition.coerceAtMost(formatted.length))
    )
}



















@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    BuMarketPlaceTheme {
        LoginScreen(onGoogleSignInClicked = {})
    }
}
