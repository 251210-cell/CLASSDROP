package com.classdrop.ui.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemSubjectHomeBinding
import com.classdrop.model.Subject

class SubjectsAdapter(
    private val onSubjectClick: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder>() {

    private var subjects: List<Subject> = emptyList()

    fun submitList(newSubjects: List<Subject>) {
        subjects = newSubjects
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectHomeBinding.inflate(
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

    inner class SubjectViewHolder(private val binding: ItemSubjectHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(subject: Subject) {
            binding.tvSubjectName.text = subject.name
            binding.tvFileCount.text = "${subject.fileCount} archivos"
            
            // Aplicar icono y colores dinámicos elegidos por el admin
            binding.ivSubjectIcon.setImageResource(subject.iconRes)
            binding.ivSubjectIcon.backgroundTintList = ColorStateList.valueOf(Color.parseColor(subject.iconBgColor))
            binding.ivSubjectIcon.imageTintList = ColorStateList.valueOf(Color.parseColor(subject.iconTintColor))
            
            binding.cardSubject.setOnClickListener { onSubjectClick(subject) }
        }
    }
}
