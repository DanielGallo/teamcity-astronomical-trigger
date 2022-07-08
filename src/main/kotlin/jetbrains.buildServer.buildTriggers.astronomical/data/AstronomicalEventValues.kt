package jetbrains.buildServer.buildTriggers.astronomical.data

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.Serializable

@Serializable
data class AstronomicalEventValues(
    @Serializable(with = InstantIso8601Serializer::class)
    val sunrise: Instant,
    @Serializable(with = InstantIso8601Serializer::class)
    val sunset: Instant,
    @Serializable(with = InstantIso8601Serializer::class)
    val solar_noon: Instant,
    @Serializable(with = InstantIso8601Serializer::class)
    val civil_twilight_begin: Instant,
    @Serializable(with = InstantIso8601Serializer::class)
    val civil_twilight_end: Instant,
    @Serializable(with = InstantIso8601Serializer::class)
    val nautical_twilight_begin: Instant,
    @Serializable(with = InstantIso8601Serializer::class)
    val nautical_twilight_end: Instant,
    @Serializable(with = InstantIso8601Serializer::class)
    val astronomical_twilight_begin: Instant,
    @Serializable(with = InstantIso8601Serializer::class)
    val astronomical_twilight_end: Instant
)