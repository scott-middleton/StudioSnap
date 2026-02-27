package com.middleton.studiosnap.feature.onboarding.data.repository

import androidx.compose.ui.graphics.Color
import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.feature.onboarding.domain.model.DemoExample
import com.middleton.studiosnap.feature.onboarding.domain.repository.OnboardingDemoRepository

class FakeOnboardingDemoRepository : OnboardingDemoRepository {
    
    private val testDemoSet1 = listOf(
        DemoExample(
            title = UiText.DynamicString("Test Faces"),
            description = UiText.DynamicString("Test face enhancement"),
            beforeLabel = UiText.DynamicString("Blurry"),
            afterLabel = UiText.DynamicString("Enhanced"),
            beforeColor = Color.Gray,
            afterColor = Color.Green,
            beforeImage = null,
            afterImage = null
        ),
        DemoExample(
            title = UiText.DynamicString("Test Color"),
            description = UiText.DynamicString("Test colorization"),
            beforeLabel = UiText.DynamicString("B&W"),
            afterLabel = UiText.DynamicString("Colorized"),
            beforeColor = Color.Gray,
            afterColor = Color.Blue,
            beforeImage = null,
            afterImage = null
        )
    )
    
    private val testDemoSet2 = listOf(
        DemoExample(
            title = UiText.DynamicString("Test Damage"),
            description = UiText.DynamicString("Test damage repair"),
            beforeLabel = UiText.DynamicString("Damaged"),
            afterLabel = UiText.DynamicString("Restored"),
            beforeColor = Color.Gray,
            afterColor = Color.Red,
            beforeImage = null,
            afterImage = null
        ),
        DemoExample(
            title = UiText.DynamicString("Test Fading"),
            description = UiText.DynamicString("Test fade restoration"),
            beforeLabel = UiText.DynamicString("Faded"),
            afterLabel = UiText.DynamicString("Restored"),
            beforeColor = Color.Gray,
            afterColor = Color.White,
            beforeImage = null,
            afterImage = null
        )
    )
    
    private val testDemoSet3 = listOf(
        DemoExample(
            title = UiText.DynamicString("Test Faces"),
            description = UiText.DynamicString("Test face restoration"),
            beforeLabel = UiText.DynamicString("Blurry"),
            afterLabel = UiText.DynamicString("Enhanced"),
            beforeColor = Color.Gray,
            afterColor = Color.Green,
            beforeImage = null,
            afterImage = null
        )
    )
    
    override fun getDemoSet1(): List<DemoExample> = testDemoSet1
    
    override fun getDemoSet2(): List<DemoExample> = testDemoSet2
    
    override fun getDemoSet3(): List<DemoExample> = testDemoSet3
}