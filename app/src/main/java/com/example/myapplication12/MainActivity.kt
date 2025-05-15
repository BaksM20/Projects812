package com.example.myapplication12

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication12.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import android.content.Intent

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var navController: NavController
    private val credentialsFile = "user_credentials.txt"
    private var isLoggedIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views
        emailEditText = findViewById(R.id.editTextTextEmailAddress)
        passwordEditText = findViewById(R.id.editTextNumberPassword)
        loginButton = findViewById(R.id.button)
        registerButton = findViewById(R.id.registerButton)

        // Set up navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        // Initially hide the bottom navigation and show login views
        binding.bottomNavigation.visibility = View.GONE
        binding.navHostFragment.visibility = View.GONE

        setupLoginButton()
        setupRegisterButton()
    }

    private fun setupLoginButton() {
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isValidLogin(email, password)) {
                isLoggedIn = true
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                
                // Hide login views
                emailEditText.visibility = View.GONE
                passwordEditText.visibility = View.GONE
                loginButton.visibility = View.GONE
                registerButton.visibility = View.GONE
                findViewById<View>(R.id.textView).visibility = View.GONE
                
                // Show main content
                binding.navHostFragment.visibility = View.VISIBLE
                binding.bottomNavigation.visibility = View.VISIBLE
                
                // Navigate to category setup
                navController.navigate(R.id.category_setup)
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRegisterButton() {
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isValidLogin(email: String, password: String): Boolean {
        val file = File(filesDir, credentialsFile)
        if (!file.exists()) {
            // If file doesn't exist, create it with the first login
            saveCredentials(email, password)
            return true
        }

        // Read existing credentials
        val credentials = file.readLines()
        return credentials.any { line ->
            val (storedEmail, storedPassword) = line.split(":")
            email == storedEmail && password == storedPassword
        }
    }

    private fun saveCredentials(email: String, password: String) {
        try {
            val file = File(filesDir, credentialsFile)
            if (!file.exists()) {
                file.createNewFile()
            }
            
            // Append new credentials
            FileOutputStream(file, true).use { output ->
                val credentials = "$email:$password\n"
                output.write(credentials.toByteArray())
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving credentials: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}