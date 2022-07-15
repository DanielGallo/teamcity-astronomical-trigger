package jetbrains.buildServer.buildTriggers.astronomical

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor
import jetbrains.buildServer.buildTriggers.BuildTriggerService
import jetbrains.buildServer.buildTriggers.astronomical.data.AstronomicalEventQuery
import jetbrains.buildServer.buildTriggers.astronomical.helper.AstronomicalEvent
import jetbrains.buildServer.buildTriggers.astronomical.helper.Utils
import jetbrains.buildServer.buildTriggers.async.AsyncPolledBuildTriggerFactory
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.springframework.stereotype.Service
import kotlinx.datetime.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class AstronomicalTriggerService(
    factory: AsyncPolledBuildTriggerFactory,
    private val myPluginDescriptor: PluginDescriptor
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

        val offset = properties[AstronomicalTriggerUtil.OFFSET_PARAM]
        if (offset.isNullOrEmpty()) {
            errors.add(InvalidProperty(AstronomicalTriggerUtil.OFFSET_PARAM, "Offset is required"))
        } else {
            val offsetNum = offset.toIntOrNull()

            if (offsetNum == null) {
                errors.add(InvalidProperty(AstronomicalTriggerUtil.OFFSET_PARAM, "Offset needs to be either a positive or negative whole number"))
            }
        }

        errors
    }

    override fun describeTrigger(buildTriggerDescriptor: BuildTriggerDescriptor): String {
        val properties = buildTriggerDescriptor.properties
        val eventKey = properties[AstronomicalTriggerUtil.EVENT_PARAM].toString()
        val latitude = properties[AstronomicalTriggerUtil.LATITUDE_PARAM]?.toDouble() ?: 0
        val longitude = properties[AstronomicalTriggerUtil.LONGITUDE_PARAM]?.toDouble() ?: 0
        val offset = properties[AstronomicalTriggerUtil.OFFSET_PARAM]?.toInt() ?: 0
        var triggerTimeDescription = ""

        val query = AstronomicalEventQuery(
            latitude,
            longitude,
            eventKey,
            offset
        )

        val paramsHash = Utils.generateHash(query)
        val nextTriggerTime: LocalDateTime? = AstronomicalEvent.triggerTimes[paramsHash]

        var eventName = AstronomicalEvent.generateEventDescription(query)
        eventName = "Daily, $eventName"

        // If there's a cached trigger time, show it in the trigger description
        if (nextTriggerTime != null) {
            // The date/time returned from the Sunrise-Sunset API is in UTC format
            // Use a formatter to show the full date and time as both a UTC and local server time
            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' HH:mm:ss z")
            val dateTimeUTC = nextTriggerTime.toJavaLocalDateTime().atZone(ZoneId.of("UTC"))
            val dateTimeLocal = dateTimeUTC.withZoneSameInstant(ZoneId.systemDefault())
            val formattedDateTimeUTC = formatter.format(dateTimeUTC)
            val formattedDateTimeLocal = formatter.format(dateTimeLocal)

            triggerTimeDescription = "$formattedDateTimeUTC ($formattedDateTimeLocal)"
        } else {
            // If there's no cached trigger time yet, show a "fetching..." message
            triggerTimeDescription = "Fetching..."
        }

        val description = """
            Event: $eventName
            Location: $latitude, $longitude
            Next trigger time: $triggerTimeDescription
        """.trimIndent()

        return description
    }

    override fun getEditParametersUrl(): String {
        return myPluginDescriptor.getPluginResourcesPath("editAstronomicalBuildTrigger.jsp")
    }

    override fun getBuildTriggeringPolicy() = myPolicy

    override fun isMultipleTriggersPerBuildTypeAllowed() = true
}