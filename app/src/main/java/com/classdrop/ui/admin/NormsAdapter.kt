package com.classdrop.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemRuleAdminBinding
import com.classdrop.model.CommunityRule

class NormsAdapter(
    private var rules: List<CommunityRule>,
    private val onEditClick: (CommunityRule) -> Unit,
    private val onDeleteClick: (CommunityRule) -> Unit
) : RecyclerView.Adapter<NormsAdapter.RuleViewHolder>() {

    inner class RuleViewHolder(private val binding: ItemRuleAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(rule: CommunityRule) {
            binding.apply {
                tvRuleTitle.text = rule.title
                tvRuleDescription.text = rule.description

                btnEdit.setOnClickListener { onEditClick(rule) }
                btnDelete.setOnClickListener { onDeleteClick(rule) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleViewHolder {
        val binding = ItemRuleAdminBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RuleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RuleViewHolder, position: Int) {
        holder.bind(rules[position])
    }

    override fun getItemCount(): Int = rules.size

    fun updateData(newRules: List<CommunityRule>) {
        rules = newRules
        notifyDataSetChanged()
    }
}
