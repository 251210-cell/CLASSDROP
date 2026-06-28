package com.classdrop.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemSubjectAdminBinding
import com.classdrop.model.Subject

class SubjectsAdminAdapter(
    private val onEditClick: (Subject) -> Unit,
    private val onDeleteClick: (Subject) -> Unit,
    private val onSubjectClick: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectsAdminAdapter.SubjectViewHolder>() {

    private var subjects: List<Subject> = emptyList()

    fun submitList(newSubjects: List<Subject>) {
        subjects = newSubjects
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(subjects[position])
    }

    override fun getItemCount(): Int = subjects.size

    inner class SubjectViewHolder(private val binding: ItemSubjectAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(subject: Subject) {
            binding.tvSubjectName.text = subject.name
            binding.tvSubjectSubtitle.text = subject.cuatrimestre
            
            binding.ivSubjectIcon.setImageResource(subject.iconRes)
            
            try {
                binding.ivSubjectIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(subject.iconBgColor))
                binding.ivSubjectIcon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(subject.iconTintColor))
            } catch (e: Exception) {
                // Fallback colors
            }
            
            binding.btnEdit.setOnClickListener { onEditClick(subject) }
            binding.btnDelete.setOnClickListener { onDeleteClick(subject) }
            
            // Navegación al detalle al pulsar la tarjeta
            binding.root.setOnClickListener { onSubjectClick(subject) }
        }
    }
}
