package com.example.bumarketplace
import androidx.compose.ui.draw.clip

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Sell

import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.border
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.navigation.compose.currentBackStackEntryAsState

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference

import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.background

// Everything here is for camera implementation
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.bumarketplace.MainActivity.Companion.TAG

//Firebase Database imports:
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.painterResource
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.google.gson.Gson


data class User(
    val userId: String,
    val name: String,
    val email: String,
    val profileImageUrl: String? = null
)

data class MarketItem(
    val title: String,
    val description: String,
    val price: Double,
    val images: List<String>,
    val category: String,
    val condition: String,
    val quantity: Int,
    val seller: String
)




class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navController: NavHostController
    private val userNameState = mutableStateOf("Guest")
    private val userEmailState = mutableStateOf("")

    private val profileImageUrlState = mutableStateOf("")


    private lateinit var database: DatabaseReference


    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "GoogleSignIn"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance().getReference()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference()

        setContent {
            navController = rememberNavController() // Initialize navController
            BuMarketPlaceTheme {
                Scaffold(
                    bottomBar = {
                        // Show the bottom navigation bar only when not on the login screen
                        val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
                        if (currentDestination != "login") {
                            NavigationBar(navController = navController, userNameState = userNameState)
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = if (firebaseAuth.currentUser != null) "home/{userNameState.value}" else "login"
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
                                onLogoutClicked = { logout() },
                                onProductClicked = { product ->
                                    // Pass parameters explicitly and encode for safety
                                    navController.navigate(
                                        "item_details/${Uri.encode(product.title)}/" +
                                                "${Uri.encode(product.description)}/" +
                                                "${product.price}/" +
                                                "${Uri.encode(product.condition)}/" +
                                                "${product.quantity}/" +
                                                "${Uri.encode(product.images.firstOrNull() ?: "")}/" +
                                                "${Uri.encode(product.seller)}"
                                    )
                                },
                                onSearchSubmitted = { query ->
                                    // Define what happens when the user submits a search query
                                    Log.d("Search", "User searched for: $query")
                                    // You can update your list or perform any other action based on the search query here
                                }
                            )
                        }
                        composable("item_details/{title}/{description}/{price}/{condition}/{quantity}/{image}/{seller}") { backStackEntry ->
                            val title = backStackEntry.arguments?.getString("title") ?: "No Title"
                            val description = backStackEntry.arguments?.getString("description") ?: "No Description"
                            val price = backStackEntry.arguments?.getString("price") ?: "0"
                            val condition = backStackEntry.arguments?.getString("condition") ?: "Unknown"
                            val quantity = backStackEntry.arguments?.getString("quantity")?.toIntOrNull() ?: 0
                            val image = backStackEntry.arguments?.getString("image") ?: ""
                            val seller = backStackEntry.arguments?.getString("seller") ?: "Unknown Seller"

                            ItemDetailsScreen(
                                title = title,
                                description = description,
                                price = price,
                                condition = condition,
                                quantity = quantity,
                                images = listOf(image), // Use the first image passed
                                onBackClicked = { navController.popBackStack() },
                                seller = seller
                            )
                        }



                        composable("profile") {
                            ProfileScreen(
                                userName = userNameState.value,
                                userEmail = userEmailState.value,
                                profileImageUrl = profileImageUrlState.value,
                                onLogoutClicked = { logout()
                                }
                            )
                        }





                        composable("cart") { CartScreen() }
                        composable("selling") { SellingScreen(navController) }
                        composable("full_selling_screen") { FullSellingScreen(navController) }
                    }
                }
            }
        }
    }

    private fun addUserToDatabase(user: User) {
        database.child("users").child(user.userId).setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "User successfully added to database.")
            }
            .addOnFailureListener {
                Log.w(TAG, "Failed to add user to database.", it)
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
                            userNameState.value = user?.displayName ?: "Guest"
                            profileImageUrlState.value = user?.photoUrl?.toString() ?: ""

                            val newUser = user?.let {
                                User(
                                    userId = it.uid,
                                    name = user.displayName ?: "N/A",
                                    email = user.email ?: "N/A",
                                    profileImageUrl = user.photoUrl.toString()
                                )
                            }
                            if (newUser != null) {
                                addUserToDatabase(newUser)
                            }

                            // Correct navigation to match the route
                            navController.navigate("home/${userNameState.value}")
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
// FirebaseManager - Inside MainActivity.kt but outside of the MainActivity class
object FirebaseManager {
    private val database = FirebaseDatabase.getInstance().getReference("items")

    fun fetchItems(onSuccess: (List<Product>) -> Unit, onFailure: (Exception) -> Unit) {
        database.get()
            .addOnSuccessListener { snapshot ->
                val itemsList = mutableListOf<Product>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(Product::class.java)
                    if (item != null) {
                        itemsList.add(item)
                    }
                }
                Log.d("FirebaseDebug", "Fetched Items: $itemsList") // Log the fetched items
                onSuccess(itemsList)
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseDebug", "Error fetching items: ${exception.message}")
                onFailure(exception)
            }
    }

    fun fetchUserItems(
        userName: String,
        onSuccess: (List<Product>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        database.get()
            .addOnSuccessListener { snapshot ->
                val userItems = mutableListOf<Product>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(Product::class.java)
                    if (item != null && item.seller == userName) {
                        userItems.add(item)
                        Log.d("UserItems", "Item: ${item.title} by ${item.seller}")
                    }
                }
                onSuccess(userItems)
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseDebug", "Error fetching user items: ${exception.message}")
                onFailure(exception)
            }
    }
}



// ItemDetails ----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    title: String = "No Title",
    description: String = "No Description",
    price: String = "0.0",
    condition: String = "Unknown",
    seller: String = "Unknown Seller",
    quantity: Int = 0,
    images: List<String> = emptyList(),
    onBackClicked: () -> Unit
) {
    Log.d("ItemDetailsDebug", "Title: $title, Description: $description, Price: $price")
    Log.d("ItemDetailsDebug", "Condition: $condition, Quantity: $quantity, Seller: $seller")
    Log.d("ItemDetailsDebug", "Images: $images")
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { /* Handle contact seller */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Contact Seller", fontSize = 18.sp, color = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Images Section
            LazyRow {
                items(images) { imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Product Image",
                        modifier = Modifier
                            .size(250.dp)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Product Details Section
            Text(title, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text("Price: $$price", fontSize = 18.sp, color = Color.Gray)
            Text("Condition: $condition", fontSize = 16.sp, color = Color.Gray)
            Text("Quantity Available: $quantity", fontSize = 16.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))
            Text(description, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // Seller Contact
            Divider(color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Seller Contact", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Seller: $seller", fontSize = 16.sp)
        }
    }
}


// ItemDetails ----------------------------------------------------------------

@Composable
fun HomeScreen(
    userName: String,
    onLogoutClicked: () -> Unit,
    onProductClicked: (Product) -> Unit,
    onSearchSubmitted: (String) -> Unit
) {
    val itemsState = remember { mutableStateOf<List<Product>>(emptyList()) }
    val searchText = remember { mutableStateOf("") }

    // Fetch items initially and whenever the search text changes
    LaunchedEffect(searchText.value) {
        FirebaseManager.fetchItems(
            onSuccess = { fetchedItems ->
                if (searchText.value.isEmpty()) {
                    itemsState.value = fetchedItems
                } else {
                    val filteredItems = fetchedItems.filter {
                        it.title.contains(searchText.value, ignoreCase = true)
                    }
                    itemsState.value = filteredItems
                    filteredItems.forEach { product ->
                        Log.d("SearchResults", "Matching Product: ${product.title}")
                    }
                }
            },
            onFailure = { exception ->
                Log.e("HomeScreen", "Failed to fetch items: ${exception.message}")
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            OutlinedTextField(
                value = searchText.value,
                onValueChange = { searchText.value = it },
                modifier = Modifier.weight(1f),
                label = { Text("Search Products") },
                singleLine = true
            )
            Button(
                onClick = {
                    onSearchSubmitted(searchText.value)
                    // Optional: You might handle additional logic here if needed
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Search")
            }
        }

        // Product List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(itemsState.value) { product ->
                ProductItem(product, onClick = { onProductClicked(product) })

            }
        }
    }
}


// Helper Composable for Filter Buttons
@Composable
fun FilterButton(text: String) {
    Button(
        onClick = {},
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF570303))
    ) {
        Text(text = text, color = Color.White)
    }
}

// Product Item Composable
@Composable
fun ProductItem(product: Product, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        // Use the first image or a placeholder if the list is empty
        val imageUrl = product.images.firstOrNull() ?: "https://via.placeholder.com/150"

        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = "Product Image",
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Product Details
        Column(modifier = Modifier.weight(1f)) {
            Text(product.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Price: $${product.price}", fontSize = 16.sp, color = Color.Gray)
            Text(product.description, fontSize = 14.sp, maxLines = 2)
        }
    }
}




// Product Data Class
data class Product(
    val title: String = "No Title",
    val description: String = "No Description",
    val price: Int = 0,
    val condition: String = "Unknown",
    val quantity: Int = 0,
    val images: List<String> = emptyList(),
    val seller: String = "Unknown Seller",
    val category: String = "" // Add this field to match Firebase structure

)



// class for Navigation Items
data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)





@Composable
fun NavigationBar(navController: NavController, userNameState: MutableState<String>) {
    val tabItems = listOf(
        BottomNavigationItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavigationItem("Selling", Icons.Filled.Sell, Icons.Outlined.Sell),
        BottomNavigationItem("Cart", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
        BottomNavigationItem("Profile", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle)

    )

    val routes = listOf("home", "selling", "cart", "profile")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    androidx.compose.material3.NavigationBar {
        tabItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedTabIndex,
                onClick = {
                    selectedTabIndex = index
                    if (routes[index] == "home") {
                        navController.navigate("home/${userNameState.value}") {
                            popUpTo("home") { inclusive = true } // Avoid duplicate stacks
                        }
                    } else {
                        navController.navigate(routes[index])
                    }
                },
                label = { Text(item.title) },
                icon = {
                    Icon(
                        imageVector = if (index == selectedTabIndex) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                }
            )
        }
    }
}

@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    profileImageUrl: String?,
    onLogoutClicked: () -> Unit
) {
    val userItems = remember { mutableStateOf<List<Product>>(emptyList()) }

    // Fetch items on initialization or when userName changes
    LaunchedEffect(userName) {
        FirebaseManager.fetchUserItems(userName, onSuccess = {
            userItems.value = it
        }, onFailure = {
            // Handle failure if needed
            Log.e("ProfileScreen", "Failed to fetch items for user $userName")
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileHeader(userName, profileImageUrl)
        Spacer(modifier = Modifier.height(16.dp))
        ProfileDetail("Email", userEmail)
        if (userItems.value.isNotEmpty()) {
            Text("Your Listings", style = MaterialTheme.typography.titleLarge)
            userItems.value.forEach { product ->
                Text(product.title, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Text("You have no listings yet.", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onLogoutClicked,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun ProfileHeader(userName: String, profileImageUrl: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (profileImageUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(profileImageUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Default Profile Picture",
                tint = Color.Gray,
                modifier = Modifier.size(72.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(userName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun ProfileDetail(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}



@Composable
fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(vertical = 8.dp))
}


@Composable
fun ListItemRow(items: List<String>) {
    LazyRow {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .size(120.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDEDED)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = item,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}





@Composable
fun CartScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Cart Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SellingScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Add padding for better spacing
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Placeholder for an empty state image or illustration
            Image(
                painter = painterResource(id = R.drawable.logobu), // Replace with your drawable resource
                contentDescription = "Empty State Illustration",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 16.dp)
            )

            // Header Title with enhanced styling
            Text(
                text = "Start Selling",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Description to guide the user
            Text(
                text = "No items listed yet. Tap below to add your first item!",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // "List an Item" Button with enhanced styling
            Button(
                onClick = { navController.navigate("full_selling_screen") }, // Navigate to Full Selling Screen
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp), // Rounded button corners
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add, // Icon for the button
                    contentDescription = "Add Icon",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "List an Item",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}



// All Functions from this point on are helper to SellingScreen
@Composable
fun FullSellingScreen(navController: NavController) {
    // These variables are passed in to there respective helper functions so we can creating accurate listings.

    // For saving all uploaded images
    val selectedImageUris = remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Title to listing
    val titleText = remember { mutableStateOf("") }

    // 2 categories of listings Course Materials & Supplies and other user has to pick from the 2
    val selectedCategory = remember { mutableStateOf("Course Materials & Supplies") }
    // The condition of the item
    val selectedCondition = remember { mutableStateOf("Brand New ") }

    // How many of the listing item
    val itemQuantity = remember { mutableStateOf("") }

    // Description of the item
    val description = remember { mutableStateOf("") }

    // Price of the item
    val price = remember { mutableStateOf("") }

    // This is actually the listers information. For off campus we don't display address
    val selectedAddress = remember { mutableStateOf("Warren Towers") }
    val offCampusAddress = remember { mutableStateOf("") }

    // If lister accepts returns
    val selectedReturn = remember { mutableStateOf("Yes Returns") }

    // Listers information we need to know to give them money
    val cardHolderName = remember { mutableStateOf("") }
    val cardNumber = remember { mutableStateOf(TextFieldValue()) }
    val expiryDate = remember { mutableStateOf(TextFieldValue()) }
    val cvv = remember { mutableStateOf("") }

    var message by remember { mutableStateOf("") }

    // function that checks to make all fields are filled
    fun validateFields(): Boolean {
        // Basic validation checks
        // For demonstration, we assume all must be non-empty where applicable
        if (selectedImageUris.value.isEmpty()) return false
        if (titleText.value.isBlank()) return false
        if (selectedCategory.value.isBlank()) return false
        if (selectedCondition.value.isBlank()) return false
        if (itemQuantity.value.isBlank()) return false
        if (description.value.isBlank()) return false
        if (price.value.isBlank()) return false
        // If the user selected "Off Campus", ensure offCampusAddress is provided
        if (selectedAddress.value == "Off Campus" && offCampusAddress.value.isBlank()) return false
        if (selectedReturn.value.isBlank()) return false
        if (cardHolderName.value.isBlank()) return false
        if (cardNumber.value.text.isBlank()) return false
        if (expiryDate.value.text.isBlank()) return false
        if (cvv.value.isBlank()) return false

        return true
    }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val sellerName = currentUser?.displayName ?: "Unknown Seller"


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp) // Additional padding to ensure visibility
    ) {
        PhotoSection(selectedImageUris = selectedImageUris)
        Spacer(modifier = Modifier.height(16.dp))

        TitleSection(titleText = titleText)
        Spacer(modifier = Modifier.height(16.dp))

        CategorySection(selectedCategory = selectedCategory)
        Spacer(modifier = Modifier.height(16.dp))

        ConditionSection(selectedCondition = selectedCondition)
        Spacer(modifier = Modifier.height(16.dp))

        QuantitySection(itemQuantity = itemQuantity)
        Spacer(modifier = Modifier.height(16.dp))

        DescriptionSection(description = description)
        Spacer(modifier = Modifier.height(16.dp))

        PricingSection(price = price)
        Spacer(modifier = Modifier.height(16.dp))

        AddressSection(selectedAddress = selectedAddress, offCampusAddress = offCampusAddress)
        Spacer(modifier = Modifier.height(16.dp))

        ReturnSection(selectedReturn = selectedReturn)
        Spacer(modifier = Modifier.height(16.dp))

        PaymentSection(
            cardHolderName = cardHolderName,
            cardNumber = cardNumber,
            expiryDate = expiryDate,
            cvv = cvv
        )
        Spacer(modifier = Modifier.height(16.dp))

        // if all fields are filled we print variables and navigate back to previous screen and listings are added
        if (message.isNotBlank()) {
            Text(
                text = message,
                color = if (message.contains("Success")) Color.Green else Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                if (validateFields()) {
                    // Print all variables to console
                    println("Selected Image URIs: ${selectedImageUris.value}")
                    println("Title: ${titleText.value}")
                    println("Category: ${selectedCategory.value}")
                    println("Condition: ${selectedCondition.value}")
                    println("Quantity: ${itemQuantity.value}")
                    println("Description: ${description.value}")
                    println("Price: ${price.value}")
                    println("Address: ${selectedAddress.value}")
                    println("OffCampus Address: ${offCampusAddress.value}")
                    println("Return Policy: ${selectedReturn.value}")
                    println("Card Holder Name: ${cardHolderName.value}")
                    println("Card Number: ${cardNumber.value.text}")
                    println("Expiry Date: ${expiryDate.value.text}")
                    println("CVV: ${cvv.value}")

                    message = "Success! Listing is ready."

                    val newItem = MarketItem(
                        title = titleText.value,
                        description = description.value,
                        price = price.value.toDouble(),
                        images = selectedImageUris.value.map { it.toString() }, // Assuming URIs can be converted to strings
                        category = selectedCategory.value,
                        condition = selectedCondition.value,
                        quantity = itemQuantity.value.toInt(),
                        seller = sellerName
                    )
                    addItemToDatabase(newItem)
                    // Navigate back to the original selling screen
                    navController.popBackStack()

                } else {
                    message = "Please fill out all fields."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("List your item")
        }
    }
}

fun addItemToDatabase(item: MarketItem) {
    val database = FirebaseDatabase.getInstance().getReference()

    val TAG = "MainActivity"

    database.child("items").push().setValue(item)
        .addOnSuccessListener {
            Log.d(TAG, "Item successfully added to database.")
        }
        .addOnFailureListener {
            Log.w(TAG, "Failed to add item to database.", it)
        }
}


@Composable
fun PhotoSection(selectedImageUris: MutableState<List<Uri>>) {
    val maxImages = 5 // Max number of images
    var showWarning by remember { mutableStateOf(false) } // Use this to know if user surpassing num of images

    val pickImages = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            val totalImages = selectedImageUris.value.size + uris.size
            if (totalImages <= maxImages) {
                selectedImageUris.value += uris
                showWarning = false
            } else {
                val remainingSlots = maxImages - selectedImageUris.value.size
                selectedImageUris.value += uris.take(remainingSlots)
                showWarning = true
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        SectionHeader(title = "Photos")

        if (showWarning) {
            Text(
                text = "You can only select up to $maxImages images.",
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        LazyRow {
            itemsIndexed(selectedImageUris.value) { index, uri ->
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

                    IconButton(
                        onClick = {
                            selectedImageUris.value = selectedImageUris.value.toMutableList().apply {
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
                    // Puts number in corner of images
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

        // Displays images and checks to see if there over maxImages
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (selectedImageUris.value.size < maxImages) {
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
}


@Composable
fun TitleSection(titleText: MutableState<String>) {
    // User can only write at max 80 characters for a title and we save that
    val maxChars = 80
    Column(modifier = Modifier.padding(8.dp)) {
        SectionHeader(title = "Title")
        TextField(
            value = titleText.value,
            onValueChange = {
                if (it.length <= maxChars) {
                    titleText.value = it
                }
            },
            placeholder = { Text("Enter a title for your item") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Text(
            text = "${titleText.value.length} / $maxChars characters",
            style = MaterialTheme.typography.bodySmall,
            color = if (titleText.value.length >= maxChars) Color.Red else Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class) // Necessary for drop down beta menu
@Composable
fun CategorySection(selectedCategory: MutableState<String>) {
    val options = listOf("Course Materials & Supplies", "Student Life & Misc.")
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(8.dp)) {
        SectionHeader(title = "Category")
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedCategory.value,
                onValueChange = {},
                readOnly = true,
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
                            selectedCategory.value = option
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionSection(selectedCondition: MutableState<String>) {
    // Find condition of item
    val options = listOf("Brand New ", "Like New", "Very Good", "Good", "Acceptable" )
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(8.dp)) {
        SectionHeader(title = "Condition")
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedCondition.value,
                onValueChange = {},
                readOnly = true,
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
                            selectedCondition.value = option
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun QuantitySection(itemQuantity: MutableState<String>) {
    // Sets quanitiy of item
    var isError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(8.dp)) {
        SectionHeader(title = "Quantity")
        OutlinedTextField(
            value = itemQuantity.value,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    itemQuantity.value = newValue
                    isError = false
                } else {
                    isError = true
                }
            },
            label = { Text("Enter Quantity") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            isError = isError,
            singleLine = true,
            supportingText = {
                if (isError) {
                    Text("Please enter a valid positive whole number.", color = Color.Red)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (itemQuantity.value.isNotEmpty() && !isError) {
            Text(
                text = "Current Quantity: ${itemQuantity.value}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


@Composable
fun DescriptionSection(description: MutableState<String>) {
    // Description for title
    val maxChars = 500
    val remainingChars = maxChars - description.value.length

    Column(modifier = Modifier.padding(8.dp)) {
        SectionHeader(title = "Description")
        OutlinedTextField(
            value = description.value,
            onValueChange = { newValue ->
                if (newValue.length <= maxChars) {
                    description.value = newValue
                }
            },
            label = { Text("Enter description (max $maxChars characters)") },
            singleLine = false,
            maxLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Text(
            text = "Remaining characters: $remainingChars",
            style = MaterialTheme.typography.bodySmall,
            color = if (remainingChars < 0) Color.Red else Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


@Composable
fun PricingSection(price: MutableState<String>) {
    // Sets price for everything
    var isError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(8.dp)) {
        SectionHeader(title = "Pricing")

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
                value = price.value,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                        price.value = newValue
                        isError = false
                    } else {
                        isError = true
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                isError = isError,
                label = { Text("Enter Price") },
                singleLine = true,
                modifier = Modifier.width(150.dp)
            )
        }

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
fun AddressSection(selectedAddress: MutableState<String>, offCampusAddress: MutableState<String>) {
    // Gets address of seller
    val options = listOf(
        "Warren Towers", "Bay State Road", "South Campus",
        "Myles", "Hojo", "The Towers", "StuVi1", "StuVi2", "Off Campus"
    )
    var expanded by remember { mutableStateOf(false) }
    val characterLimit = 50

    Column(modifier = Modifier.padding(8.dp)) {
        SectionHeader(title = "Address")
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedAddress.value,
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

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedAddress.value = option
                            if (option != "Off Campus") offCampusAddress.value = ""
                            expanded = false
                        }
                    )
                }
            }
        }

        if (selectedAddress.value == "Off Campus") {
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = offCampusAddress.value,
                onValueChange = { newInput ->
                    if (newInput.length <= characterLimit) {
                        offCampusAddress.value = newInput
                    }
                },
                label = { Text("Enter Off Campus Address") },
                placeholder = { Text("e.g., 123 Main St, Boston") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = "${offCampusAddress.value.length} / $characterLimit characters",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnSection(selectedReturn: MutableState<String>) {
    // If they accept returns
    val options = listOf("Yes Returns", "No Returns")
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(8.dp)) {
        SectionHeader(title = "30 Day Return Policy")
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedReturn.value,
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

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedReturn.value = option
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Selected Policy: ${selectedReturn.value}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


@Composable
fun SectionHeader(title: String) {
    // Makes sections look pretty
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}


@Composable
fun PaymentSection(
    cardNumber: MutableState<TextFieldValue>,
    cardHolderName: MutableState<String>,
    expiryDate: MutableState<TextFieldValue>,
    cvv: MutableState<String>
) {
    // Formatting and making and displaying card
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
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
                    text = cardHolderName.value.ifBlank { "Make it Easy" },
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (cardNumber.value.text.isBlank()) "XXXX XXXX XXXX XXXX"
                    else cardNumber.value.text,
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = cardHolderName.value,
            onValueChange = { cardHolderName.value = it },
            label = { Text("Card Holder Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = cardNumber.value,
            onValueChange = { newValue ->
                cardNumber.value = formatCardNumberWithCaret(newValue)
            },
            label = { Text("Card Number") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = expiryDate.value,
            onValueChange = { newValue ->
                expiryDate.value = formatExpiryDateWithCaret(newValue)
            },
            label = { Text("Expiry Date (MM/YY)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = cvv.value,
            onValueChange = {
                if (it.length <= 3 && it.all { char -> char.isDigit() }) {
                    cvv.value = it
                }
            },
            label = { Text("CVV") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}






// These functions help with formatting for PaymentSection
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



















@Composable
fun LoginScreen(onGoogleSignInClicked: () -> Unit) {
    // Load the Lottie animation
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.firstpageanimation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

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
                // Add Lottie Animation Here
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

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


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    BuMarketPlaceTheme {
        LoginScreen(onGoogleSignInClicked = {})
    }
}
