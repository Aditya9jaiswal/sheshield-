package com.example.sheshield0.model
import java.io.Serializable
data class TechnicalIssue(
    val id: Int,
    val name_en: String,
    val name_hi: String,
    val desc_en: String,
    val desc_hi: String,
    val section: String,
    val solution_en: String,
    val solution_hi: String,
    val helpline: String,
    val keywords: List<String>
) : Serializable

