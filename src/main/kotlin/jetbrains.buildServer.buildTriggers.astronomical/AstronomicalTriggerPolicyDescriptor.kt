package jetbrains.buildServer.buildTriggers.astronomical

import jetbrains.buildServer.serverSide.SProject

data class AstronomicalTriggerPolicyDescriptor (
    val policyName: String,
    val project: SProject
)