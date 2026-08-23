# FlexiFit Android App - Personalized Fitness & Nutrition Planning

A modern, high-performance **Android application** built with Kotlin that serves as the mobile frontend for the FlexiFit personalized fitness ecosystem. This app connects to the FlexiFit REST API to deliver dynamic workout plans, intelligent nutrition tracking, and a unified calendar interface.

---

## 📱 Tech Stack

- **Language:** Kotlin
- **UI Framework:** Android View System with Material Design 3
- **Architecture:** MVVM (Model-View-ViewModel) with Repository pattern
- **Network:** Retrofit 2 with OkHttp Interceptors
- **Authentication:** Firebase Authentication with JWT token management
- **Local Storage:** SharedPreferences for secure token storage and user preferences
- **Background Tasks:** WorkManager for workout/meal/water reminders
- **Charts:** MPAndroidChart for fitness progress visualization
- **Image Loading:** Glide for efficient image caching and loading

---

## 📲 Core Features

### 🔐 Authentication & User Management
- **Email/Password Sign-up & Login** with Firebase Authentication
- **Google Sign-In** integration for quick access
- **Secure JWT Token Management** - automatic token refresh on expiry
- **Email Verification** flow to confirm user accounts

### 🏋️ Intelligent Workout System
- **Adaptive 28-Day Programs** (Push/Pull/Legs, Upper/Lower, Rehab)
- **Warmup & Main Workout** sections with exercise details
- **Exercise Library** with images, sets, reps, and descriptions
- **Workout Progress Tracking** - mark sessions as Complete or Skip
- **Calendar View** to track workout history and upcoming sessions

### 🥗 Smart Nutrition Planning
- **Personalized Meal Plans** based on dietary preferences (Balanced, Vegan, High-Protein)
- **Macro Tracking** - Protein, Carbs, Fats with progress indicators
- **Food Database** with detailed nutritional information
- **Water Intake Tracking** with customizable glass sizes
- **Meal Logging** - log Breakfast, Lunch, Snacks, and Dinner

### 📅 Unified Calendar
- **28-Day Program Calendar** showing daily workout status
- **Color-coded Status Indicators**:
  - 🟢 COMPLETED - Day finished
  - 🟡 PENDING - Current day ready
  - ⚪ SKIPPED - Day skipped
  - 🔵 REST - Rest day
  - ⚫ NOT_STARTED - Future day
- **Day Detail View** with workout and nutrition tabs

### 📊 Progress Tracking
- **Weight Trends Chart** to monitor changes
- **Calories Burned Bar Chart** for activity visualization
- **Streak Tracking** - days of consecutive activity
- **Achievement System** - badges for milestones (streaks, workouts, nutrition)
- **Compliance Rate** - percentage of completed workouts

### 🔔 Intelligent Reminders
- **Workout Reminders** - daily schedule-based notifications
- **Meal Reminders** - consistent meal logging
- **Water Reminders** - hourly hydration prompts
- **Notification Settings** - customizable intervals and times

### ⚙️ Customizable Settings
- **Dark Mode / Light Mode / Auto** theme support
- **Notification Preferences** for workout, meal, and water reminders
- **Water Goal & Glass Size** customization
- **Calorie Display Mode** - show remaining or net calories
- **Account Management** - change password, update email, logout, delete account

---

## 🏗️ Project Architecture

```
app/
├── src/main/java/com/example/flexifitapp/
│   ├── auth/                 # Authentication flow (Login, Register, Verification)
│   ├── dashboard/            # Main dashboard with stats and quick actions
│   ├── workout/              # Workout screens (Today, History, Detail)
│   ├── nutri/                # Nutrition screens (Daily Plan, Food Details)
│   ├── calendar/             # Unified calendar with day selection
│   ├── progress/             # Progress tracking with charts
│   ├── notification/         # Notification system and settings
│   ├── profile/              # User profile and achievements
│   ├── settings/             # App settings (theme, notifications, account)
│   ├── onboarding/           # User onboarding flow
│   ├── data/                 # Repository pattern with Retrofit interfaces
│   ├── utils/                # Helpers, extensions, constants
│   └── model/                # Data classes and DTOs
├── src/main/res/
│   ├── layout/               # All activity/fragment layouts
│   ├── drawable/             # Icons, shapes, backgrounds
│   ├── values/               # Colors, strings, themes
│   └── menu/                 # Navigation drawer menus
└── src/main/assets/          # Static assets
```

---

## 🎨 Screens

| Screen | Description |
|--------|-------------|
| **Login / Register** | Authentication with email/password or Google |
| **Onboarding** | User profile setup, fitness goals, dietary preferences |
| **Dashboard** | Welcome message, daily stats, quick actions |
| **Workout Plan** | Day's workout with warmups and exercises |
| **Workout Detail** | Exercise-specific details with images and video tutorials |
| **Nutrition Plan** | Daily meals with macros and water tracking |
| **Food Detail** | Nutritional info with portion control |
| **Calendar** | 28-day visual progress tracker |
| **Day View** | Detailed day info with workout/nutrition tabs |
| **Progress Tracker** | Charts and statistics |
| **Notifications** | History of alerts and reminders |
| **Settings** | App customization and account management |
| **Profile** | User info, achievements, and stats |

---

## 🔗 Related Projects

- **[FlexiFit API](https://github.com/CjConvento/FlexiFit.Api)** - Backend REST API (C#) - *Required for full app functionality*
- **[FlexiFit Admin Panel](https://github.com/CjConvento/FlexiFitAdminPanel)** - Admin dashboard

---

## 🚀 Developer Setup

### Prerequisites

- **JDK 17 or higher**
- **Android Studio** (latest version recommended) or **VS Code** with extensions
- **Android SDK** (API Level 34+)
- **Android 13+ Device** or emulator for testing
- **Git** for version control
- **[FlexiFit API](https://github.com/CjConvento/FlexiFit.Api)** running locally or deployed

### Installation Guide

#### 1. Clone the Repository

```bash
git clone https://github.com/CjConvento/FlexiFitApp
cd FlexiFitApp
```

#### 2. Open the Project

**Option A - Android Studio:**
- Open Android Studio → **Open** → Select the project folder.

**Option B - VS Code:**
- Install extensions: **Android** (Google), **Kotlin Language**, **Android XML**
- File → Open Folder → Select the project folder.

#### 3. Configure API Connection

Update the base URL in `ApiConfig.kt`:

```kotlin
object ApiConfig {
    // For emulator with local API
    const val BASE_URL = "http://10.0.2.2:5160/"
    
    // For physical device with local API
    // const val BASE_URL = "http://192.168.x.x:5160/"
    
    // For production API
    // const val BASE_URL = "https://your-api-domain.com/"
}
```

#### 4. Configure Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (or use existing)
3. Add Android app with your package name: `com.example.flexifitapp`
4. Download `google-services.json`
5. Place it in `app/` folder

#### 5. Build the Project

**Using Android Studio:**
- Build → Make Project

**Using VS Code/Command Line:**
```bash
./gradlew clean build
```

#### 6. Generate APK

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing)
./gradlew assembleRelease
```

#### 7. Install on Device

```bash
./gradlew installDebug
```

Or manually copy the APK:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📦 Dependencies

```gradle
dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    
    // Retrofit & Network
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    
    // Google Sign-In
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    
    // WorkManager (Background tasks)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // Glide (Image loading)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
}
```

---

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

### Lint Checks
```bash
./gradlew lint
```

---

## 📸 Screenshots

_Add screenshots of your app here._

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Cj Convento**
- GitHub: [@CjConvento](https://github.com/CjConvento)
- Email: conventocj110@gmail.com

---

## 🙏 Acknowledgments

- [Material Design 3](https://m3.material.io/) for design guidelines
- [OpenAI](https://openai.com/) for AI-assisted development support
- [JetBrains](https://www.jetbrains.com/) for Kotlin and Android Studio
- All open-source libraries used in this project

---

## ⚡ Quick Commands

| Command | Purpose |
|---------|---------|
| `./gradlew build` | Build the project |
| `./gradlew clean build` | Clean and rebuild |
| `./gradlew assembleDebug` | Generate debug APK |
| `./gradlew assembleRelease` | Generate release APK |
| `./gradlew installDebug` | Install on connected device |
| `./gradlew test` | Run unit tests |
| `./gradlew lint` | Run lint checks |

---

*Built with ❤️ for fitness enthusiasts everywhere.*
