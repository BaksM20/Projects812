package com.example.myapplication12

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication12.databinding.FragmentCategorySetupBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class ExpenseCategory(
    val name: String
)

class CategorySetupFragment : Fragment() {
    private var _binding: FragmentCategorySetupBinding? = null
    private val binding get() = _binding!!
    private val categories = mutableListOf<ExpenseCategory>()
    private lateinit var adapter: CategoryAdapter
    private val categoriesFile = "expense_categories.json"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategorySetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCategories()
        setupRecyclerView()
        setupAddCategoryButton()
        setupContinueButton()
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(categories) { position ->
            categories.removeAt(position)
            adapter.notifyItemRemoved(position)
            saveCategories()
        }
        binding.categoriesList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CategorySetupFragment.adapter
        }
    }

    private fun setupAddCategoryButton() {
        binding.addCategoryButton.setOnClickListener {
            val categoryName = binding.categoryName.text.toString()
            if (categoryName.isEmpty()) {
                Toast.makeText(context, "Please enter a category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = ExpenseCategory(categoryName)
            categories.add(category)
            adapter.notifyItemInserted(categories.size - 1)
            binding.categoryName.text.clear()
            saveCategories()
        }
    }

    private fun setupContinueButton() {
        binding.continueButton.setOnClickListener {
            if (categories.isEmpty()) {
                Toast.makeText(context, "Please add at least one category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.action_category_setup_to_dashboard)
        }
    }

    private fun saveCategories() {
        try {
            val file = File(requireContext().filesDir, categoriesFile)
            val json = Gson().toJson(categories)
            file.writeText(json)
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving categories: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCategories() {
        try {
            val file = File(requireContext().filesDir, categoriesFile)
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<ExpenseCategory>>() {}.type
                val loadedCategories = Gson().fromJson<List<ExpenseCategory>>(json, type)
                categories.clear()
                categories.addAll(loadedCategories)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading categories: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class CategoryAdapter(
    private val categories: List<ExpenseCategory>,
    private val onDeleteClick: (Int) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val categoryName: android.widget.TextView = view.findViewById(R.id.category_name)
        val deleteButton: android.widget.ImageButton = view.findViewById(R.id.delete_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryName.text = category.name
        holder.deleteButton.setOnClickListener { onDeleteClick(position) }
    }

    override fun getItemCount() = categories.size
} 