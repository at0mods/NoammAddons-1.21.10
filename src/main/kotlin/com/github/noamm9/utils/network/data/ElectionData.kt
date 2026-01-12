package com.github.noamm9.utils.network.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElectionData(
    @SerialName("mayor")
    val mayor: Mayor,

    @SerialName("minister")
    val minister: Minister? = null
) {
    @Serializable
    data class Mayor(
        @SerialName("name")
        val name: String,

        @SerialName("perks")
        val perks: List<Perk> = emptyList()
    )

    @Serializable
    data class Minister(
        @SerialName("name") val name: String,
        @SerialName("perk") val perk: Perk
    )

    @Serializable
    data class Perk(
        @SerialName("name") val name: String,
        @SerialName("description") val description: String
    )

    companion object {
        val empty = ElectionData(Mayor(""))
    }
}