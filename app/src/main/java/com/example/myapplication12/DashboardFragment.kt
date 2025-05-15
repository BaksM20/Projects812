package com.example.myapplication12

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication12.databinding.DialogAddItemBinding
import com.example.myapplication12.databinding.FragmentDashboardBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class ListItem(
    val name: String,
    val quantity: Int,
    val notes: String,
    var completed: Boolean = false
)

data class CategoryList(
    val name: String,
    val items: MutableList<ListItem> = mutableListOf()
)

data class Category(
    val name: String,
    val lists: MutableList<CategoryList> = mutableListOf()
)

data class Expense(
    val category: String,
    val list: String?,
    val description: String,
    val amount: Double,
    val date: String,
    val time: String,
    val receiptPath: String? = null
)

data class Income(
    val category: String,
    val description: String,
    val amount: Double,
    val date: String,
    val time: String
)

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val expenses = mutableListOf<Expense>()
    private val incomes = mutableListOf<Income>()
    private val categories = mutableListOf<Category>()
    private lateinit var adapter: ExpenseAdapter
    private val expensesFile = "expenses.json"
    private val incomesFile = "incomes.json"
    private val categoriesFile = "categories.json"
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private lateinit var sharedPreferences: SharedPreferences
    private var currentReceiptPath: String? = null
    private var currentCategory: String? = null
    private var currentList: String? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let { saveReceiptImage(it) }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                saveReceiptFromUri(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSharedPreferences()
        setupThemeSwitch()
        loadExpenses()
        loadIncomes()
        setupRecyclerView()
        setupCategoryDropdowns()
        setupDateAndTimePickers()
        setupAddExpenseButton()
        setupAddIncomeButton()
        setupReceiptButtons()
        setupListManagement()
        updateBalance()
    }

    private fun setupSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences("ThemePrefs", 0)
    }

    private fun setupThemeSwitch() {
        binding.themeSwitch.isChecked = sharedPreferences.getBoolean("dark_mode", false)
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun setupCategoryDropdowns() {
        loadCategories()
        val categoryNames = categories.map { it.name }
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames)
        binding.categoryAutocomplete.setAdapter(categoryAdapter)
        binding.incomeCategoryAutocomplete.setAdapter(categoryAdapter)

        binding.categoryAutocomplete.setOnItemClickListener { _, _, position, _ ->
            currentCategory = categoryNames[position]
            updateListDropdown()
        }
    }

    private fun updateListDropdown() {
        val category = categories.find { it.name == currentCategory }
        val listNames = category?.lists?.map { it.name } ?: emptyList()
        val listAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listNames)
        binding.listAutocomplete.setAdapter(listAdapter)
        binding.listAutocomplete.setOnItemClickListener { _, _, position, _ ->
            currentList = listNames[position]
        }
    }

    private fun loadCategories() {
        try {
            val file = File(requireContext().filesDir, categoriesFile)
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<Category>>() {}.type
                val loadedCategories = Gson().fromJson<List<Category>>(json, type)
                categories.clear()
                categories.addAll(loadedCategories)
            } else {
                // Add some default categories if none exist
                categories.add(Category("Groceries", mutableListOf(CategoryList("Weekly Shopping"), CategoryList("Monthly Shopping"))))
                categories.add(Category("Bills", mutableListOf(CategoryList("Utilities"), CategoryList("Rent"))))
                categories.add(Category("Entertainment", mutableListOf(CategoryList("Movies"), CategoryList("Dining Out"))))
                saveCategories()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error loading categories: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCategories() {
        try {
            val file = File(requireContext().filesDir, categoriesFile)
            val json = Gson().toJson(categories)
            file.writeText(json)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error saving categories: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDateAndTimePickers() {
        binding.expenseDate.setOnClickListener {
            showDatePicker(binding.expenseDate)
        }

        binding.expenseTime.setOnClickListener {
            showTimePicker(binding.expenseTime)
        }

        binding.incomeDate.setOnClickListener {
            showDatePicker(binding.incomeDate)
        }

        binding.incomeTime.setOnClickListener {
            showTimePicker(binding.incomeTime)
        }
    }

    private fun showDatePicker(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                editText.setText(dateFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                editText.setText(timeFormatter.format(calendar.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(expenses)
        binding.expensesList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@DashboardFragment.adapter
        }
    }

    private fun setupAddExpenseButton() {
        binding.addExpenseButton.setOnClickListener {
            val category = binding.categoryAutocomplete.text.toString()
            val list = binding.listAutocomplete.text.toString()
            val description = binding.expenseDescription.text.toString()
            val amountStr = binding.expenseAmount.text.toString()
            val date = binding.expenseDate.text.toString()
            val time = binding.expenseTime.text.toString()

            if (category.isEmpty() || description.isEmpty() || amountStr.isEmpty() || date.isEmpty() || time.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val amount = amountStr.toDouble()
                if (amount <= 0) {
                    Toast.makeText(requireContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val expense = Expense(category, list, description, amount, date, time, currentReceiptPath)
                expenses.add(expense)
                adapter.notifyItemInserted(expenses.size - 1)
                saveExpenses()
                updateBalance()
                clearInputs()
            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAddIncomeButton() {
        binding.addIncomeButton.setOnClickListener {
            val category = binding.incomeCategoryAutocomplete.text.toString()
            val description = binding.incomeDescription.text.toString()
            val amountStr = binding.incomeAmount.text.toString()
            val date = binding.incomeDate.text.toString()
            val time = binding.incomeTime.text.toString()

            if (category.isEmpty() || description.isEmpty() || amountStr.isEmpty() || date.isEmpty() || time.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val amount = amountStr.toDouble()
                if (amount <= 0) {
                    Toast.makeText(requireContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val income = Income(category, description, amount, date, time)
                incomes.add(income)
                saveIncomes()
                updateBalance()
                clearIncomeInputs()
            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupReceiptButtons() {
        binding.takePictureButton.setOnClickListener {
            takePicture()
        }

        binding.pickImageButton.setOnClickListener {
            pickImage()
        }
    }

    private fun setupListManagement() {
        binding.manageListButton.setOnClickListener {
            showListManagementDialog()
        }
    }

    private fun showListManagementDialog() {
        val category = currentCategory
        if (category == null) {
            Toast.makeText(requireContext(), "Please select a category first", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryObj = categories.find { it.name == category }
        if (categoryObj == null) {
            Toast.makeText(requireContext(), "Category not found", Toast.LENGTH_SHORT).show()
            return
        }

        val listNames = categoryObj.lists.map { it.name }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Select List")
            .setItems(listNames) { _, which ->
                val selectedList = categoryObj.lists[which]
                showListItemsDialog(selectedList)
            }
            .setPositiveButton("Add New List") { _, _ ->
                showAddListDialog(categoryObj)
            }
            .show()
    }

    private fun showListItemsDialog(list: CategoryList) {
        val dialogBinding = DialogAddItemBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("${list.name} Items")
            .setView(dialogBinding.root)
            .setPositiveButton("Add Item") { _, _ ->
                val name = dialogBinding.itemNameInput.text.toString()
                val quantityStr = dialogBinding.itemQuantityInput.text.toString()
                val notes = dialogBinding.itemNotesInput.text.toString()

                if (name.isNotEmpty() && quantityStr.isNotEmpty()) {
                    try {
                        val quantity = quantityStr.toInt()
                        if (quantity <= 0) {
                            Toast.makeText(requireContext(), "Quantity must be greater than 0", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        val item = ListItem(name, quantity, notes)
                        list.items.add(item)
                        saveCategories()
                        showListItemsDialog(list) // Refresh the dialog
                    } catch (e: NumberFormatException) {
                        Toast.makeText(requireContext(), "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Close", null)
            .setNeutralButton("Share List") { _, _ ->
                shareList(list)
            }
            .create()

        // Show current items with completion status
        val itemsText = list.items.joinToString("\n") { item -> 
            "• ${item.name} (${item.quantity})${if (item.notes.isNotEmpty()) " - ${item.notes}" else ""}${if (item.completed) " ✓" else ""}" 
        }
        dialogBinding.itemNotesInput.setText(itemsText)
        dialogBinding.itemNotesInput.isEnabled = false

        // Add click listener to toggle item completion
        dialogBinding.itemNotesInput.setOnClickListener {
            val items = list.items
            if (items.isNotEmpty()) {
                val itemNames = items.map { it.name }.toTypedArray()
                val checkedItems = BooleanArray(items.size) { index -> items[index].completed }
                
                AlertDialog.Builder(requireContext())
                    .setTitle("Mark Items as Complete")
                    .setMultiChoiceItems(itemNames, checkedItems) { _, _, _ -> }
                    .setPositiveButton("Save") { _, _ ->
                        items.forEachIndexed { index, item ->
                            item.completed = checkedItems[index]
                        }
                        saveCategories()
                        showListItemsDialog(list) // Refresh the dialog
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        dialog.show()
    }

    private fun shareList(list: CategoryList) {
        val itemsText = list.items.joinToString("\n") { item -> 
            "• ${item.name} (${item.quantity})${if (item.notes.isNotEmpty()) " - ${item.notes}" else ""}${if (item.completed) " ✓" else ""}" 
        }
        val shareText = "${list.name}:\n$itemsText"
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, "Share List"))
    }

    private fun showAddListDialog(category: Category) {
        val input = TextInputEditText(requireContext())
        input.hint = "List Name"

        AlertDialog.Builder(requireContext())
            .setTitle("Add New List")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val listName = input.text.toString()
                if (listName.isNotEmpty()) {
                    if (category.lists.any { it.name == listName }) {
                        Toast.makeText(requireContext(), "A list with this name already exists", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    category.lists.add(CategoryList(listName))
                    saveCategories()
                    updateListDropdown()
                } else {
                    Toast.makeText(requireContext(), "Please enter a list name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearInputs() {
        binding.categoryAutocomplete.text.clear()
        binding.listAutocomplete.text.clear()
        binding.expenseDescription.text?.clear()
        binding.expenseAmount.text?.clear()
        binding.expenseDate.text?.clear()
        binding.expenseTime.text?.clear()
        currentReceiptPath = null
        currentCategory = null
        currentList = null
        binding.receiptPreview.visibility = View.GONE
    }

    private fun clearIncomeInputs() {
        binding.incomeCategoryAutocomplete.text.clear()
        binding.incomeDescription.text?.clear()
        binding.incomeAmount.text?.clear()
        binding.incomeDate.text?.clear()
        binding.incomeTime.text?.clear()
    }

    private fun updateBalance() {
        val totalExpenses = expenses.sumOf { it.amount }
        val totalIncomes = incomes.sumOf { it.amount }
        val balance = totalIncomes - totalExpenses
        binding.balanceAmount.text = String.format("$%.2f", balance)
    }

    private fun saveExpenses() {
        try {
            val file = File(requireContext().filesDir, expensesFile)
            val json = Gson().toJson(expenses)
            file.writeText(json)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error saving expenses: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveIncomes() {
        try {
            val file = File(requireContext().filesDir, incomesFile)
            val json = Gson().toJson(incomes)
            file.writeText(json)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error saving incomes: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadExpenses() {
        try {
            val file = File(requireContext().filesDir, expensesFile)
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<Expense>>() {}.type
                val loadedExpenses = Gson().fromJson<List<Expense>>(json, type)
                expenses.clear()
                expenses.addAll(loadedExpenses)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error loading expenses: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadIncomes() {
        try {
            val file = File(requireContext().filesDir, incomesFile)
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<Income>>() {}.type
                val loadedIncomes = Gson().fromJson<List<Income>>(json, type)
                incomes.clear()
                incomes.addAll(loadedIncomes)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error loading incomes: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveReceiptImage(bitmap: Bitmap) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "receipt_$timestamp.jpg"
            val file = File(requireContext().filesDir, filename)
            
            // Resize bitmap if too large
            val maxDimension = 1024
            val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
                Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
            } else {
                bitmap
            }
            
            FileOutputStream(file).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            // Recycle the original bitmap if we created a scaled version
            if (scaledBitmap != bitmap) {
                bitmap.recycle()
            }
            
            currentReceiptPath = file.absolutePath
            binding.receiptPreview.setImageBitmap(scaledBitmap)
            binding.receiptPreview.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Receipt saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to save receipt. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveReceiptFromUri(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "receipt_$timestamp.jpg"
            val file = File(requireContext().filesDir, filename)
            
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            
            currentReceiptPath = file.absolutePath
            binding.receiptPreview.setImageURI(uri)
            binding.receiptPreview.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Receipt saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error saving receipt: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takePictureIntent)
    }

    private fun pickImage() {
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(pickImageIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ExpenseAdapter(private val expenses: List<Expense>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val category: android.widget.TextView = view.findViewById(R.id.expense_category)
        val description: android.widget.TextView = view.findViewById(R.id.expense_description)
        val amount: android.widget.TextView = view.findViewById(R.id.expense_amount)
        val dateTime: android.widget.TextView = view.findViewById(R.id.expense_datetime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.category.text = expense.category
        holder.description.text = expense.description
        holder.amount.text = String.format("$%.2f", expense.amount)
        holder.dateTime.text = "${expense.date} ${expense.time}"
    }

    override fun getItemCount() = expenses.size
} 