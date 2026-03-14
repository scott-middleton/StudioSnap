package com.middleton.studiosnap.core.data.auth

import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.OAuthProvider
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.reinterpret
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * iOS implementation using Apple Sign-In via ASAuthorizationController.
 * Presents the Apple Sign-In sheet and returns a Firebase OAuthCredential.
 */
actual class NativeAuthProvider {

    /**
     * Strong reference to the delegate to prevent garbage collection.
     * ASAuthorizationController.delegate is a weak reference in ObjC,
     * so without this the delegate could be collected before the callback fires.
     */
    private var currentDelegate: NSObject? = null

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual suspend fun getCredential(): AuthCredential {
        return suspendCancellableCoroutine { continuation ->
            val rawNonce = generateRandomNonce()
            val hashedNonce = sha256(rawNonce)

            val provider = ASAuthorizationAppleIDProvider()
            val request = provider.createRequest()
            request.requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
            request.nonce = hashedNonce

            val delegate = object : NSObject(),
                ASAuthorizationControllerDelegateProtocol,
                ASAuthorizationControllerPresentationContextProvidingProtocol {

                override fun authorizationController(
                    controller: ASAuthorizationController,
                    didCompleteWithAuthorization: ASAuthorization
                ) {
                    val appleCredential = didCompleteWithAuthorization.credential
                        as? ASAuthorizationAppleIDCredential
                        ?: run {
                            continuation.resumeWithException(Exception("Invalid Apple credential"))
                            return
                        }

                    val idTokenData = appleCredential.identityToken
                        ?: run {
                            continuation.resumeWithException(Exception("No identity token"))
                            return
                        }

                    val idToken = NSString.create(
                        data = idTokenData,
                        encoding = NSUTF8StringEncoding
                    )?.toString()
                        ?: run {
                            continuation.resumeWithException(Exception("Failed to decode identity token"))
                            return
                        }

                    val credential = OAuthProvider.credential(
                        providerId = "apple.com",
                        idToken = idToken,
                        rawNonce = rawNonce
                    )

                    currentDelegate = null
                    continuation.resume(credential)
                }

                override fun authorizationController(
                    controller: ASAuthorizationController,
                    didCompleteWithError: NSError
                ) {
                    currentDelegate = null
                    val code = didCompleteWithError.code
                    if (code == 1001L) { // ASAuthorizationError.canceled
                        continuation.resumeWithException(Exception("Sign-in cancelled by user"))
                    } else {
                        continuation.resumeWithException(
                            Exception("Apple Sign-In failed: ${didCompleteWithError.localizedDescription}")
                        )
                    }
                }

                override fun presentationAnchorForAuthorizationController(
                    controller: ASAuthorizationController
                ): UIWindow {
                    return getKeyWindow()
                        ?: throw Exception("No key window available")
                }
            }

            // Retain delegate to prevent GC (ASAuthorizationController.delegate is weak)
            currentDelegate = delegate

            continuation.invokeOnCancellation { currentDelegate = null }

            val authController = ASAuthorizationController(
                authorizationRequests = listOf(request)
            )
            authController.delegate = delegate
            authController.presentationContextProvider = delegate
            authController.performRequests()
        }
    }

    private fun getKeyWindow(): UIWindow? {
        return UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull()
            ?.windows
            ?.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun generateRandomNonce(length: Int = 32): String {
        val bytes = ByteArray(length)
        bytes.usePinned { pinned ->
            val status = SecRandomCopyBytes(kSecRandomDefault, length.toULong(), pinned.addressOf(0))
            require(status == errSecSuccess) { "Failed to generate random nonce" }
        }
        return bytes.joinToString("") { it.toInt().and(0xFF).toString(16).padStart(2, '0') }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun sha256(input: String): String {
        val data = input.encodeToByteArray()
        val hash = ByteArray(CC_SHA256_DIGEST_LENGTH)
        data.usePinned { inputPinned ->
            hash.usePinned { hashPinned ->
                CC_SHA256(inputPinned.addressOf(0), data.size.toUInt(), hashPinned.addressOf(0).reinterpret())
            }
        }
        return hash.joinToString("") { it.toInt().and(0xFF).toString(16).padStart(2, '0') }
    }
}