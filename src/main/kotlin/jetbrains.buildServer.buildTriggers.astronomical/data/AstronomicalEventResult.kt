package jetbrains.buildServer.buildTriggers.astronomical.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class AstronomicalEventResult(
    var displayName: String? = null,
    val value: LocalDateTime
)
