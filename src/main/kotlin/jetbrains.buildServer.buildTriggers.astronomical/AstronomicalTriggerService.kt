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
        AstronomicalTriggerPolicy(timeService, myAstronomicalTriggerManager),
        Logger.getInstance(AstronomicalTriggerService::class.qualifiedName)
    )

    override fun getName() = "teamcityAstronomicalTrigger"
    override fun getDisplayName() = "Astronomical Trigger"

    override fun getDefaultTriggerProperties(): Map<String, String> {
        val defaults = mutableMapOf<String, String>()
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
        val properties = buildTriggerDescriptor.properties;
        val eventKey = properties["astronomical.trigger.event"];
        val latitude = properties["astronomical.trigger.latitude"];
        val longitude = properties["astronomical.trigger.longitude"];
        val offset = properties["astronomical.trigger.offset"]?.toInt() ?: 0;

        val events = mapOf(
            "sunrise" to "Sunrise",
            "sunset" to "Sunset",
            "solar_noon" to "Solar Noon",
            "civil_twilight_begin" to "Beginning of civil twilight",
            "civil_twilight_end" to "Ending of civil twilight",
            "nautical_twilight_begin" to "Beginning of nautical twilight",
            "nautical_twilight_end" to "Ending of nautical twilight",
            "astronomical_twilight_begin" to "Beginning of astronomical twilight",
            "astronomical_twilight_end" to "Ending of astronomical twilight"
        );
        var eventName = events[eventKey];

        // Include the offset in the plugin description
        if (offset != 0) {
            eventName = eventName?.lowercase()
            var offsetString = offset.toString();
            val suffix = if (offset < 0) "before" else "after";

            // Remove the minus sign from the start of the negative number
            if (offset < 0) {
                offsetString = offsetString.substring(1);
            }

            eventName = "$offsetString minutes $suffix $eventName";
        }

        val nextTriggerTime = null;

        val description = """
            Event: $eventName
            Location: $latitude, $longitude
        """.trimIndent()

        return description;
    }

    override fun getEditParametersUrl(): String {
        return myPluginDescriptor.getPluginResourcesPath("editAstronomicalBuildTrigger.jsp")
    }

    override fun getBuildTriggeringPolicy() = myPolicy
    override fun isMultipleTriggersPerBuildTypeAllowed() = true
}