package com.middleton.studiosnap.core.domain.exception

class NotAuthenticatedException(
    message: String = "Please sign in to continue"
) : Exception(message)
