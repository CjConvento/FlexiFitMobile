package com.example.flexifitapp

import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
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

        // Connect toolbar + drawer + nav controller
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        // Connect drawer menu clicks
        NavigationUI.setupWithNavController(navView, navController)

        // Drawer Header Setup
        if (navView.headerCount > 0) {
            val headerView: View = navView.getHeaderView(0)

            // Settings Button
            val btnHeaderSettings = headerView.findViewById<ImageButton>(R.id.btnHeaderSettings)
            btnHeaderSettings?.setOnClickListener {
                drawerLayout.closeDrawers()
                navController.navigate(R.id.nav_settings)
            }

            // 🔔 Notification Button – ADD THIS
            val btnNotifications = headerView.findViewById<ImageButton>(R.id.btnHeaderNotifications)
            btnNotifications?.setOnClickListener {
                drawerLayout.closeDrawers()
                navController.navigate(R.id.notificationFragment)
            }

            // User Info
            val txtName = headerView.findViewById<TextView>(R.id.txtHeaderName)
            val txtEmail = headerView.findViewById<TextView>(R.id.txtHeaderEmail)

            txtName?.text = getSharedPreferences("flexifit_prefs", MODE_PRIVATE)
                .getString("user_name", "FlexiFit User")
            txtEmail?.text = getSharedPreferences("flexifit_prefs", MODE_PRIVATE)
                .getString("user_handle", "@user")
        }

        // Logout
        navView.menu.findItem(R.id.nav_logout).setOnMenuItemClickListener {
            FirebaseAuth.getInstance().signOut()
            TokenStore.clear(this)
            UserPrefs.clearAuth(this)   // ← ADD THIS LINE

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