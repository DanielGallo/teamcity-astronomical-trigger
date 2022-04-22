package jetbrains.buildServer.buildTriggers.astronomical.action

import jetbrains.buildServer.buildTriggers.astronomical.data.AstronomicalEventQuery
import jetbrains.buildServer.buildTriggers.astronomical.data.AstronomicalEventResults
import jetbrains.buildServer.buildTriggers.astronomical.controller.AstronomicalTriggerController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import jetbrains.buildServer.web.openapi.ControllerAction
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import kotlinx.serialization.json.*
import org.jdom.Element
import kotlin.reflect.KProperty1

class AstronomicalTriggerAction(
    controller: AstronomicalTriggerController
): ControllerAction {
    init {
        controller.registerAction(this)
    }

    override fun canProcess(request: HttpServletRequest) =
        request.method.lowercase() == "post"

    override fun process(
        request: HttpServletRequest,
        response: HttpServletResponse,
        ajaxResponse: Element?
    ) {
        val query = AstronomicalEventQuery(
            latitude = request.getParameter("latitude").toDouble(),
            longitude = request.getParameter("longitude").toDouble(),
            event = request.getParameter("event"),
            offset = request.getParameter("offset").toInt()
        )

        // TODO: Maybe look for better alternative - should I avoid "runBlocking"?
        val eventResults: List<AstronomicalEventResults> = runBlocking {
            lookupTriggerTime(query)
        }

        val el = Element("times")
        ajaxResponse?.addContent(el)

        for (item in eventResults) {
            val eventTime = getDateTimeValue(item.results, query.event, query.offset)

            val timeElement = Element("time")
            timeElement.setAttribute("label", item.displayName)
            timeElement.setAttribute("value", eventTime.toString())

            el.addContent(timeElement)
        }
    }

    private fun getDateTimeValue(obj: Any, propertyName: String, offset: Number): LocalDateTime {
        val property = obj::class.members.first {
            it.name == propertyName
        } as KProperty1<Any, *>

        // Get the date/time value for the specified property
        var value = property.get(obj) as Instant

        // Add or subtract the offset (in minutes)
        value = value.plus(offset.toInt(), DateTimeUnit.MINUTE, TimeZone.currentSystemDefault())

        return value.toLocalDateTime(TimeZone.currentSystemDefault())
    }

    private suspend fun lookupTriggerTime(
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