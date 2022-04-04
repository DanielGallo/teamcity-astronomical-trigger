package jetbrains.buildServer.buildTriggers.astronomical

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor
import jetbrains.buildServer.buildTriggers.BuildTriggerService
import jetbrains.buildServer.buildTriggers.async.AsyncPolledBuildTriggerFactory
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

    override fun getDefaultTriggerProperties(): Map<String, String> {
        val defaults = mutableMapOf<String, String>() // MutableMap<String, String> = HashMap()
        defaults[AstronomicalTriggerUtil.OFFSET_PARAM] = "0"
        return defaults
    }

    override fun getTriggerPropertiesProcessor() = PropertiesProcessor { properties: Map<String, String> ->
        val errors = mutableListOf<InvalidProperty>()

        val latitude = properties[AstronomicalTriggerUtil.LATITUDE_PARAM]
        if (latitude.isNullOrEmpty()) {
            errors.add(InvalidProperty(AstronomicalTriggerUtil.LATITUDE_PARAM, "Latitude is required"))
        } else {
            val latitudeNum = latitude.toFloatOrNull()

            if (latitudeNum == null) {
                errors.add(InvalidProperty(AstronomicalTriggerUtil.LATITUDE_PARAM, "Latitude is not in the correct format"))
            } else if (latitudeNum < -90 || latitudeNum > 90) {
                errors.add(InvalidProperty(AstronomicalTriggerUtil.LATITUDE_PARAM, "Latitude must be in the range of -90 to 90 degrees inclusive"))
            }
        }

        val longitude = properties[AstronomicalTriggerUtil.LONGITUDE_PARAM]
        if (longitude.isNullOrEmpty()) {
            errors.add(InvalidProperty(AstronomicalTriggerUtil.LONGITUDE_PARAM, "Longitude is required"))
        } else {
            val longitudeNum = longitude.toFloatOrNull()

            if (longitudeNum == null) {
                errors.add(InvalidProperty(AstronomicalTriggerUtil.LONGITUDE_PARAM, "Longitude is not in the correct format"))
            } else if (longitudeNum < -180 || longitudeNum > 180) {
                errors.add(InvalidProperty(AstronomicalTriggerUtil.LONGITUDE_PARAM, "Longitude must be in the range of -180 to 180 degrees inclusive"))
            }
        }

        errors
    }

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

    override fun getEditParametersUrl(): String {
        return myPluginDescriptor.getPluginResourcesPath("editAstronomicalBuildTrigger.jsp")
    }

    override fun getBuildTriggeringPolicy() = myPolicy
    override fun isMultipleTriggersPerBuildTypeAllowed() = true
}