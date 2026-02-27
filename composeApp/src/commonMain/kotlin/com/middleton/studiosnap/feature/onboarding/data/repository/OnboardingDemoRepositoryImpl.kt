package com.middleton.studiosnap.feature.onboarding.data.repository

import androidx.compose.ui.graphics.Color
import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.feature.onboarding.domain.model.DemoExample
import com.middleton.studiosnap.feature.onboarding.domain.repository.OnboardingDemoRepository
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.after_belfast_street
import studiosnap.composeapp.generated.resources.after_damaged_couple
import studiosnap.composeapp.generated.resources.after_diverse_group
import studiosnap.composeapp.generated.resources.after_gadouas_family
import studiosnap.composeapp.generated.resources.after_man_paint_can
import studiosnap.composeapp.generated.resources.after_tripoli_girl
import studiosnap.composeapp.generated.resources.before_belfast_street
import studiosnap.composeapp.generated.resources.before_damaged_couple
import studiosnap.composeapp.generated.resources.before_diverse_group
import studiosnap.composeapp.generated.resources.before_gadouas_family
import studiosnap.composeapp.generated.resources.before_man_paint_can
import studiosnap.composeapp.generated.resources.before_tripoli_girl
import studiosnap.composeapp.generated.resources.demo_color_description
import studiosnap.composeapp.generated.resources.demo_color_title
import studiosnap.composeapp.generated.resources.demo_damage_description
import studiosnap.composeapp.generated.resources.demo_damage_title
import studiosnap.composeapp.generated.resources.demo_faces_description
import studiosnap.composeapp.generated.resources.demo_faces_title
import studiosnap.composeapp.generated.resources.demo_label_bw
import studiosnap.composeapp.generated.resources.demo_label_colorized
import studiosnap.composeapp.generated.resources.demo_label_damaged
import studiosnap.composeapp.generated.resources.demo_label_enhanced
import studiosnap.composeapp.generated.resources.demo_label_blurry
import studiosnap.composeapp.generated.resources.demo_label_restored

class OnboardingDemoRepositoryImpl : OnboardingDemoRepository {

    override fun getDemoSet1(): List<DemoExample> {
        return listOf(
            DemoExample(
                title = UiText.StringResource(Res.string.demo_damage_title),
                description = UiText.StringResource(Res.string.demo_damage_description),
                beforeLabel = UiText.StringResource(Res.string.demo_label_damaged),
                afterLabel = UiText.StringResource(Res.string.demo_label_restored),
                beforeColor = Color.Gray,
                afterColor = Color(0xFF8B5CF6),
                beforeImage = Res.drawable.before_tripoli_girl,
                afterImage = Res.drawable.after_tripoli_girl
            ),
            DemoExample(
                title = UiText.StringResource(Res.string.demo_damage_title),
                description = UiText.StringResource(Res.string.demo_damage_description),
                beforeLabel = UiText.StringResource(Res.string.demo_label_damaged),
                afterLabel = UiText.StringResource(Res.string.demo_label_restored),
                beforeColor = Color.Gray,
                afterColor = Color(0xFF8B5CF6),
                beforeImage = Res.drawable.before_damaged_couple,
                afterImage = Res.drawable.after_damaged_couple
            )
        )
    }

    override fun getDemoSet2(): List<DemoExample> {
        return listOf(
            DemoExample(
                title = UiText.StringResource(Res.string.demo_color_title),
                description = UiText.StringResource(Res.string.demo_color_description),
                beforeLabel = UiText.StringResource(Res.string.demo_label_bw),
                afterLabel = UiText.StringResource(Res.string.demo_label_colorized),
                beforeColor = Color.Gray,
                afterColor = Color(0xFFF59E0B),
                beforeImage = Res.drawable.before_belfast_street,
                afterImage = Res.drawable.after_belfast_street
            ),
            DemoExample(
                title = UiText.StringResource(Res.string.demo_color_title),
                description = UiText.StringResource(Res.string.demo_color_description),
                beforeLabel = UiText.StringResource(Res.string.demo_label_bw),
                afterLabel = UiText.StringResource(Res.string.demo_label_colorized),
                beforeColor = Color.Gray,
                afterColor = Color(0xFFF59E0B),
                beforeImage = Res.drawable.before_diverse_group,
                afterImage = Res.drawable.after_diverse_group
            )
        )
    }

    override fun getDemoSet3(): List<DemoExample> {
        return listOf(
            DemoExample(
                title = UiText.StringResource(Res.string.demo_faces_title),
                description = UiText.StringResource(Res.string.demo_faces_description),
                beforeLabel = UiText.StringResource(Res.string.demo_label_blurry),
                afterLabel = UiText.StringResource(Res.string.demo_label_enhanced),
                beforeColor = Color.Gray.copy(alpha = 0.5f),
                afterColor = Color(0xFF10B981),
                beforeImage = Res.drawable.before_gadouas_family,
                afterImage = Res.drawable.after_gadouas_family
            ),
            DemoExample(
                title = UiText.StringResource(Res.string.demo_faces_title),
                description = UiText.StringResource(Res.string.demo_faces_description),
                beforeLabel = UiText.StringResource(Res.string.demo_label_blurry),
                afterLabel = UiText.StringResource(Res.string.demo_label_enhanced),
                beforeColor = Color.Gray.copy(alpha = 0.5f),
                afterColor = Color(0xFF10B981),
                beforeImage = Res.drawable.before_man_paint_can,
                afterImage = Res.drawable.after_man_paint_can
            )
        )
    }
}
