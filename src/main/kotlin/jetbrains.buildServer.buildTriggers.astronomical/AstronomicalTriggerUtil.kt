package jetbrains.buildServer.buildTriggers.astronomical

import jetbrains.buildServer.buildTriggers.PolledTriggerContext
import jetbrains.buildServer.serverSide.CustomDataStorage

internal object AstronomicalTriggerUtil {
    const val LATITUDE_PARAM = "astronomical.trigger.latitude"
    const val LONGITUDE_PARAM = "astronomical.trigger.longitude"
    const val EVENT_PARAM = "astronomical.trigger.event"
    const val OFFSET_PARAM = "astronomical.trigger.offset"

    fun getCustomDataStorageOfTrigger(context: PolledTriggerContext): CustomDataStorage {
        val triggerServiceId = context.triggerDescriptor.buildTriggerService::class.qualifiedName
        val triggerId = context.triggerDescriptor.id

        return context.buildType.getCustomDataStorage(triggerServiceId + "_" + triggerId)
    }
}