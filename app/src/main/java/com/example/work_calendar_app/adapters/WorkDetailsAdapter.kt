package com.example.work_calendar_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.work_calendar_app.R
import com.example.work_calendar_app.data.WorkDetails

class WorkDetailsAdapter(private var workDetailsList: MutableList<WorkDetails>) : RecyclerView.Adapter<WorkDetailsAdapter.WorkDetailsViewHolder>() {

    //Inner class to hold the views for each work details item
    class WorkDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workDateTextView: TextView = itemView.findViewById<TextView>(R.id.work_date)
        val startTimeTextView: TextView = itemView.findViewById<TextView>(R.id.start_time)
        val endTimeTextView: TextView = itemView.findViewById<TextView>(R.id.end_time)

        val wageTextView: TextView = itemView.findViewById<TextView>(R.id.wage_text)
    }

    //Creates a new ViewHolder object for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkDetailsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_work_detail, parent, false)
        return WorkDetailsViewHolder(view)
    }

    //Binds the data to the views in each ViewHolder
    override fun onBindViewHolder(holder: WorkDetailsViewHolder, position: Int) {
        val workDetail = workDetailsList[position]

        holder.workDateTextView.text = workDetail.workDate
        holder.startTimeTextView.text = workDetail.startTime
        holder.endTimeTextView.text = workDetail.endTime
        holder.endTimeTextView.text = workDetail.tips.toString()
        holder.wageTextView.text = workDetail.wage.toString()
    }

    override fun getItemCount(): Int {
        return workDetailsList.size
    }



    //Function to update the work details list and refresh the RecyclerView
    fun updateData(newWorkDetails: List<WorkDetails>) {
        workDetailsList.clear()
        workDetailsList.addAll(newWorkDetails)
        notifyDataSetChanged()
    }
}