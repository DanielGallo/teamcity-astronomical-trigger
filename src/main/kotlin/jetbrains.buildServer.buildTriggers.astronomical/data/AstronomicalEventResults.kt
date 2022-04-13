package jetbrains.buildServer.buildTriggers.astronomical.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.datetime.serializers.LocalDateTimeComponentSerializer
import kotlinx.serialization.SerialName
import java.util.*

@Serializable
data class AstronomicalEventResults(
    @SerialName("results")
    val results: AstronomicalEventValues,
    val status: String
)

@Serializable
data class AstronomicalEventValues(
    @Serializable(with = LocalDateTimeComponentSerializer::class)
    val sunrise: LocalDateTime,
    @Serializable(with = LocalDateTimeComponentSerializer::class)
    val sunset: LocalDateTime,
    @Serializable(with = LocalDateTimeComponentSerializer::class)
    val solar_noon: LocalDateTime,
    @Serializable(with = LocalDateTimeComponentSerializer::class)
    val civil_twilight_begin: LocalDateTime,
    @Serializable(with = LocalDateTimeComponentSerializer::class)
    val civil_twilight_end: LocalDateTime,
    @Serializable(with = LocalDateTimeComponentSerializer::class)
    val nautical_twilight_begin: LocalDateTime,
    @Serializable(with = LocalDateTimeComponentSerializer::class)
    val nautical_twilight_end: LocalDateTime,
    @Serializable(with = LocalDateTimeComponentSerializer::class)
    val astronomical_twilight_begin: LocalDateTime,
    @Serializable(with = LocalDateTimeComponentSerializer::class)
    val astronomical_twilight_end: LocalDateTime
)