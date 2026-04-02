package com.example.sheshield0.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sheshield0.R
import com.example.sheshield0.model.FamilyMember

class FamilyAdapter(private var familyList: List<FamilyMember>) :
    RecyclerView.Adapter<FamilyAdapter.FamilyViewHolder>() {

    // Update the adapter data
    fun updateData(newList: List<FamilyMember>) {
        familyList = newList
        notifyDataSetChanged()
    }

    // Getter for current family list
    fun getFamilyList(): List<FamilyMember> = familyList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FamilyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_family_member, parent, false)
        return FamilyViewHolder(view)
    }

    override fun onBindViewHolder(holder: FamilyViewHolder, position: Int) {
        val member = familyList[position]
        holder.tvName.text = member.name
        holder.tvRelation.text = member.relation
        holder.tvMobile.text = member.mobile
    }

    override fun getItemCount(): Int = familyList.size

    class FamilyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvFamilyName)
        val tvRelation: TextView = itemView.findViewById(R.id.tvFamilyRelation)
        val tvMobile: TextView = itemView.findViewById(R.id.tvFamilyMobile)
    }
}
