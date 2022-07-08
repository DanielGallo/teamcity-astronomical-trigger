package jetbrains.buildServer.buildTriggers.astronomical

import jetbrains.buildServer.buildTriggers.PolledTriggerContext
import jetbrains.buildServer.serverSide.CustomDataStorage
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.util.TimeService

private const val RUNNING_BUILD_AMOUNT = 20
private const val HISTORY_SIZE = 20

internal object AstronomicalTriggerUtil {
    const val LATITUDE_PARAM = "astronomical.trigger.latitude"
    const val LONGITUDE_PARAM = "astronomical.trigger.longitude"
    const val EVENT_PARAM = "astronomical.trigger.event"
    const val OFFSET_PARAM = "astronomical.trigger.offset"
    const val PROJECT_ID_PARAM = "astronomical.trigger.projectId"

    fun getCustomDataStorageOfTrigger(context: PolledTriggerContext): CustomDataStorage {
        val triggerServiceId = context.triggerDescriptor.buildTriggerService::class.qualifiedName
        val triggerId = context.triggerDescriptor.id

        return context.buildType.getCustomDataStorage(triggerServiceId + "_" + triggerId)
    }
}