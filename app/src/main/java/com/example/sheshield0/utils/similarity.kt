package com.example.sheshield0.utils

fun String.similarity(other: String): Double {
    val s1 = this.lowercase()
    val s2 = other.lowercase()
    val longer = if (s1.length > s2.length) s1 else s2
    val shorter = if (s1.length > s2.length) s2 else s1
    val longerLength = longer.length
    if (longerLength == 0) return 1.0
    val distance = levenshtein(longer, shorter)
    return (longerLength - distance) / longerLength.toDouble()
}

private fun levenshtein(lhs: String, rhs: String): Int {
    val lhsLen = lhs.length
    val rhsLen = rhs.length
    val dp = Array(lhsLen + 1) { IntArray(rhsLen + 1) }
    for (i in 0..lhsLen) dp[i][0] = i
    for (j in 0..rhsLen) dp[0][j] = j
    for (i in 1..lhsLen) {
        for (j in 1..rhsLen) {
            dp[i][j] = if (lhs[i - 1] == rhs[j - 1]) {
                dp[i - 1][j - 1]
            } else {
                1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
            }
        }
    }
    return dp[lhsLen][rhsLen]
}
