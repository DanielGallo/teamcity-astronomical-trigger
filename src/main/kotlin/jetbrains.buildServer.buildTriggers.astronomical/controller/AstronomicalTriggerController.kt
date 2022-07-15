package jetbrains.buildServer.buildTriggers.astronomical.controller

import jetbrains.buildServer.buildTriggers.astronomical.PluginConstants
import jetbrains.buildServer.controllers.BaseAjaxActionController
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.stereotype.Controller

@Controller
class AstronomicalTriggerController(
    myWebControllerManager: WebControllerManager
) : BaseAjaxActionController(myWebControllerManager) {

    init {
        myWebControllerManager.registerController(PluginConstants.TEST_TRIGGER_URL, this)
    }
}