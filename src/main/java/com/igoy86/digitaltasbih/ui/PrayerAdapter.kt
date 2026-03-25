package com.igoy86.digitaltasbih.ui

import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.igoy86.digitaltasbih.R
import com.igoy86.digitaltasbih.data.model.PrayerTime
import com.igoy86.digitaltasbih.databinding.ItemPrayerTimeBinding

class PrayerAdapter(
    private val onAlarmToggle: (PrayerTime, Boolean) -> Unit
) : ListAdapter<PrayerTime, PrayerAdapter.ViewHolder>(DiffCallback()) {

    private var primaryHex: String = "#4CAF50"
    private var darkHex: String = "#388E3C"

    fun updateThemeColor(primary: String, dark: String) {
        primaryHex = primary
        darkHex = dark
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPrayerTimeBinding) :
        RecyclerView.ViewHolder(binding.root) {

       fun bind(item: PrayerTime) {
            binding.tvPrayerName.text = item.name
            binding.tvPrayerTime.text = item.time

            // Hide lonceng untuk Imsak & Terbit
            val isAlarmable = item.key != "imsak" && item.key != "sunrise"
            binding.btnAlarm.visibility = if (isAlarmable) View.VISIBLE else View.GONE

            // Background card ikut tema
            val bgColor = when {
                item.isNext -> Color.parseColor(primaryHex)
                item.isPast -> Color.parseColor(darkHex)
                else -> resolveAttr(com.google.android.material.R.attr.colorSurface)
            }
            binding.cardPrayer.setCardBackgroundColor(bgColor)

            // Teks kontras
            val textColor = if (item.isNext || item.isPast)
                Color.WHITE
            else
                resolveAttr(com.google.android.material.R.attr.colorOnSurface)

            binding.tvPrayerName.setTextColor(textColor)
            binding.tvPrayerTime.setTextColor(textColor)

            // Alpha redup untuk yang sudah lewat
            binding.cardPrayer.alpha = if (item.isPast && !item.isNext) 0.5f else 1.0f

            // Badge berikutnya
            binding.tvNextBadge.visibility = if (item.isNext) View.VISIBLE else View.GONE

            // Icon alarm (hanya untuk yang isAlarmable)
            if (isAlarmable) {
                binding.btnAlarm.setImageResource(
                    if (item.notifEnabled) R.drawable.ic_alarm_on
                    else R.drawable.ic_alarm_off
                )
                binding.btnAlarm.setOnClickListener {
                    onAlarmToggle(item, !item.notifEnabled)
                }
            }
        }

        private fun resolveAttr(attr: Int): Int {
            val typedValue = TypedValue()
            binding.root.context.theme.resolveAttribute(attr, typedValue, true)
            return typedValue.data
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPrayerTimeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<PrayerTime>() {
        override fun areItemsTheSame(oldItem: PrayerTime, newItem: PrayerTime) =
            oldItem.key == newItem.key
        override fun areContentsTheSame(oldItem: PrayerTime, newItem: PrayerTime) =
            oldItem == newItem
    }
}