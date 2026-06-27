package com.classdrop.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemSubjectAdminBinding
import com.classdrop.model.Subject

class SubjectsAdminAdapter(
    private val onEditClick: (Subject) -> Unit,
    private val onDeleteClick: (Subject) -> Unit
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
            // Simulamos el cuatrimestre basado en el ID o un valor aleatorio para el ejemplo visual
            binding.tvSubjectSubtitle.text = "${(1..9).random()} Cuatrimestre"
            
            binding.ivSubjectIcon.setImageResource(subject.iconRes)
            
            // Aplicar colores dinámicos si están disponibles
            try {
                binding.ivSubjectIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(subject.iconBgColor))
                binding.ivSubjectIcon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(subject.iconTintColor))
            } catch (e: Exception) {
                // Colores por defecto si fallara el parseo
            }
            
            binding.btnEdit.setOnClickListener { onEditClick(subject) }
            binding.btnDelete.setOnClickListener { onDeleteClick(subject) }
        }
    }
}