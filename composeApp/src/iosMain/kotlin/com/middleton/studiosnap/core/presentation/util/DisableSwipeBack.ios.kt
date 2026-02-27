package com.middleton.studiosnap.core.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene

@Composable
actual fun DisableSwipeBack() {
    DisposableEffect(Unit) {
        val navigationController = findNavigationController()
        val wasEnabled = navigationController?.interactivePopGestureRecognizer?.enabled ?: true
        navigationController?.interactivePopGestureRecognizer?.enabled = false

        onDispose {
            navigationController?.interactivePopGestureRecognizer?.enabled = wasEnabled
        }
    }
}

private fun findNavigationController(): UINavigationController? {
    val rootViewController = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull()
        ?.keyWindow
        ?.rootViewController ?: return null

    return findNavigationControllerIn(rootViewController)
}

private fun findNavigationControllerIn(controller: UIViewController): UINavigationController? {
    if (controller is UINavigationController) return controller

    controller.presentedViewController?.let {
        findNavigationControllerIn(it)?.let { nav -> return nav }
    }

    return null
}
