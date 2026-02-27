package com.middleton.studiosnap.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RevenueCatVirtualCurrenciesDto(
    val items: List<VirtualCurrencyBalanceDto>
)

@Serializable
data class VirtualCurrencyBalanceDto(
    @SerialName("currency_code") val currencyCode: String,
    val balance: Int,
    val name: String,
    @SerialName("server_description") val serverDescription: String? = null
)

@Serializable
data class VirtualCurrencyTransactionRequest(
    val adjustments: Map<String, Int>,
    val metadata: Map<String, String>? = null
)

@Serializable
data class CreditDeductionResponse(
    @SerialName("currency_code") val currencyCode: String,
    val balance: Int,
    val success: Boolean
)
