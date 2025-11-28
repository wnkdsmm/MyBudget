package com.example.myapplication1.ui.home

import ProductRepository
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication1.BudgetApp
import com.example.myapplication1.ProductAdapter
import com.example.myapplication1.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var repository: ProductRepository
    private lateinit var adapter: ProductAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Создаем простой layout программно
        val rootView = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
        }

        // Добавляем заголовок
        val title = TextView(requireContext()).apply {
            text = "Все операции"
            textSize = 20f
            gravity = android.view.Gravity.CENTER
            setPadding(50, 50, 50, 50) // dp to pixels
        }
        rootView.addView(title)

        // Добавляем RecyclerView
        recyclerView = RecyclerView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        rootView.addView(recyclerView)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем repository из Application
        repository = (requireActivity().application as BudgetApp).repository

        setupRecyclerView()
        observeProducts()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun observeProducts() {
        lifecycleScope.launch {
            repository.allProducts.collect { products ->
                adapter.submitList(products)

                if (products.isEmpty()) {
                    Toast.makeText(requireContext(), "Нет записей", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}