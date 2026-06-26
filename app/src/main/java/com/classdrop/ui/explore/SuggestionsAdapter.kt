package com.classdrop.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemSearchSuggestionBinding

data class Suggestion(
    val title: String,
    val type: String
)

class SuggestionsAdapter(
    private val onSuggestionClick: (Suggestion) -> Unit
) : RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder>() {

    private var suggestions: List<Suggestion> = emptyList()

    fun submitList(newList: List<Suggestion>) {
        suggestions = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val binding = ItemSearchSuggestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SuggestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.binding.tvSuggestionTitle.text = suggestion.title
        holder.binding.tvSuggestionType.text = suggestion.type
        holder.itemView.setOnClickListener { onSuggestionClick(suggestion) }
    }

    override fun getItemCount(): Int = suggestions.size

    inner class SuggestionViewHolder(val binding: ItemSearchSuggestionBinding) :
        RecyclerView.ViewHolder(binding.root)
}