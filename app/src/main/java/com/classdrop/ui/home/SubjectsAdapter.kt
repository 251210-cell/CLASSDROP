package com.classdrop.ui.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemSubjectHomeBinding
import com.classdrop.model.MateriaResponse
import com.classdrop.utils.IconMapper

class SubjectsAdapter(
    private val onSubjectClick: (MateriaResponse) -> Unit
) : RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder>() {

    private var subjects: List<MateriaResponse> = emptyList()

    fun submitList(newSubjects: List<MateriaResponse>) {
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

        fun bind(subject: MateriaResponse) {
            binding.tvSubjectName.text = subject.nombre
            binding.tvFileCount.text = "${subject.fileCount ?: 0} archivos"

            val style = IconMapper.fromKey(subject.icono)
            binding.ivSubjectIcon.setImageResource(style.drawableRes)
            binding.ivSubjectIcon.backgroundTintList = ColorStateList.valueOf(Color.parseColor(style.bgColor))
            binding.ivSubjectIcon.imageTintList = ColorStateList.valueOf(Color.parseColor(style.tintColor))

            binding.cardSubject.setOnClickListener { onSubjectClick(subject) }
        }
    }
}