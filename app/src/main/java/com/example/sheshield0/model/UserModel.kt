package com.example.sheshield0.model

data class UserModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val mobile: String = "",
    val age: String = "",
    val gender: String = "",
    var password: String = "",
    val fcmToken: String = "",
    val familyMembers: List<FamilyMember> = emptyList()

)

