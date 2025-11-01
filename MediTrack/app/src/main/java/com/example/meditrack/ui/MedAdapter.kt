package com.example.meditrack.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.data.MedWithNext
import java.time.format.DateTimeFormatter

class MedAdapter(
    private var items: List<MedWithNext>,
    private val onClick: (MedWithNext) -> Unit
) : RecyclerView.Adapter<MedAdapter.VH>() {

    private val fmt = DateTimeFormatter.ofPattern("EEE, d MMM • HH:mm")

    class VH(v: View): RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvName)
        val dosage: TextView = v.findViewById(R.id.tvDosage)
        val next: TextView = v.findViewById(R.id.tvNext)
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.row_med, p, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, i: Int) {
        val m = items[i]
        h.name.text = m.med.name
        h.dosage.text = m.med.dosage
        h.next.text = m.nextDue?.let { "Next: ${it.format(fmt)}" } ?: "Next: —"
        h.itemView.setOnClickListener { onClick(m) }
    }

    fun submit(list: List<MedWithNext>?) {
        items = list ?: emptyList()
        notifyDataSetChanged()
    }
}
