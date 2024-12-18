# BU Market

**Boston University CS501 Final Project**  
**Team Members**: Zander Reid, Abdullahi Nu, Wei Zhang

## Description

BU Market is a mobile application built with Jetpack Compose, designed to help Boston University students buy, sell, and exchange items. Think of it as the eBay for BU—created by students, for students.

Our goal with BU Market is to provide a universal platform where Boston University students can trade college essentials like furniture, electronics, clothing, and textbooks. This initiative reduces waste by giving unused items a second life while helping students avoid exorbitant prices for everyday necessities.

### Our Purpose
If you take a look at the Boston University subreddit, you’ll find countless posts of students trying to sell everything from tickets and furniture to textbooks and school supplies.
[BU Subreddit](https://www.reddit.com/r/BostonU/search/?q=selling&cId=e17eee43-97c8-4605-bac6-d0666b4bdac1&iId=1b0bbca6-63bf-4e12-be28-66911bcbe8b9&sort=new).


But here’s the problem: selling books back to Barnes & Noble often leads to getting ripped off, Ticketmaster is too broad and doesn’t focus on the BU community, and old textbooks are only useful to BU students taking specific courses—finding those naturally is a challenge.

There’s a clear demand for a BU-specific marketplace where students can easily buy and sell to one another. Imagine a platform where you could trade used items without relying on big businesses or discarding them when they’re no longer needed. If Target, Ticketmaster, and Barnes & Noble are too expensive (as they often are), BU Market provides a cheaper, more sustainable alternative. This not only saves money but also helps reduce waste by recycling items within the BU community.

### Core Features  
1. **User Authentication**  
   - Secure login via Google Sign-In using Firebase Authentication.  

2. **Item Listings**  
   - Sellers can list items with a description, price, and images.  
   - Categories: "Academic Supplies" and "Lifestyle & Essentials".  

3. **Real-Time Updates**  
   - Item availability and cart updates synced in real-time using **Firebase Firestore**.  

4. **Cart Management**  
   - Buyers can add items to their cart for purchase.  

5. **Meetups and Locations**  
   - Arrange meet-ups at predefined dorm locations: Warren, Towers, and Baystate.  

6. **Payment Validation**  
   - Secure input and validation of payment details to enable transactions.  

7. **Dark Mode**  
   - Supports both light and dark themes with Jetpack Compose theming.  

8. **Responsive Design**  
   - The UI adapts to various screen sizes while maintaining clean navigation. 

# Getting Started 
Follow these instructions to build and run the project

### Requirements  
- **Android Version**: 8.0 (API Level 26) or higher.
- **Development Tools**: Android Studio, Jetpack Compose. 
- **IOS Version**: Coming in 2099. 

### Android 
### Setup Instructions  
1. **Clone the Repository**  
   Run the following command in your terminal:  
   ```bash
   git clone https://github.com/GavinXZhang/Bu-Market-Place.git
   
2. **Google Services Setup**  
    - **Set up Firebase** in your Google Cloud console.
    - **Download** the `google-services.json` file.
    - Place the file in the `/app` directory.
    
3. **Run the App**
    - Open the project in **Android Studio.**
    - Connect an **Android device** or **emulator.**
    - Run the project.

4. **Dependencies**
    <br> Ensure the following dependencies are set up in your project:
    - Jetpack Compose
    - Firebase Authentication
    - Firebase Firestore
    - Google Play Services

---

## Usage

### For Buyers
1. Log in with your **BU Google account**.
2. Browse items under the **Home** or **Search** tabs.
3. Add items to your cart and **proceed to checkout**.

### For Sellers
1. Log in with your **BU Google account**.
2. Navigate to the **Selling** tab to list a new item.
3. Input the item’s name, price, description, and upload images.
4. Submit the listing to make it available in the marketplace.

---

# Copyright


## APIs and Libraries

- **Firebase Authentication**: Google Sign-In for secure login.
- **Firebase Firestore**: Real-time database for item listings, cart management, and meet-up scheduling.
- **Jetpack Compose**: Declarative UI toolkit for modern Android development.
- **Lottie**: Animation support for splash screens.

# Developer's Note
## Created with
- Jetpack Compose (Kotlin)

# Credits
- ZReidPie – Lead Front-End Developer

ZReidPie spearheaded the development of the user interface using Jetpack Compose, ensuring a clean, modern, and responsive design. Their focus on usability and performance resulted in a seamless and visually polished user experience, aligning with best practices in mobile UI development.

- GavinXZhang – Full-Stack Developer & User Authentication Specialist

GavinXZhang implemented robust user authentication systems, integrating secure login and registration functionalities. In addition to authentication, they contributed significantly to both front-end and back-end development, bridging the gap between design and functionality to create a cohesive user experience.

- Abdul-pste – Back-End & Database Developer

Abdul-pste architected the application’s back-end infrastructure and database systems, ensuring optimal performance, data integrity, and scalability. Their expertise in server-side development provided a strong foundation for the project’s core functionality and data management.


### License
This project is developed as part of the **Boston University CS501 Final Project** and is subject to BU academic guidelines.

### Assets and Presentation
Last Presentation for this project: https://docs.google.com/presentation/d/1Mfi6qC7S97VOx6a49Fidfni1VdfLTOjWVbxh1Nmyy5Y/edit?usp=sharing
