package com.nb.app.domain.model

data class Player(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val team: Team?,
)
