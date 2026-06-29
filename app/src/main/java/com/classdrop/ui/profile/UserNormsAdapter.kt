package com.classdrop.ui.profile

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemRuleUserBinding
import com.classdrop.model.CommunityRule

class UserNormsAdapter(
    private var rules: List<CommunityRule>
) :
    RecyclerView.Adapter<UserNormsAdapter.UserRuleViewHolder>() {

    inner class UserRuleViewHolder(private val binding: ItemRuleUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(rule: CommunityRule) {
            binding.tvRuleTitle.text = rule.title
            binding.tvRuleDescription.text = rule.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRuleViewHolder {
        val binding = ItemRuleUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserRuleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserRuleViewHolder, position: Int) {
        holder.bind(rules[position])
    }

    override fun getItemCount(): Int = rules.size

    fun updateData(newRules: List<CommunityRule>) {
        rules = newRules
        notifyDataSetChanged()
    }
}
