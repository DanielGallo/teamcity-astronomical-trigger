package jetbrains.buildServer.buildTriggers.astronomical

import jetbrains.buildServer.buildTriggers.PolledTriggerContext
import jetbrains.buildServer.buildTriggers.astronomical.data.AstronomicalEventQuery
import jetbrains.buildServer.buildTriggers.astronomical.helper.AstronomicalEvent
import jetbrains.buildServer.buildTriggers.astronomical.helper.Utils
import jetbrains.buildServer.buildTriggers.async.BaseAsyncPolledBuildTrigger
import jetbrains.buildServer.util.TimeService
import kotlinx.datetime.*

class AstronomicalTriggerPolicy : BaseAsyncPolledBuildTrigger() {
    override fun triggerBuild(prev: String?, context: PolledTriggerContext): String? {
        val buildType = context.buildType
        val customDataStorage = AstronomicalTriggerUtil.getCustomDataStorageOfTrigger(context)
        val triggerProperties = context.triggerDescriptor.properties
        val storedTriggerTime = customDataStorage.getValue("nextTriggerTime")
        val storedParamsHash = customDataStorage.getValue("paramsHash")
        var nextTriggerTime: LocalDateTime? = null
        var message = ""

        val query = AstronomicalEventQuery(
            triggerProperties.get(AstronomicalTriggerUtil.LATITUDE_PARAM)?.toDouble() ?: 0,
            triggerProperties.get(AstronomicalTriggerUtil.LONGITUDE_PARAM)?.toDouble() ?: 0,
            triggerProperties.get(AstronomicalTriggerUtil.EVENT_PARAM).toString(),
            triggerProperties.get(AstronomicalTriggerUtil.OFFSET_PARAM)?.toInt() ?: 0
        )

        val newParamsHash = Utils.generateHash(query)

        // If the next trigger time hasn't been calculated yet, or the parameters have changed, fetch the next trigger time
        if (storedTriggerTime.isNullOrEmpty() || storedParamsHash != newParamsHash) {
            message = "Get next trigger time"

            nextTriggerTime = AstronomicalEvent.getNextTriggerTime(query)

            customDataStorage.putValue("nextTriggerTime", nextTriggerTime.toString())
            customDataStorage.putValue("paramsHash", newParamsHash)
        } else {
            nextTriggerTime = storedTriggerTime.toLocalDateTime()
        }

        if (buildType.isInQueue || buildType.runningBuilds.isNotEmpty()) {
            message = "Existing build in queue"
        } else if (triggerTimePassed(nextTriggerTime)) {
            buildType.addToQueue("Astronomical event")
            customDataStorage.putValue("nextTriggerTime", null)
            message = "Add new build to queue"
        }

        return message;
    }

    private fun triggerTimePassed(nextTriggerTime: LocalDateTime): Boolean {
        val diff = nextTriggerTime.compareTo(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))

        return diff <= 0
    }
}
