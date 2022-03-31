package jetbrains.buildServer.buildTriggers.astronomical

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.buildTriggers.PolledTriggerContext
import jetbrains.buildServer.buildTriggers.async.BaseAsyncPolledBuildTrigger
import jetbrains.buildServer.util.TimeService
import java.io.File

private const val HOST = "localhost"
private const val PORT = 8080

class AstronomicalTriggerPolicy(
    private val myTimeService: TimeService
) : BaseAsyncPolledBuildTrigger() {
    override fun triggerBuild(prev: String?, context: PolledTriggerContext): String? {
        return null
    }
}
