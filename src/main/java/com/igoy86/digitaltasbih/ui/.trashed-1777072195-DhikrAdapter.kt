package com.igoy86.digitaltasbih.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.igoy86.digitaltasbih.R
import com.igoy86.digitaltasbih.data.model.DhikrEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DhikrAdapter(
    private val onDelete: (DhikrEntity) -> Unit,
    private val onToggleFavorite: (DhikrEntity) -> Unit,
    private val onEditName: (DhikrEntity) -> Unit,
    private val onLoad: (DhikrEntity) -> Unit
) : ListAdapter<DhikrEntity, DhikrAdapter.ViewHolder>(DiffCallback()) {

    // Warna tema saat ini — diperbarui dari Activity via updateThemeColor()
    private var themeColorHex: String = "#1A531A"

    /**
     * Dipanggil dari Activity setiap kali tema berubah.
     * notifyDataSetChanged() agar semua card langsung diperbarui.
     */
    fun updateThemeColor(darkHex: String) {
        themeColorHex = darkHex
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView       = view.findViewById(R.id.card_dhikr)
        val tvName: TextView     = view.findViewById(R.id.tv_dhikr_name)
        val tvCount: TextView    = view.findViewById(R.id.tv_dhikr_count)
        val tvTarget: TextView   = view.findViewById(R.id.tv_dhikr_target)
        val tvDate: TextView     = view.findViewById(R.id.tv_dhikr_date)
        val btnFavorite: ImageButton = view.findViewById(R.id.ibtn_favorite)
        val btnDelete: ImageButton   = view.findViewById(R.id.ibtn_delete)
        val btnEdit: ImageButton     = view.findViewById(R.id.ibtn_edit)
        val btnLoad: ImageButton     = view.findViewById(R.id.ibtn_load)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dhikr_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dhikr      = getItem(position)
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id"))

        holder.tvName.text   = dhikr.name
        holder.tvCount.text  = "Count: ${dhikr.count}"
        holder.tvTarget.text = if (dhikr.target > 0) "Target: ${dhikr.target}" else "Target: -"
        holder.tvDate.text   = dateFormat.format(Date(dhikr.savedAt))

        holder.btnFavorite.setImageResource(
            if (dhikr.isFavorite) R.drawable.ic_star_solid
            else R.drawable.ic_star_outline
        )

        // Terapkan warna tema ke card
        val cardColor = Color.parseColor(themeColorHex)
        holder.card.setCardBackgroundColor(cardColor)

        // Buat teks tetap terbaca di atas card berwarna
        val textColor = if (isColorDark(cardColor)) Color.WHITE else Color.BLACK
        holder.tvName.setTextColor(textColor)
        holder.tvCount.setTextColor(ColorUtils.blendARGB(textColor, cardColor, 0.2f))
        holder.tvTarget.setTextColor(ColorUtils.blendARGB(textColor, cardColor, 0.2f))
        holder.tvDate.setTextColor(ColorUtils.blendARGB(textColor, cardColor, 0.3f))

        holder.btnFavorite.setOnClickListener {
            onToggleFavorite(dhikr)
            val msg = if (dhikr.isFavorite) "Dihapus dari favorit" else "Ditambahkan ke favorit ⭐"
            Toast.makeText(holder.itemView.context, msg, Toast.LENGTH_SHORT).show()
        }
        holder.btnDelete.setOnClickListener { onDelete(dhikr) }
        holder.btnEdit.setOnClickListener   { onEditName(dhikr) }
        holder.btnLoad.setOnClickListener   { onLoad(dhikr) }
    }

    /** Cek apakah warna gelap (untuk memilih teks putih/hitam) */
    private fun isColorDark(color: Int): Boolean {
        val luminance = (0.299 * Color.red(color) +
                         0.587 * Color.green(color) +
                         0.114 * Color.blue(color)) / 255
        return luminance < 0.5
    }

    class DiffCallback : DiffUtil.ItemCallback<DhikrEntity>() {
        override fun areItemsTheSame(a: DhikrEntity, b: DhikrEntity) = a.id == b.id
        override fun areContentsTheSame(a: DhikrEntity, b: DhikrEntity) = a == b
    }
}







