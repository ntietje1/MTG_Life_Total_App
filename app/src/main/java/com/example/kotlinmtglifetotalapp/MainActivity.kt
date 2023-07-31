package com.example.kotlinmtglifetotalapp

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.kotlinmtglifetotalapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_life_counter, R.id.navigation_settings
            )
        )
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        //setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Assuming you have some data you want to pass, let's call it "myData"
        val myData = "Hello, this is my data!"

        // Create a Bundle and put the data into it
        val bundle = Bundle()
        bundle.putString("data_key", myData)

        // Navigate to the destination fragment with the data bundle
        navController.navigate(R.id.navigation_home, bundle)
    }
}