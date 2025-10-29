package com.example.meditrackpro.ui.meds

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrackpro.data.entity.Medication

class MedsAdapter(private val isCardLayout: Boolean) : ListAdapter<Medication, RecyclerView.ViewHolder>(MedicationDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // TODO: Inflate list or card layout
        return object : RecyclerView.ViewHolder(parent) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // TODO: Bind data to view holder
    }
}

class MedicationDiffCallback : DiffUtil.ItemCallback<Medication>() {
    override fun areItemsTheSame(oldItem: Medication, newItem: Medication): Boolean {
        return oldItem.medId == newItem.medId
    }

    override fun areContentsTheSame(oldItem: Medication, newItem: Medication): Boolean {
        return oldItem == newItem
    }
}
