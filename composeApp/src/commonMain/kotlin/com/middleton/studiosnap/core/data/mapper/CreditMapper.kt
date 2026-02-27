package com.middleton.studiosnap.core.data.mapper

import com.middleton.studiosnap.core.domain.model.UserCredits

/**
 * Maps raw token balance (Int) from backend to Domain model
 */
fun Int.toUserCredits(): UserCredits {
    return UserCredits(
        amount = this
    )
}
