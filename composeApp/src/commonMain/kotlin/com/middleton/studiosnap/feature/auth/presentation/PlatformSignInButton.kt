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
import com.middleton.studiosnap.feature.auth.domain.usecase.SignInUseCase
import com.middleton.studiosnap.getPlatform
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.error_network
import studiosnap.composeapp.generated.resources.error_signin_failed
import studiosnap.composeapp.generated.resources.signin_apple
import studiosnap.composeapp.generated.resources.signin_apple_unavailable
import studiosnap.composeapp.generated.resources.signin_google
import studiosnap.composeapp.generated.resources.signin_google_unavailable
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithApple
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
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
    val supabaseClient: SupabaseClient = koinInject()
    val signInUseCase: SignInUseCase = koinInject()
    val scope = rememberCoroutineScope()
    val networkErrorMessage = stringResource(Res.string.error_network)
    val signInFailedMessage = stringResource(Res.string.error_signin_failed)

    val handleResult: (NativeSignInResult) -> Unit = { result ->
        when (result) {
            is NativeSignInResult.Success -> {
                onLoading(true)
                scope.launch {
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
                }
            }
            is NativeSignInResult.Error -> onSignInError(result.message)
            is NativeSignInResult.NetworkError -> onSignInError(networkErrorMessage)
            is NativeSignInResult.ClosedByUser -> onLoading(false)
        }
    }

    val platformType = getPlatform().type

    when (platformType) {
        PlatformType.IOS -> {
            val unavailableMessage = stringResource(Res.string.signin_apple_unavailable)
            val signIn = supabaseClient.composeAuth.rememberSignInWithApple(
                onResult = handleResult,
                fallback = { handleResult(NativeSignInResult.Error(unavailableMessage)) }
            )
            NativeSignInButtonContent(
                text = stringResource(Res.string.signin_apple),
                onClick = { signIn.startFlow() },
                modifier = modifier
            )
        }
        else -> {
            val unavailableMessage = stringResource(Res.string.signin_google_unavailable)
            val signIn = supabaseClient.composeAuth.rememberSignInWithGoogle(
                onResult = handleResult,
                fallback = { handleResult(NativeSignInResult.Error(unavailableMessage)) }
            )
            NativeSignInButtonContent(
                text = stringResource(Res.string.signin_google),
                onClick = { signIn.startFlow() },
                modifier = modifier
            )
        }
    }
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
