package jetbrains.buildServer.buildTriggers.astronomical

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor
import jetbrains.buildServer.buildTriggers.BuildTriggerService
import jetbrains.buildServer.buildTriggers.async.AsyncPolledBuildTriggerFactory
import jetbrains.buildServer.buildTriggers.astronomical.controller.AstronomicalTriggerPropertiesController
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.util.TimeService
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.springframework.stereotype.Service

@Service
class AstronomicalTriggerService(
    factory: AsyncPolledBuildTriggerFactory,
    timeService: TimeService,
    private val myPluginDescriptor: PluginDescriptor,
    private val myProjectManager: ProjectManager,
    private val myAstronomicalTriggerManager: AstronomicalTriggerManager
) : BuildTriggerService() {

    private val myPolicy = factory.createBuildTrigger(
        AstronomicalTriggerPolicy(timeService),
        Logger.getInstance(AstronomicalTriggerService::class.qualifiedName)
    )

    override fun getName() = "teamcityAstronomicalTrigger"
    override fun getDisplayName() = "Astronomical Trigger"

    override fun describeTrigger(buildTriggerDescriptor: BuildTriggerDescriptor): String {
        val properties = buildTriggerDescriptor.properties
        val policyName = AstronomicalTriggerUtil.getTargetTriggerPolicyName(properties)
            ?: return "Trigger policy is not selected"

        val projectId = buildTriggerDescriptor.properties["projectId"] ?: return "Project id cannot be determined"
        val project = myProjectManager.findProjectByExternalId(projectId) ?: return "Project cannot be determined"

        val policyDescriptor = AstronomicalTriggerPolicyDescriptor(policyName, project)

        val disabledStatus =
            if (myAstronomicalTriggerManager.isTriggerPolicyEnabled(policyDescriptor)) ""
            else "(disabled)"

        return "Uses $policyName $disabledStatus"
    }

    override fun getTriggerPropertiesProcessor() = PropertiesProcessor { properties: Map<String, String> ->
        val errors = mutableListOf<InvalidProperty>()

        errors
    }

    override fun getEditParametersUrl(): String {
        return "astronomicalTrigger.html"
    }
        //myPluginDescriptor.getPluginResourcesPath("editAstronomicalBuildTrigger.html")

    override fun getBuildTriggeringPolicy() = myPolicy
    override fun isMultipleTriggersPerBuildTypeAllowed() = true
}