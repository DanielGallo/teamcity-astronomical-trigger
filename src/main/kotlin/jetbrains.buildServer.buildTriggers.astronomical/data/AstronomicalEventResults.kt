package jetbrains.buildServer.buildTriggers.astronomical.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * A collection of astronomical event results
 */
@Serializable
data class AstronomicalEventResults(
    @SerialName("results")
    val results: AstronomicalEventValues,
    val status: String,
    var displayName: String? = null
)
