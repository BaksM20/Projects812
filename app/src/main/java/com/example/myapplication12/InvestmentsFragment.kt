package com.example.myapplication12

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication12.databinding.FragmentInvestmentsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class SavingsGoal(
    val name: String,
    val targetAmount: Double,
    var currentAmount: Double = 0.0,
    val createdAt: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
)

data class SavingsAccount(
    var balance: Double = 0.0,
    var currentDay: Int = 0,
    var lastUpdated: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
)

class InvestmentsFragment : Fragment() {
    private var _binding: FragmentInvestmentsBinding? = null
    private val binding get() = _binding!!
    private val savingsGoals = mutableListOf<SavingsGoal>()
    private lateinit var adapter: SavingsGoalAdapter
    private val savingsGoalsFile = "savings_goals.json"
    private val savingsAccountFile = "savings_account.json"
    private var savingsAccount = SavingsAccount()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvestmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSavingsGoals()
        loadSavingsAccount()
        setupRecyclerView()
        setupAddGoalButton()
        setupAddSavingsButton()
        updateSavingsAccountUI()
    }

    private fun setupRecyclerView() {
        adapter = SavingsGoalAdapter(
            savingsGoals,
            onAddClick = { position -> showAddToGoalDialog(position) },
            onDeleteClick = { position -> deleteGoal(position) }
        )
        binding.goalsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@InvestmentsFragment.adapter
        }
    }

    private fun setupAddGoalButton() {
        binding.addGoalButton.setOnClickListener {
            val name = binding.goalName.text.toString()
            val amountStr = binding.goalAmount.text.toString()

            if (name.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val amount = amountStr.toDouble()
                val goal = SavingsGoal(name, amount)
                savingsGoals.add(goal)
                adapter.notifyItemInserted(savingsGoals.size - 1)
                saveSavingsGoals()
                clearGoalInputs()
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAddSavingsButton() {
        binding.addSavingsButton.setOnClickListener {
            showAddToSavingsDialog()
        }
    }

    private fun showAddToSavingsDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_savings, null)
        val amountInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.savings_amount)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add to Savings")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val amountStr = amountInput.text.toString()
                if (amountStr.isNotEmpty()) {
                    try {
                        val amount = amountStr.toDouble()
                        addToSavings(amount)
                    } catch (e: NumberFormatException) {
                        Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddToGoalDialog(position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_savings, null)
        val amountInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.savings_amount)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add to Goal")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val amountStr = amountInput.text.toString()
                if (amountStr.isNotEmpty()) {
                    try {
                        val amount = amountStr.toDouble()
                        addToGoal(position, amount)
                    } catch (e: NumberFormatException) {
                        Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addToSavings(amount: Double) {
        savingsAccount.balance += amount
        savingsAccount.currentDay = minOf(savingsAccount.currentDay + 1, 32)
        savingsAccount.lastUpdated = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        saveSavingsAccount()
        updateSavingsAccountUI()
    }

    private fun addToGoal(position: Int, amount: Double) {
        val goal = savingsGoals[position]
        goal.currentAmount += amount
        adapter.notifyItemChanged(position)
        saveSavingsGoals()
    }

    private fun deleteGoal(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete this goal?")
            .setPositiveButton("Delete") { _, _ ->
                savingsGoals.removeAt(position)
                adapter.notifyItemRemoved(position)
                saveSavingsGoals()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateSavingsAccountUI() {
        binding.savingsBalance.text = String.format("$%.2f", savingsAccount.balance)
        binding.savingsProgress.text = "Day ${savingsAccount.currentDay} of 32"
        binding.savingsProgressBar.progress = (savingsAccount.currentDay * 100) / 32
    }

    private fun clearGoalInputs() {
        binding.goalName.text?.clear()
        binding.goalAmount.text?.clear()
    }

    private fun saveSavingsGoals() {
        try {
            val file = File(requireContext().filesDir, savingsGoalsFile)
            val json = Gson().toJson(savingsGoals)
            file.writeText(json)
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving goals: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSavingsAccount() {
        try {
            val file = File(requireContext().filesDir, savingsAccountFile)
            val json = Gson().toJson(savingsAccount)
            file.writeText(json)
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving account: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSavingsGoals() {
        try {
            val file = File(requireContext().filesDir, savingsGoalsFile)
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<SavingsGoal>>() {}.type
                val loadedGoals = Gson().fromJson<List<SavingsGoal>>(json, type)
                savingsGoals.clear()
                savingsGoals.addAll(loadedGoals)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading goals: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSavingsAccount() {
        try {
            val file = File(requireContext().filesDir, savingsAccountFile)
            if (file.exists()) {
                val json = file.readText()
                savingsAccount = Gson().fromJson(json, SavingsAccount::class.java)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading account: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class SavingsGoalAdapter(
    private val goals: List<SavingsGoal>,
    private val onAddClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<SavingsGoalAdapter.GoalViewHolder>() {

    class GoalViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val name: android.widget.TextView = view.findViewById(R.id.goal_name)
        val amount: android.widget.TextView = view.findViewById(R.id.goal_amount)
        val progress: android.widget.TextView = view.findViewById(R.id.goal_progress)
        val progressBar: com.google.android.material.progressindicator.LinearProgressIndicator = view.findViewById(R.id.goal_progress_bar)
        val addButton: com.google.android.material.button.MaterialButton = view.findViewById(R.id.add_to_goal_button)
        val deleteButton: com.google.android.material.button.MaterialButton = view.findViewById(R.id.delete_goal_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_savings_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        holder.name.text = goal.name
        holder.amount.text = String.format("$%.2f / $%.2f", goal.currentAmount, goal.targetAmount)
        holder.progress.text = String.format("%.1f%%", (goal.currentAmount / goal.targetAmount) * 100)
        holder.progressBar.progress = ((goal.currentAmount / goal.targetAmount) * 100).toInt()
        holder.addButton.setOnClickListener { onAddClick(position) }
        holder.deleteButton.setOnClickListener { onDeleteClick(position) }
    }

    override fun getItemCount() = goals.size
} 