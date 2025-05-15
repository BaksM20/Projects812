package com.example.myapplication12

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication12.databinding.ActivityRegisterBinding
import java.io.File
import java.io.FileOutputStream

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val credentialsFile = "user_credentials.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRegisterButton()
        setupLoginLink()
    }

    private fun setupRegisterButton() {
        binding.registerButton.setOnClickListener {
            val email = binding.registerEmail.text.toString()
            val password = binding.registerPassword.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEmailAlreadyRegistered(email)) {
                Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save credentials
            saveCredentials(email, password)
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

            // Return to login screen
            finish()
        }
    }

    private fun setupLoginLink() {
        binding.loginLink.setOnClickListener {
            finish()
        }
    }

    private fun isEmailAlreadyRegistered(email: String): Boolean {
        val file = File(filesDir, credentialsFile)
        if (!file.exists()) {
            return false
        }

        val credentials = file.readLines()
        return credentials.any { line ->
            val (storedEmail, _) = line.split(":")
            email == storedEmail
        }
    }

    private fun saveCredentials(email: String, password: String) {
        try {
            val file = File(filesDir, credentialsFile)
            if (!file.exists()) {
                file.createNewFile()
            }
            
            FileOutputStream(file, true).use { output ->
                val credentials = "$email:$password\n"
                output.write(credentials.toByteArray())
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving credentials: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
} 