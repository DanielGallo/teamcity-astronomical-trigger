package jetbrains.buildServer.buildTriggers.astronomical.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.buildTriggers.astronomical.AstronomicalTriggerManager
import jetbrains.buildServer.buildTriggers.astronomical.AstronomicalTriggerProperty
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.controllers.BasePropertiesBean
import jetbrains.buildServer.parameters.ParametersUtil
import jetbrains.buildServer.serverSide.BuildTypeNotFoundException
import jetbrains.buildServer.serverSide.ControlDescription
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.parameters.WellknownParameterArguments
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.stereotype.Controller
import org.springframework.web.servlet.ModelAndView
import java.nio.file.Paths
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
internal class AstronomicalTriggerPropertiesController(
    myWebControllerManager: WebControllerManager,
    private val myPluginDescriptor: PluginDescriptor,
    private val myProjectManager: ProjectManager,
    private val myAstronomicalTriggersManager: AstronomicalTriggerManager
) : BaseController() {

    init {
        myWebControllerManager.registerController("astronomicalTrigger.html", this)
    }

    override fun doHandle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView? {
        val mv = ModelAndView(myPluginDescriptor.getPluginResourcesPath("editAstronomicalBuildTrigger.jsp"))

        return mv
    }
}
