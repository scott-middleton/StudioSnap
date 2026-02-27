package com.middleton.studiosnap.feature.home.domain.error

class PredictionFailedException(message: String) : Exception(message)
class PredictionTimeoutException(message: String) : Exception(message)
