package jetbrains.buildServer.buildTriggers.astronomical

class TriggerContext(
    val currentTime: Long,
    val properties: Map<String, String>,
    val customData: MutableMap<String, String>,
    val buildType: BuildType
)