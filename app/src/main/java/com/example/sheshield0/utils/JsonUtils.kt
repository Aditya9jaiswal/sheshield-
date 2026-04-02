package com.example.sheshield0.utils

import android.content.Context
import com.example.sheshield0.model.TechnicalIssue
import org.json.JSONObject

object JsonUtils {
    fun loadTechnicalIssuesFromJson(context: Context): List<TechnicalIssue> {
        val issues = mutableListOf<TechnicalIssue>()
        try {
            // Open JSON from assets folder
            val inputStream = context.assets.open("technical_issues.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            // Parse JSON
            val jsonObject = JSONObject(jsonString)
            val techArray = jsonObject.getJSONArray("technical")

            for (i in 0 until techArray.length()) {
                val obj = techArray.getJSONObject(i)
                val keywordsJson = obj.getJSONArray("keywords")
                val keywordsList = mutableListOf<String>()
                for (j in 0 until keywordsJson.length()) {
                    keywordsList.add(keywordsJson.getString(j).lowercase())
                }

                issues.add(
                    TechnicalIssue(
                        id = obj.getInt("id"),
                        name_en = obj.getString("name_en"),
                        name_hi = obj.getString("name_hi"),
                        desc_en = obj.getString("desc_en"),
                        desc_hi = obj.getString("desc_hi"),
                        section = obj.getString("section"),
                        solution_en = obj.getString("solution_en"),
                        solution_hi = obj.getString("solution_hi"),
                        helpline = obj.getString("helpline"),
                        keywords = keywordsList
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return issues
    }
}
