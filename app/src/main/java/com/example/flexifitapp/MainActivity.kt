package com.example.flexifitapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth



class MainActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "flexifit_settings"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {

        // Apply saved night mode
        val nightMode = AppPrefs.getNightMode(this)
        AppCompatDelegate.setDefaultNightMode(nightMode)

        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))
        setContentView(R.layout.activity_main)

        applyReadMode()

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        // NavHost
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as? NavHostFragment
                ?: throw IllegalStateException(
                    "NavHostFragment not found. Check activity_main.xml uses NavHostFragment for fragment_container."
                )

        navController = navHostFragment.navController

        // ✅ UPDATE: Include all destinations including notification
        appBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_home,
            R.id.WorkoutTabRootFragment,
            R.id.nutritionTabRootFragment,
            R.id.nav_progresstracker,           // Progress Tracker
            R.id.notificationFragment,  // ✅ ADD NOTIFICATIONS
            R.id.nav_profile,
            R.id.nav_settings,
        )
            .setOpenableLayout(drawerLayout)
            .build()

        // Connect toolbar + drawer + nav controller (this handles the hamburger icon)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        // =========================================================
        // MANUAL DRAWER NAVIGATION (instead of automatic setup)
        // =========================================================
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Always pop back to Home and clear the back stack
                    navController.popBackStack(R.id.nav_home, inclusive = false)
                }
                // IMPORTANT: Use the correct fragment ID (uppercase W) even though the menu item ID may be lowercase
                R.id.WorkoutTabRootFragment -> navController.navigate(R.id.WorkoutTabRootFragment)
                R.id.nutritionTabRootFragment -> navController.navigate(R.id.nutritionTabRootFragment)
                R.id.nav_progresstracker -> navController.navigate(R.id.nav_progresstracker)
                R.id.nav_profile -> navController.navigate(R.id.nav_profile)
                R.id.nav_settings -> navController.navigate(R.id.nav_settings)
                R.id.nav_logout -> {
                    // Handle logout
                    FirebaseAuth.getInstance().signOut()
                    TokenStore.clear(this)
                    UserPrefs.clearAuth(this)

                    getSharedPreferences("flexifit_prefs", MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
            // Close the drawer after navigation
            drawerLayout.closeDrawers()
            true
        }

        // Update the checked item in the drawer when the destination changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_home -> navView.setCheckedItem(R.id.nav_home)
                R.id.WorkoutTabRootFragment -> navView.setCheckedItem(R.id.WorkoutTabRootFragment)
                R.id.nutritionTabRootFragment -> navView.setCheckedItem(R.id.nutritionTabRootFragment)
                R.id.nav_progresstracker -> navView.setCheckedItem(R.id.nav_progresstracker)
                R.id.nav_profile -> navView.setCheckedItem(R.id.nav_profile)
                R.id.nav_settings -> navView.setCheckedItem(R.id.nav_settings)
                // For other destinations (like notificationFragment), we leave the previous selection
            }
        }

        // Drawer Header Setup – buttons only, data loaded via updateDrawerHeader()
        if (navView.headerCount > 0) {
            val headerView: View = navView.getHeaderView(0)

            val btnHeaderSettings = headerView.findViewById<ImageButton>(R.id.btnHeaderSettings)
            btnHeaderSettings?.setOnClickListener {
                drawerLayout.closeDrawers()
                navController.navigate(R.id.nav_settings)
            }

            val btnNotifications = headerView.findViewById<ImageButton>(R.id.btnHeaderNotifications)
            btnNotifications?.setOnClickListener {
                drawerLayout.closeDrawers()
                navController.navigate(R.id.notificationFragment)
            }
        }

        // Load initial header data
        updateDrawerHeader()

        // Refresh header when drawer opens
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {
                updateDrawerHeader()
            }
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })

        // (The logout item is already handled in the navigation listener, so we can remove the separate listener)
        // Remove the previous logout listener to avoid duplication.
        // We'll comment it out instead of deleting.
        /*
        navView.menu.findItem(R.id.nav_logout).setOnMenuItemClickListener {
            // ... handled above
        }
        */

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh header when returning to activity (e.g., after profile update)
        updateDrawerHeader()
    }

    private fun updateDrawerHeader() {
        if (navView.headerCount == 0) return
        val headerView = navView.getHeaderView(0)

        val txtName = headerView.findViewById<TextView>(R.id.txtHeaderName)
        val txtEmail = headerView.findViewById<TextView>(R.id.txtHeaderEmail)
        val imgAvatar = headerView.findViewById<ShapeableImageView>(R.id.imgAvatar)

        // Get data from UserPrefs
        val name = UserPrefs.getString(this, UserPrefs.KEY_NAME, "FlexiFit User")
        val username = UserPrefs.getString(this, UserPrefs.KEY_USERNAME, "")
        val email = UserPrefs.getString(this, UserPrefs.KEY_USER_EMAIL, "")
        val avatarUrl = UserPrefs.getString(this, UserPrefs.KEY_AVATAR_URL, "")

        // Set text
        txtName?.text = name
        txtEmail?.text = if (email.isNotBlank()) email else username.ifBlank { "FlexiFit User" }

        // Load avatar
        if (avatarUrl.isNotBlank()) {
            Glide.with(this)
                .load(if (avatarUrl.startsWith("http")) avatarUrl else ApiConfig.BASE_URL + avatarUrl)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .circleCrop()
                .into(imgAvatar)
        } else {
            // Default avatar
            Glide.with(this)
                .load(R.drawable.profile)
                .circleCrop()
                .into(imgAvatar)
        }
    }

    // Add this method somewhere after onCreate
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Notification permission granted")
            } else {
                Log.d("MainActivity", "Notification permission denied")
            }
        }
    }

    private fun applyReadMode() {
        val isReadMode = AppPrefs.isReadModeEnabled(this)
        val root = findViewById<ViewGroup>(android.R.id.content)
        if (isReadMode) {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            val filter = ColorMatrixColorFilter(matrix)
            val paint = Paint().apply { colorFilter = filter }
            root.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
        } else {
            root.setLayerType(View.LAYER_TYPE_NONE, null)
        }
    }

    fun navigateToNutritionTab() {
        navController.navigate(R.id.nutritionTabRootFragment)
        drawerLayout.closeDrawers()
    }

    fun navigateToNotifications() {
        navController.navigate(R.id.notificationFragment)
        drawerLayout.closeDrawers()
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}