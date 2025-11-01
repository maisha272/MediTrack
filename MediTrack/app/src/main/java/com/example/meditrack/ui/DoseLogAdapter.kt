package com.example.meditrack.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R



class DoseLogAdapter(private var items: List<DoseDisplay>) :
    RecyclerView.Adapter<DoseLogAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvMedName)
        val time: TextView = v.findViewById(R.id.tvTime)
        val status: TextView = v.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(p.context)
            .inflate(R.layout.row_dose, p, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, i: Int) {
        val item = items[i]
        h.name.text = item.medName
        h.time.text = item.time
        h.status.text = item.status

        // --- NEW ICON LOGIC ---
        val icon = when {
            item.status.contains("Taken") -> R.drawable.ic_done
            item.status.contains("Missed") -> R.drawable.ic_snooze
            else -> R.drawable.ic_notification_pill
        }

        h.status.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
        h.status.compoundDrawablePadding = 8
    }

    fun submit(list: List<DoseDisplay>) {
        items = list
        notifyDataSetChanged()
    }
}

