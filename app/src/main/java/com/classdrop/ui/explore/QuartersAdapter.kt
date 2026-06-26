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

        if (position == selectedPosition) {
            holder.binding.cardQuarter.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.primary))
            holder.binding.tvQuarterNumber.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        } else {
            holder.binding.cardQuarter.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.surface_variant))
            holder.binding.tvQuarterNumber.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.on_surface))
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