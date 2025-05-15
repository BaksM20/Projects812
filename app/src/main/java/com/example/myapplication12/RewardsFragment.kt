package com.example.myapplication12

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication12.databinding.FragmentRewardsBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class RewardsFragment : Fragment() {
    private var _binding: FragmentRewardsBinding? = null
    private val binding get() = _binding!!
    private val expensesFile = "expenses.json"
    private val incomesFile = "incomes.json"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRewardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPieChart()
        updateAmounts()
    }

    private fun setupPieChart() {
        val expenses = loadExpenses()
        val incomes = loadIncomes()
        
        val totalExpenses = expenses.sumOf { it.amount }
        val totalIncomes = incomes.sumOf { it.amount }

        val entries = listOf(
            PieEntry(totalExpenses.toFloat(), "Expenses"),
            PieEntry(totalIncomes.toFloat(), "Income")
        )

        val dataSet = PieDataSet(entries, "Financial Overview")
        dataSet.colors = listOf(
            Color.rgb(255, 99, 71),  // Tomato red for expenses
            Color.rgb(50, 205, 50)   // Lime green for income
        )
        dataSet.valueTextSize = 16f
        dataSet.valueTextColor = Color.WHITE

        val data = PieData(dataSet)
        binding.financialPieChart.apply {
            this.data = data
            description.isEnabled = false
            legend.isEnabled = true
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(14f)
            animateY(1000)
            invalidate()
        }
    }

    private fun updateAmounts() {
        val expenses = loadExpenses()
        val incomes = loadIncomes()
        
        val totalExpenses = expenses.sumOf { it.amount }
        val totalIncomes = incomes.sumOf { it.amount }
        val netBalance = totalIncomes - totalExpenses

        // Update income amount
        binding.incomeAmount.text = String.format("$%.2f", totalIncomes)

        // Update expense amount
        binding.expenseAmount.text = String.format("$%.2f", totalExpenses)

        // Update balance amount with color
        binding.balanceAmount.apply {
            text = String.format("$%.2f", netBalance)
            setTextColor(if (netBalance >= 0) Color.rgb(50, 205, 50) else Color.rgb(255, 99, 71))
        }
    }

    private fun loadExpenses(): List<Expense> {
        return try {
            val file = File(requireContext().filesDir, expensesFile)
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<Expense>>() {}.type
                Gson().fromJson<List<Expense>>(json, type)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadIncomes(): List<Income> {
        return try {
            val file = File(requireContext().filesDir, incomesFile)
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<Income>>() {}.type
                Gson().fromJson<List<Income>>(json, type)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 