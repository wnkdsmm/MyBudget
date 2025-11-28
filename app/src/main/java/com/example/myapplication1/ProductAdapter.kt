package com.example.myapplication1

import Product
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductAdapter(
    private val onItemClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)

        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(product: Product) {
            // Форматируем дату
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val dateString = dateFormat.format(Date(product.date))

            val typeText = if (product.type == "income") "📈 Доход" else "📉 Расход"
            val amountText = if (product.type == "income")
                "+${String.format("%.2f", product.amount)} ₽"
            else
                "-${String.format("%.2f", product.amount)} ₽"

            text1.text = "${product.category} - $amountText"
            text2.text = "$typeText • $dateString • ${product.comment}"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
