package com.example.myapplication12

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication12.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import java.io.File

data class AppSettings(
    var notificationsEnabled: Boolean = true,
    var biometricEnabled: Boolean = false,
    var darkModeEnabled: Boolean = false,
    var defaultCurrency: String = "USD",
    var autoSyncEnabled: Boolean = true,
    var budgetAlertsEnabled: Boolean = true,
    var bankAccountConnected: Boolean = false,
    var userLevel: Int = 1,
    var experiencePoints: Int = 0,
    var userName: String = "Financial Wizard",
    var achievements: List<Achievement> = emptyList()
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val points: Int,
    val unlocked: Boolean = false
)

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val settingsFile = "app_settings.json"
    private var settings = AppSettings()
    private val achievements = listOf(
        Achievement("first_transaction", "First Transaction", "Record your first transaction", 100),
        Achievement("savings_milestone", "Savings Milestone", "Save your first $100", 200),
        Achievement("budget_master", "Budget Master", "Stay within budget for 7 days", 300),
        Achievement("investment_beginner", "Investment Beginner", "Make your first investment", 400),
        Achievement("financial_wizard", "Financial Wizard", "Reach level 5", 500)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSettings()
        setupUserProfile()
        setupBankAccountSync()
        setupGeneralSettings()
        setupFinancialSettings()
        setupDataAndPrivacy()
    }

    private fun setupUserProfile() {
        binding.userName.text = settings.userName
        binding.userLevel.text = "Level ${settings.userLevel} Investor"
        
        // Calculate progress to next level
        val pointsForNextLevel = settings.userLevel * 1000
        val progress = (settings.experiencePoints.toFloat() / pointsForNextLevel) * 100
        binding.levelProgress.progress = progress.toInt()
        binding.levelStatus.text = "${progress.toInt()}% to Level ${settings.userLevel + 1}"

        binding.editProfileButton.setOnClickListener {
            showEditProfileDialog()
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val nameInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.name_input)
        nameInput.setText(settings.userName)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameInput.text.toString()
                if (newName.isNotEmpty()) {
                    settings.userName = newName
                    saveSettings()
                    binding.userName.text = newName
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addExperiencePoints(points: Int) {
        settings.experiencePoints += points
        val pointsForNextLevel = settings.userLevel * 1000
        
        if (settings.experiencePoints >= pointsForNextLevel) {
            settings.userLevel++
            showLevelUpDialog()
        }
        
        saveSettings()
        setupUserProfile()
    }

    private fun showLevelUpDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Level Up! 🎉")
            .setMessage("Congratulations! You've reached level ${settings.userLevel}!")
            .setPositiveButton("Awesome!", null)
            .show()
    }

    private fun checkAchievements() {
        achievements.forEach { achievement ->
            if (!achievement.unlocked) {
                when (achievement.id) {
                    "first_transaction" -> {
                        // Check if user has any transactions
                        val hasTransactions = File(requireContext().filesDir, "expenses.json").exists() ||
                                           File(requireContext().filesDir, "incomes.json").exists()
                        if (hasTransactions) {
                            unlockAchievement(achievement)
                        }
                    }
                    "savings_milestone" -> {
                        // Check if user has saved $100
                        val savingsFile = File(requireContext().filesDir, "savings_account.json")
                        if (savingsFile.exists()) {
                            val savings = Gson().fromJson(savingsFile.readText(), SavingsAccount::class.java)
                            if (savings.balance >= 100) {
                                unlockAchievement(achievement)
                            }
                        }
                    }
                    // Add more achievement checks here
                }
            }
        }
    }

    private fun unlockAchievement(achievement: Achievement) {
        val updatedAchievements = settings.achievements.toMutableList()
        updatedAchievements.add(achievement.copy(unlocked = true))
        settings.achievements = updatedAchievements
        addExperiencePoints(achievement.points)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Achievement Unlocked! 🏆")
            .setMessage("${achievement.title}\n${achievement.description}")
            .setPositiveButton("Awesome!", null)
            .show()
    }

    private fun setupBankAccountSync() {
        binding.connectBankButton.setOnClickListener {
            if (!settings.bankAccountConnected) {
                showBankConnectionDialog()
            } else {
                showDisconnectBankDialog()
            }
        }
        updateBankSyncStatus()
    }

    private fun showBankConnectionDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Connect Bank Account")
            .setMessage("Would you like to connect your bank account? This will allow automatic transaction syncing.")
            .setPositiveButton("Connect") { _, _ ->
                // In a real app, this would launch bank OAuth flow
                settings.bankAccountConnected = true
                saveSettings()
                updateBankSyncStatus()
                Toast.makeText(context, "Bank account connected successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDisconnectBankDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Disconnect Bank Account")
            .setMessage("Are you sure you want to disconnect your bank account? This will stop automatic transaction syncing.")
            .setPositiveButton("Disconnect") { _, _ ->
                settings.bankAccountConnected = false
                saveSettings()
                updateBankSyncStatus()
                Toast.makeText(context, "Bank account disconnected", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateBankSyncStatus() {
        binding.syncStatus.text = if (settings.bankAccountConnected) "Connected" else "Not Connected"
        binding.connectBankButton.text = if (settings.bankAccountConnected) "Disconnect Bank Account" else "Connect Bank Account"
    }

    private fun setupGeneralSettings() {
        binding.notificationsSwitch.isChecked = settings.notificationsEnabled
        binding.biometricSwitch.isChecked = settings.biometricEnabled
        binding.darkModeSwitch.isChecked = settings.darkModeEnabled

        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            settings.notificationsEnabled = isChecked
            saveSettings()
        }

        binding.biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            settings.biometricEnabled = isChecked
            saveSettings()
        }

        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            settings.darkModeEnabled = isChecked
            saveSettings()
            // In a real app, this would trigger theme change
        }
    }

    private fun setupFinancialSettings() {
        binding.currencyInput.setText(settings.defaultCurrency)
        binding.autoSyncSwitch.isChecked = settings.autoSyncEnabled
        binding.budgetAlertsSwitch.isChecked = settings.budgetAlertsEnabled

        binding.currencyInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                settings.defaultCurrency = binding.currencyInput.text.toString()
                saveSettings()
            }
        }

        binding.autoSyncSwitch.setOnCheckedChangeListener { _, isChecked ->
            settings.autoSyncEnabled = isChecked
            saveSettings()
        }

        binding.budgetAlertsSwitch.setOnCheckedChangeListener { _, isChecked ->
            settings.budgetAlertsEnabled = isChecked
            saveSettings()
        }
    }

    private fun setupDataAndPrivacy() {
        binding.exportDataButton.setOnClickListener {
            showExportDataDialog()
        }

        binding.clearDataButton.setOnClickListener {
            showClearDataDialog()
        }

        binding.privacyPolicyButton.setOnClickListener {
            // In a real app, this would open privacy policy
            Toast.makeText(context, "Privacy Policy", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showExportDataDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Export Data")
            .setMessage("Would you like to export your financial data? This will create a CSV file with all your transactions.")
            .setPositiveButton("Export") { _, _ ->
                // In a real app, this would export data
                Toast.makeText(context, "Data exported successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearDataDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear All Data")
            .setMessage("Are you sure you want to clear all your data? This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAllData() {
        try {
            // Clear all data files
            val files = listOf(
                "expenses.json",
                "incomes.json",
                "savings_goals.json",
                "savings_account.json",
                "app_settings.json"
            )
            
            files.forEach { filename ->
                File(requireContext().filesDir, filename).delete()
            }

            // Reset settings
            settings = AppSettings()
            saveSettings()
            
            // Update UI
            setupGeneralSettings()
            setupFinancialSettings()
            updateBankSyncStatus()

            Toast.makeText(context, "All data cleared successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error clearing data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSettings() {
        try {
            val file = File(requireContext().filesDir, settingsFile)
            val json = Gson().toJson(settings)
            file.writeText(json)
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSettings() {
        try {
            val file = File(requireContext().filesDir, settingsFile)
            if (file.exists()) {
                val json = file.readText()
                settings = Gson().fromJson(json, AppSettings::class.java)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 