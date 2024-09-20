package com.example.work_calendar_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.work_calendar_app.R
import com.example.work_calendar_app.data.WorkDetails

class WorkDetailsAdapter(private var workDetailsList: MutableList<WorkDetails>) : RecyclerView.Adapter<WorkDetailsAdapter.WorkDetailsViewHolder>() {

    //Creates a new ViewHolder object for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkDetailsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_work_detail, parent, false)
        return WorkDetailsViewHolder(view)
    }

    //Binds the data to the views in each ViewHolder
    override fun onBindViewHolder(holder: WorkDetailsViewHolder, position: Int) {
        val workDetail = workDetailsList[position]
        holder.bind(workDetail)
    }

    override fun getItemCount(): Int = workDetailsList.size

    //Inner class to hold the views for each work details item
    class WorkDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(workDetail: WorkDetails) {
            itemView.findViewById<TextView>(R.id.work_date).text = workDetail.workDate
            itemView.findViewById<TextView>(R.id.start_time).text = workDetail.startTime
            itemView.findViewById<TextView>(R.id.end_time).text = workDetail.endTime

        }
    }
}