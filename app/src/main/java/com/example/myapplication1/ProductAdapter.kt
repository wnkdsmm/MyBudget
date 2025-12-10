package com.example.myapplication1


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductAdapter(
    private val onItemClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_card, parent, false)
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
        private val categoryText: TextView = itemView.findViewById(R.id.text_category)
        private val amountText: TextView = itemView.findViewById(R.id.text_amount)
        private val typeDate: TextView = itemView.findViewById(R.id.text_type_date)
        private val comment: TextView = itemView.findViewById(R.id.text_comment)

        fun bind(product: Product) {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val dateString = dateFormat.format(Date(product.date))

            val isIncome = product.type == "income"
            val amountDisplay = if (isIncome) {
                "+${String.format("%.2f", product.amount)} ₽"
            } else {
                "-${String.format("%.2f", product.amount)} ₽"
            }

            val typeDisplay = if (isIncome) "📈 Доход" else "📉 Расход"

            categoryText.text = product.category
            amountText.text = amountDisplay
            typeDate.text = "$typeDisplay • $dateString"
            comment.text = product.comment.ifEmpty { "Без комментария" }

            // Цвет суммы: зелёный для дохода, красный для расхода
            val amountColor = if (isIncome) 0xFF4CAF50.toInt() else 0xFFF44336.toInt()
            amountText.setTextColor(amountColor)
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
