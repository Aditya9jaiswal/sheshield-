package com.example.sheshield0.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sheshield0.R
import com.example.sheshield0.model.TechnicalIssue

class TechnicalAdapter(
    private val issues: List<TechnicalIssue>,
    private val onItemClick: (TechnicalIssue) -> Unit
) : RecyclerView.Adapter<TechnicalAdapter.TechnicalViewHolder>() {

    inner class TechnicalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIssueName: TextView = itemView.findViewById(R.id.tvIssueName)
        val tvIssueSection: TextView = itemView.findViewById(R.id.tvIssueSection)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TechnicalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_technical_issue, parent, false)
        return TechnicalViewHolder(view)
    }

    override fun onBindViewHolder(holder: TechnicalViewHolder, position: Int) {
        val issue = issues[position]
        holder.tvIssueName.text = issue.name_en
        holder.tvIssueSection.text = issue.section
        holder.itemView.setOnClickListener { onItemClick(issue) }
    }

    override fun getItemCount(): Int = issues.size
}
