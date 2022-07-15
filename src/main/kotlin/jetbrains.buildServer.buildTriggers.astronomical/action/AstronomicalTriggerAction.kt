package jetbrains.buildServer.buildTriggers.astronomical.action

import jetbrains.buildServer.buildTriggers.astronomical.data.AstronomicalEventQuery
import jetbrains.buildServer.buildTriggers.astronomical.controller.AstronomicalTriggerController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import jetbrains.buildServer.buildTriggers.astronomical.helper.AstronomicalEvent
import jetbrains.buildServer.buildTriggers.astronomical.helper.Utils
import jetbrains.buildServer.web.openapi.ControllerAction
import org.jdom.Element

class AstronomicalTriggerAction(
    controller: AstronomicalTriggerController
): ControllerAction {
    init {
        controller.registerAction(this)
    }

    override fun canProcess(request: HttpServletRequest) =
        request.method.lowercase() == "post" && request.requestURI.contains("checkAstronomicalTriggerTime.html")

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

        // There may be no event results - for example, at high latitudes there may be
        // no sunrise or sunset times during summer months.
        if (eventResults.isNotEmpty()) {
            val paramsHash = Utils.generateHash(query)
            AstronomicalEvent.triggerTimes[paramsHash] = eventResults[0].value
        }

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