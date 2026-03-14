package com.middleton.studiosnap.core.data.service

/**
 * Initializes Firebase App Check on each platform.
 * Android uses Play Integrity, iOS uses App Attest.
 * Must be called before any Firebase Cloud Function calls.
 */
expect fun initializeAppCheck()
