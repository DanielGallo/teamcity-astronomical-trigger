package jetbrains.buildServer.buildTriggers.astronomical.action

import jetbrains.buildServer.buildTriggers.astronomical.data.AstronomicalEventQuery
import jetbrains.buildServer.buildTriggers.astronomical.data.AstronomicalEventResults
import jetbrains.buildServer.buildTriggers.astronomical.controller.AstronomicalTriggerController
import jetbrains.buildServer.web.openapi.ControllerAction
import org.jdom.Content
import org.jdom.Element
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class AstronomicalTriggerAction(
    controller: AstronomicalTriggerController
): ControllerAction {
    init {
        controller.registerAction(this)
    }

    override fun canProcess(request: HttpServletRequest) =
        request.method.toLowerCase() == "post"

    override fun process(
        request: HttpServletRequest,
        response: HttpServletResponse,
        ajaxResponse: Element?
    ) {
        val el = Element("result")
        ajaxResponse!!.addContent(el as Content)

        el.setAttribute("success", "true")

        val query = AstronomicalEventQuery(
            latitude = request.getParameter("latitude").toDouble(),
            longitude = request.getParameter("longitude").toDouble(),
            event = request.getParameter("event"),
            offset = request.getParameter("offset").toInt()
        )

        // TODO: Maybe look for better alternative - should I avoid "runBlocking"?
        el.setAttribute("result", runBlocking {
            lookupTriggerTime(query).results.civil_twilight_begin.toString()
        })
    }

    suspend fun lookupTriggerTime(
        eventInstance: AstronomicalEventQuery
    ): AstronomicalEventResults {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        val eventValues: AstronomicalEventResults = client.get("https://api.sunrise-sunset.org/json?lat=${eventInstance.latitude}&lng=${eventInstance.longitude}&date=today&formatted=0").body()

        return eventValues
        //return response
    }
}