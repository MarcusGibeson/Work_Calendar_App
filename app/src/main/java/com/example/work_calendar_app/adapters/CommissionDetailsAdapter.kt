package com.example.work_calendar_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.work_calendar_app.R

class CommissionDetailsAdapter(private val commissionDetails: List<Double>) : RecyclerView.Adapter<CommissionDetailsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val commissionDetailTextView: TextView = view.findViewById(R.id.commissionDetailTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.commission_detail_item, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.commissionDetailTextView.text = commissionDetails[position].toString()
    }

    override fun getItemCount() = commissionDetails.size

    fun getCommissionList(): List<Double> {
        return commissionDetails
    }
}