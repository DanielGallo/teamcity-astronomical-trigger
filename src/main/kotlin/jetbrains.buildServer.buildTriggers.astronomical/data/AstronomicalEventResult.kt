package jetbrains.buildServer.buildTriggers.astronomical.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * A singular astronomical event result
 */
@Serializable
data class AstronomicalEventResult(
    var displayName: String? = null,
    val value: LocalDateTime
)
