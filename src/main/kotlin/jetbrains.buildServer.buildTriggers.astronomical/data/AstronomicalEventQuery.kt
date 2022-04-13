package jetbrains.buildServer.buildTriggers.astronomical.data

data class AstronomicalEventQuery(
    val latitude: Number,
    val longitude: Number,
    val event: String,
    val offset: Number
)
