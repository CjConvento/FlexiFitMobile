package com.example.flexifitapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.flexifitapp.TokenStore
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

        // Theme
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))
        setContentView(R.layout.activity_main)

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

        appBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_home,
            R.id.workoutTabRootFragment,
            R.id.nutritionTabRootFragment,
            R.id.nav_profile,
            R.id.nav_about
        )
            .setOpenableLayout(drawerLayout)
            .build()

        // Connect toolbar + drawer + nav controller
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        // Connect drawer menu clicks
        NavigationUI.setupWithNavController(navView, navController)

        // Header settings button
        val headerView: View = navView.getHeaderView(0)
        val btnHeaderSettings = headerView.findViewById<ImageButton>(R.id.btnHeaderSettings)
        btnHeaderSettings.setOnClickListener {
            drawerLayout.close()
        }

        // Logout
        navView.menu.findItem(R.id.nav_logout).setOnMenuItemClickListener {

            FirebaseAuth.getInstance().signOut()
            TokenStore.clear(this)

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

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}