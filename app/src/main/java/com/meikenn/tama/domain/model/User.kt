package com.meikenn.tama.domain.model

data class User(
    val id: String,
    val username: String,
    val fullName: String,
    val encryptedPassword: String? = null,
    val allKeijiMidokCnt: Int? = null,
    val deviceToken: String? = null,
    val maxJigenNo: Int? = null
)
