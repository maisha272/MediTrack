package com.example.meditrackpro.ui.meds

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrackpro.data.entity.Medication
import com.example.meditrackpro.databinding.ItemMedicationCardBinding
import com.example.meditrackpro.databinding.ItemMedicationListBinding

class MedsAdapter(
    private val isCardLayout: Boolean = false,
    private val onMedicationClick: (Medication) -> Unit
) : ListAdapter<Medication, RecyclerView.ViewHolder>(MedicationDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return if (isCardLayout) VIEW_TYPE_CARD else VIEW_TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LIST -> {
                val binding = ItemMedicationListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ListViewHolder(binding)
            }
            VIEW_TYPE_CARD -> {
                val binding = ItemMedicationCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CardViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val medication = getItem(position)
        when (holder) {
            is ListViewHolder -> holder.bind(medication)
            is CardViewHolder -> holder.bind(medication)
        }
    }

    inner class ListViewHolder(private val binding: ItemMedicationListBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                onMedicationClick(getItem(adapterPosition))
            }
        }

        fun bind(medication: Medication) {
            // TODO: Bind data for list view
        }
    }

    inner class CardViewHolder(private val binding: ItemMedicationCardBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                onMedicationClick(getItem(adapterPosition))
            }
        }

        fun bind(medication: Medication) {
            // TODO: Bind data for card view
        }
    }

    companion object {
        private const val VIEW_TYPE_LIST = 0
        private const val VIEW_TYPE_CARD = 1
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
