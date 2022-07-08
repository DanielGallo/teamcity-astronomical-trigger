package jetbrains.buildServer.buildTriggers.astronomical.controller

import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.stereotype.Controller
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
internal class AstronomicalTriggerPropertiesController(
    myWebControllerManager: WebControllerManager,
    private val myPluginDescriptor: PluginDescriptor
) : BaseController() {

    init {
        myWebControllerManager.registerController("/astronomicalTrigger.html", this)
    }

    override fun doHandle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView? {
        return ModelAndView(myPluginDescriptor.getPluginResourcesPath("editAstronomicalBuildTrigger.jsp"))
    }
}
