package jetbrains.buildServer.buildTriggers.astronomical

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.buildTriggers.PolledTriggerContext
import jetbrains.buildServer.buildTriggers.async.AsyncPolledBuildTrigger
import jetbrains.buildServer.buildTriggers.async.BaseAsyncPolledBuildTrigger
import jetbrains.buildServer.util.TimeService
import java.io.File

class AstronomicalTriggerPolicy(
    private val myTimeService: TimeService,
    private val myAstronomicalTriggerManager: AstronomicalTriggerManager
) : BaseAsyncPolledBuildTrigger() {

    override fun getPollInterval(context: PolledTriggerContext): Int {
        return 60 * 10;   // TODO: Change to a once-daily interval
    }

    override fun triggerBuild(prev: String?, context: PolledTriggerContext): String? {
        val buildType = context.buildType;
        var message = "";

        if (buildType.isInQueue || buildType.runningBuilds.isNotEmpty()) {
            message = "Existing build";
        } else {
            buildType.addToQueue("test");
            message = "New build";
        }

        return message;
    }
}
