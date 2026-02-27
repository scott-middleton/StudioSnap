package com.middleton.studiosnap.purchases

import com.middleton.studiosnap.composeapp.BuildKonfig

actual fun PurchasesManager.getPlatformApiKey(): String {
    return BuildKonfig.REVENUE_CAT_ANDROID_KEY
}