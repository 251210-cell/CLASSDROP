package com.classdrop.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.R
import com.classdrop.databinding.ItemQuarterBinding

class QuartersAdapter(
    private val quarters: List<String>,
    private val onQuarterSelected: (String) -> Unit
) : RecyclerView.Adapter<QuartersAdapter.QuarterViewHolder>() {

    private var selectedPosition = -1

    inner class QuarterViewHolder(val binding: ItemQuarterBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuarterViewHolder {
        val binding = ItemQuarterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuarterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuarterViewHolder, position: Int) {
        val quarter = quarters[position]
        holder.binding.tvQuarterNumber.text = quarter

        val context = holder.itemView.context
        val typedValue = android.util.TypedValue()

        if (position == selectedPosition) {
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
            holder.binding.cardQuarter.setCardBackgroundColor(typedValue.data)
            
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
            holder.binding.tvQuarterNumber.setTextColor(typedValue.data)
        } else {
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, typedValue, true)
            holder.binding.cardQuarter.setCardBackgroundColor(typedValue.data)
            
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
            holder.binding.tvQuarterNumber.setTextColor(typedValue.data)
        }

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onQuarterSelected(quarter)
        }
    }

    override fun getItemCount(): Int = quarters.size
}