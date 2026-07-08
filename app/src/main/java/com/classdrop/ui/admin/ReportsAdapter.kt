package com.classdrop.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemReportCommentBinding
import com.classdrop.model.Reporte
import com.classdrop.utils.TimeUtils

// Al llegar a este número de dislikes el contenido se oculta automáticamente
// (coincide con UMBRAL_DISLIKES_ARCHIVO / UMBRAL_DISLIKES_COMENTARIO del backend).
private const val UMBRAL_DISLIKES = 5

class ReportsAdapter(
    private val onKeep: (Reporte) -> Unit,
    private val onRemove: (Reporte) -> Unit
) : ListAdapter<Reporte, ReportsAdapter.ViewHolder>(ReportDiffCallback()) {

    inner class ViewHolder(val binding: ItemReportCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReportCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reporte = getItem(position)
        holder.binding.apply {
            val esArchivo = reporte.tipoContenido == "archivo"

            // --- Quién reportó ---
            // reportador == null significa que fue el sistema el que lo mandó
            // aquí automáticamente por llegar a 5 dislikes, no una persona.
            if (reporte.reportador != null) {
                tvReporterAvatar.text = reporte.reportador.nombreCompleto.split(" ")
                    .filter { it.isNotBlank() }
                    .mapNotNull { it.firstOrNull()?.uppercase() }
                    .take(2)
                    .joinToString("")
                tvReporterName.text = "Reportado por ${reporte.reportador.nombreCompleto}"
            } else {
                tvReporterAvatar.text = "IA"
                tvReporterName.text = "Reportado automáticamente por dislikes"
            }
            tvReportTime.text = TimeUtils.tiempoRelativo(reporte.creadoEn)
            tvDislikeCount.text = "${reporte.totalDislikes}/$UMBRAL_DISLIKES"

            if (esArchivo) {
                val archivo = reporte.archivo
                tvReportContext.text = archivo?.materia?.nombre ?: "Archivo"
                tvReportedUser.text = "Archivo reportado (Usuario: ${archivo?.autor?.nombreCompleto ?: "desconocido"}):"
                tvCommentContent.text = buildString {
                    append(archivo?.titulo ?: "(sin título)")
                    if (!archivo?.descripcion.isNullOrBlank()) {
                        append("\n")
                        append(archivo?.descripcion)
                    }
                }
            } else {
                val comentario = reporte.comentario
                tvReportContext.text = comentario?.archivo?.titulo ?: "Comentario"
                tvReportedUser.text = "Comentario reportado (Usuario: ${comentario?.autor?.nombreCompleto ?: "desconocido"}):"
                tvCommentContent.text = comentario?.contenido ?: ""
            }

            btnKeep.setOnClickListener { onKeep(reporte) }
            btnRemove.setOnClickListener { onRemove(reporte) }
        }
    }

    class ReportDiffCallback : DiffUtil.ItemCallback<Reporte>() {
        override fun areItemsTheSame(oldItem: Reporte, newItem: Reporte): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Reporte, newItem: Reporte): Boolean = oldItem == newItem
    }
}