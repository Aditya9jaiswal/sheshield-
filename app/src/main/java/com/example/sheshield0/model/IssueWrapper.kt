package com.example.sheshield0.model

data class IssueWrapper(
    val physical: List<TechnicalIssue>,
    val technical: List<TechnicalIssue>
)
