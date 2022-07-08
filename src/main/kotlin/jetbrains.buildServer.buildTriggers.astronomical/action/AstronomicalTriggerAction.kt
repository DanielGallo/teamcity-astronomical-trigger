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
import jetbrains.buildServer.buildTriggers.astronomical.helper.AstronomicalEvent
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

        val eventResults = AstronomicalEvent.getUpcomingTriggerTimes(query)

        val el = Element("times")
        ajaxResponse?.addContent(el)

        for (item in eventResults) {
            val timeElement = Element("time")
            timeElement.setAttribute("label", item.displayName)
            timeElement.setAttribute("value", item.value.toString())

            el.addContent(timeElement)
        }
    }
}