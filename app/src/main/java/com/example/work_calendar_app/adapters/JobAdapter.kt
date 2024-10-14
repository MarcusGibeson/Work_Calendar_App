package com.example.work_calendar_app.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.work_calendar_app.data.Job


class JobAdapter(context: Context, private val jobs: List<Job>) : ArrayAdapter<Job>(context, android.R.layout.simple_spinner_item, jobs ) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        return view
    }

    override fun getItem(position: Int): Job {
        return jobs[position]
    }

    override fun getItemId(position: Int): Long {
        return jobs[position].id
    }

    override fun toString(): String {
        return jobs.joinToString(", ") { it.name }
    }
}