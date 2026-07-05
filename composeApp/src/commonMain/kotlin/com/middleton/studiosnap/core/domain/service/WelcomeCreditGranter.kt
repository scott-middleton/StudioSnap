package com.middleton.studiosnap.core.domain.service

interface WelcomeCreditGranter {
    suspend fun claimWelcomeCredits(): Boolean
}
