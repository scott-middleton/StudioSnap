package com.middleton.studiosnap.feature.onboarding.domain.repository

import com.middleton.studiosnap.feature.onboarding.domain.model.DemoExample

interface OnboardingDemoRepository {
    fun getDemoSet1(): List<DemoExample>
    fun getDemoSet2(): List<DemoExample>
    fun getDemoSet3(): List<DemoExample>
}