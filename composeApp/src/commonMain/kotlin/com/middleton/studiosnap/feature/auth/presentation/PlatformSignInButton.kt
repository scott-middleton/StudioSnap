package com.middleton.studiosnap.feature.auth.presentation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.middleton.studiosnap.PlatformType
import com.middleton.studiosnap.core.data.auth.NativeAuthProvider
import com.middleton.studiosnap.feature.auth.domain.usecase.SignInUseCase
import com.middleton.studiosnap.getPlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.error_signin_failed
import studiosnap.composeapp.generated.resources.signin_apple
import studiosnap.composeapp.generated.resources.signin_google
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private val ButtonHeight = 56.dp
private val ButtonShape = RoundedCornerShape(28.dp)

/**
 * Self-contained sign-in button that triggers the platform-native sign-in dialog
 * (Google on Android, Apple on iOS) and handles the post-auth flow.
 *
 * Uses koinInject() for dependencies since this is a standalone UI component
 * without a ViewModel — acceptable trade-off for composable-level DI.
 */
@Composable
fun PlatformSignInButton(
    modifier: Modifier = Modifier,
    onSignInSuccess: () -> Unit = {},
    onSignInError: (String) -> Unit = {},
    onLoading: (Boolean) -> Unit = {}
) {
    val nativeAuthProvider: NativeAuthProvider = koinInject()
    val signInUseCase: SignInUseCase = koinInject()
    val scope = rememberCoroutineScope()
    val signInFailedMessage = stringResource(Res.string.error_signin_failed)

    val platformType = getPlatform().type
    val buttonText = if (platformType == PlatformType.IOS) {
        stringResource(Res.string.signin_apple)
    } else {
        stringResource(Res.string.signin_google)
    }

    NativeSignInButtonContent(
        text = buttonText,
        onClick = {
            onLoading(true)
            scope.launch {
                try {
                    val credential = nativeAuthProvider.getCredential()
                    Firebase.auth.signInWithCredential(credential)
                    signInUseCase.execute().fold(
                        onSuccess = {
                            onLoading(false)
                            onSignInSuccess()
                        },
                        onFailure = { error ->
                            onLoading(false)
                            onSignInError(error.message ?: signInFailedMessage)
                        }
                    )
                } catch (e: Exception) {
                    onLoading(false)
                    val isCancellation = e.message?.contains("cancelled", ignoreCase = true) == true
                    if (!isCancellation) {
                        onSignInError(e.message ?: signInFailedMessage)
                    }
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun NativeSignInButtonContent(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(ButtonHeight),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
