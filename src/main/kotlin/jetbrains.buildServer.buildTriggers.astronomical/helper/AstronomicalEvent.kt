package jetbrains.buildServer.buildTriggers.astronomical.helper

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import jetbrains.buildServer.buildTriggers.astronomical.data.AstronomicalEventQuery
import jetbrains.buildServer.buildTriggers.astronomical.data.AstronomicalEventResult
import jetbrains.buildServer.buildTriggers.astronomical.data.AstronomicalEventResults
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import kotlinx.serialization.json.Json
import kotlin.reflect.KProperty1

object AstronomicalEvent {
    /**
     * Gets the next trigger time for storing locally.
     * This is typically called once per day, when the previous trigger time has passed (or there's no stored trigger time).
     */
    fun getNextTriggerTime(query: AstronomicalEventQuery): LocalDateTime? {
        val events = fetchUpcomingTriggerTimes(query)
        var nextTriggerTime: LocalDateTime? = null

        for (event in events) {
            val diff = event.value.compareTo(Clock.System.now().toLocalDateTime(TimeZone.UTC))

            if (diff > 0) {
                nextTriggerTime = event.value
                break
            }
        }

        return nextTriggerTime
    }

    /**
     * Gets a list of upcoming trigger times (typically one or two trigger times).
     * This list is displayed when clicking on the "Calculate next trigger time" button in the UI.
     */
    fun getUpcomingTriggerTimes(query: AstronomicalEventQuery): List<AstronomicalEventResult> {
        val events = fetchUpcomingTriggerTimes(query)
        val triggerTimes = mutableListOf<AstronomicalEventResult>()

        for (event in events) {
            val diff = event.value.compareTo(Clock.System.now().toLocalDateTime(TimeZone.UTC))

            if (diff > 0) {
                triggerTimes.add(event)
            }
        }

        return triggerTimes
    }

    fun getEventNameFromKey(eventKey: String): String {
        val events = mapOf(
            "sunrise" to "sunrise",
            "sunset" to "sunset",
            "solar_noon" to "solar noon",
            "civil_twilight_begin" to "the start of civil twilight",
            "civil_twilight_end" to "the end of civil twilight",
            "nautical_twilight_begin" to "the start of nautical twilight",
            "nautical_twilight_end" to "the end of nautical twilight",
            "astronomical_twilight_begin" to "the start of astronomical twilight",
            "astronomical_twilight_end" to "the end of astronomical twilight"
        );

        return events[eventKey].toString();
    }

    private fun fetchUpcomingTriggerTimes(query: AstronomicalEventQuery): List<AstronomicalEventResult> {
        val eventResults: List<AstronomicalEventResults> = runBlocking {
            getTriggerTimes(query)
        }

        val eventTimes = mutableListOf<AstronomicalEventResult>()

        for (item in eventResults) {
            val eventTime = getDateTimeValue(item.results, query.event, query.offset)
            eventTimes.add(AstronomicalEventResult(item.displayName, eventTime))
        }

        return eventTimes
    }

    private fun getDateTimeValue(obj: Any, propertyName: String, offset: Number): LocalDateTime {
        val property = obj::class.members.first {
            it.name == propertyName
        } as KProperty1<Any, *>

        // Get the date/time value for the specified property
        var value = property.get(obj) as Instant

        // Add or subtract the offset (in minutes)
        value = value.plus(offset.toInt(), DateTimeUnit.MINUTE, TimeZone.UTC)

        return value.toLocalDateTime(TimeZone.UTC)
    }

    private suspend fun getTriggerTimes(
        eventInstance: AstronomicalEventQuery
    ): List<AstronomicalEventResults> {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        val today: AstronomicalEventResults = client.get("https://api.sunrise-sunset.org/json?lat=${eventInstance.latitude}&lng=${eventInstance.longitude}&date=today&formatted=0").body()
        today.displayName = "Today"

        val tomorrow: AstronomicalEventResults = client.get("https://api.sunrise-sunset.org/json?lat=${eventInstance.latitude}&lng=${eventInstance.longitude}&date=tomorrow&formatted=0").body()
        tomorrow.displayName = "Tomorrow"

        return listOf(today, tomorrow)
    }
}