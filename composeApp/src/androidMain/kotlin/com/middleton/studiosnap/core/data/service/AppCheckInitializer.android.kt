package com.middleton.studiosnap.core.data.service

import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.middleton.studiosnap.composeapp.BuildKonfig

actual fun initializeAppCheck() {
    val factory = if (BuildKonfig.IS_DEBUG) {
        DebugAppCheckProviderFactory.getInstance()
    } else {
        PlayIntegrityAppCheckProviderFactory.getInstance()
    }
    Firebase.appCheck.installAppCheckProviderFactory(factory)
}
