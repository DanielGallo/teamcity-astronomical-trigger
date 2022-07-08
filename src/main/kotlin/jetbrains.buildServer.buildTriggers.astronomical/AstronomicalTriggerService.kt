package jetbrains.buildServer.buildTriggers.astronomical

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor
import jetbrains.buildServer.buildTriggers.BuildTriggerService
import jetbrains.buildServer.buildTriggers.async.AsyncPolledBuildTriggerFactory
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.identifiers.BuildTypeIdentifiersManager
import jetbrains.buildServer.util.TimeService
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.springframework.stereotype.Service
import kotlinx.datetime.*

@Service
class AstronomicalTriggerService(
    factory: AsyncPolledBuildTriggerFactory,
    private val myPluginDescriptor: PluginDescriptor,
    private val myProjectManager: ProjectManager,
    private val myBuildTypeManager: BuildTypeIdentifiersManager
) : BuildTriggerService() {

    private val myPolicy = factory.createBuildTrigger(
        AstronomicalTriggerPolicy(),
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
        val properties = buildTriggerDescriptor.properties
        val eventKey = properties[AstronomicalTriggerUtil.EVENT_PARAM]
        val latitude = properties[AstronomicalTriggerUtil.LATITUDE_PARAM]
        val longitude = properties[AstronomicalTriggerUtil.LONGITUDE_PARAM]
        var offset = properties[AstronomicalTriggerUtil.OFFSET_PARAM]?.toInt() ?: 0

        //val className = this.javaClass.kotlin.qualifiedName
        //val customDataStorage = myProjectManager.rootProject.getCustomDataStorage(className + "_" + buildTriggerDescriptor.id)
        //val nextTriggerTime: LocalDateTime? = customDataStorage.getValue("nextTriggerTime")?.toLocalDateTime()

        var triggerTimeDescription = ""

        val events = mapOf(
            "sunrise" to "sunrise",
            "sunset" to "sunset",
            "solar_noon" to "solar noon",
            "civil_twilight_begin" to "the start of civil twilight",
            "civil_twilight_end" to "the end of civil twilight",
            "nautical_twilight_begin" to "the start of nautical twilight",
            "nautical_twilight_end" to "the end of nautical twilight",
            "astronomical_twilight_begin" to "the start of astronomical twilight",
            "astronomical_twilight_end" to "the end of astronomical twilight"
        );
        var eventName = events[eventKey];

        // Include the offset in the plugin description
        if (offset != 0) {
            val suffix = if (offset < 0) "before" else "after";

            // Remove the minus sign from the start of the negative number
            if (offset < 0) {
                offset *= -1
            }

            eventName = "$offset minutes $suffix $eventName";
        } else {
            eventName = "at $eventName"
        }

        eventName = "Daily, $eventName"

        //if (nextTriggerTime != null) {
        //    val formattedDate = nextTriggerTime.toString()
        //    triggerTimeDescription = "Next trigger time: $formattedDate"
        //}

        val description = """
            Event: $eventName
            Location: $latitude, $longitude
            $triggerTimeDescription
        """.trimIndent()

        return description;
    }

    override fun getEditParametersUrl(): String {
        return myPluginDescriptor.getPluginResourcesPath("editAstronomicalBuildTrigger.jsp")
    }

    override fun getBuildTriggeringPolicy() = myPolicy

    override fun isMultipleTriggersPerBuildTypeAllowed() = true
}