package com.middleton.studiosnap.core.domain.model

data class UserCredits(
    val amount: Int = 0
) {
    fun hasEnough(cost: Int): Boolean = amount >= cost
}
