package com.example.bumarketplace

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CartViewModelTest {

    private lateinit var cartViewModel: CartViewModel

    @Before
    fun setUp() {
        cartViewModel = CartViewModel()
    }

    @Test
    fun `test adding product to cart`() {
        // Arrange
        val product = Product(
            title = "Test Product",
            description = "This is a test product.",
            price = 100,
            condition = "New",
            quantity = 10,
            images = listOf("https://via.placeholder.com/150"),
            seller = "Test Seller"
        )

        // Act
        cartViewModel.addToCart(product)

        // Assert
        assertEquals(1, cartViewModel.cartItems.size)
        assertEquals(product.title, cartViewModel.cartItems[0].product.title)
        assertEquals(1, cartViewModel.cartItems[0].quantity)
    }

    @Test
    fun `test increasing quantity of product in cart`() {
        // Arrange
        val product = Product(
            title = "Test Product",
            description = "This is a test product.",
            price = 100,
            condition = "New",
            quantity = 10,
            images = listOf("https://via.placeholder.com/150"),
            seller = "Test Seller"
        )
        cartViewModel.addToCart(product)

        // Act
        cartViewModel.addToCart(product)

        // Assert
        assertEquals(1, cartViewModel.cartItems.size)
        assertEquals(2, cartViewModel.cartItems[0].quantity)
    }

    @Test
    fun `test removing product from cart`() {
        // Arrange
        val product = Product(
            title = "Test Product",
            description = "This is a test product.",
            price = 100,
            condition = "New",
            quantity = 10,
            images = listOf("https://via.placeholder.com/150"),
            seller = "Test Seller"
        )
        cartViewModel.addToCart(product)

        // Act
        cartViewModel.removeFromCart(cartViewModel.cartItems[0])

        // Assert
        assertTrue(cartViewModel.cartItems.isEmpty())
    }

    @Test
    fun `test calculating total price`() {
        // Arrange
        val product1 = Product(
            title = "Product 1",
            description = "This is product 1.",
            price = 100,
            condition = "New",
            quantity = 10,
            images = listOf("https://via.placeholder.com/150"),
            seller = "Seller 1"
        )
        val product2 = Product(
            title = "Product 2",
            description = "This is product 2.",
            price = 50,
            condition = "Used",
            quantity = 5,
            images = listOf("https://via.placeholder.com/150"),
            seller = "Seller 2"
        )
        cartViewModel.addToCart(product1)
        cartViewModel.addToCart(product2)

        // Act
        val total = cartViewModel.calculateTotal()

        // Assert
        assertEquals(150, total)
    }
}
