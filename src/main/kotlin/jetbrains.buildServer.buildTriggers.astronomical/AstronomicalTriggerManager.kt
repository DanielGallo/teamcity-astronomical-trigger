package jetbrains.buildServer.buildTriggers.astronomical

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.crypt.EncryptUtil
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.springframework.stereotype.Component

private val myFeaturePrefix = AstronomicalTriggerManager::class.qualifiedName!! + "_"

@Component
class AstronomicalTriggerManager(
    myPluginDescriptor: PluginDescriptor,
    private val myProjectManager: ProjectManager
) {
    private val myLogger = Logger.getInstance(AstronomicalTriggerManager::class.qualifiedName)

    private val myPluginName = myPluginDescriptor.pluginName
    private val myTriggerPolicyUpdatedMap = mutableMapOf<String, Boolean>()

    private val myPolicyPathParam = "policyPath"
    private val myPolicyEnabledParam = "enabled"
    private val myPolicyAuthTokenParam = "authToken"

    /*fun createCustomTriggerPolicy(policyDescriptor: AstronomicalTriggerPolicyDescriptor): String {
        val (policyName, project) = policyDescriptor
        policyDescriptor.getOrCreatePolicyFeature()

        val policyPath = project.getPathOfOwnPolicy(policyName)
        policyDescriptor.updatePolicyFeature(myPolicyPathParam to policyPath)

        return policyPath
    }*/

    fun deleteCustomTriggerPolicy(policyDescriptor: AstronomicalTriggerPolicyDescriptor): String? {
        val (policyName, project) = policyDescriptor
        if (getUsages(policyDescriptor).isNotEmpty()) {
            myLogger.debug("Policy '$policyName' of project '${project.externalId}' still has usages and cannot be deleted")
            return null
        }

        val policyPath = getTriggerPolicyFilePath(policyDescriptor) ?: run {
            myLogger.debug(policyDescriptor.policyDoesNotExistMessage())
            return null
        }
        policyDescriptor.removePolicyFeature()
        return policyPath
    }

    /** @return the unique path of the trigger policy, or null if the policy does not exist */
    fun getTriggerPolicyFilePath(policyDescriptor: AstronomicalTriggerPolicyDescriptor): String? {
        val policyFeature = policyDescriptor.getPolicyFeature() ?: run {
            myLogger.debug(policyDescriptor.policyDoesNotExistMessage())
            return null
        }

        return policyFeature.parameters[myPolicyPathParam]
            ?: throw IllegalStateException("Each policy must have a path assigned")
    }

    fun getTriggerPolicyAuthToken(policyDescriptor: AstronomicalTriggerPolicyDescriptor): String? {
        val tokenFeature = policyDescriptor.getPolicyFeature() ?: return null
        val token = tokenFeature.parameters[myPolicyAuthTokenParam]
        if (token.isNullOrEmpty()) return null

        return EncryptUtil.unscramble(token)
    }

    fun setTriggerPolicyAuthToken(policyDescriptor: AstronomicalTriggerPolicyDescriptor, newToken: String) {
        val token =
            if (newToken.isBlank()) ""
            else EncryptUtil.scramble(newToken)

        policyDescriptor.updatePolicyFeature(myPolicyAuthTokenParam to token)
    }

    fun deleteTriggerPolicyAuthToken(policyDescriptor: AstronomicalTriggerPolicyDescriptor) =
        setTriggerPolicyAuthToken(policyDescriptor, "")

    fun isTriggerPolicyEnabled(policyDescriptor: AstronomicalTriggerPolicyDescriptor): Boolean {
        val policyFeature = policyDescriptor.getPolicyFeature() ?: run {
            myLogger.debug(policyDescriptor.policyDoesNotExistMessage())
            return false
        }

        return policyFeature
            .parameters[myPolicyEnabledParam]
            ?.toBoolean()
            ?: true
    }

    fun setTriggerPolicyEnabled(policyDescriptor: AstronomicalTriggerPolicyDescriptor, enabled: Boolean) {
        policyDescriptor.updatePolicyFeature(myPolicyEnabledParam to enabled.toString())
    }

    fun isTriggerPolicyUpdated(policyDescriptor: AstronomicalTriggerPolicyDescriptor): Boolean {
        val policyPath = getTriggerPolicyFilePath(policyDescriptor) ?: run {
            myLogger.debug(policyDescriptor.policyDoesNotExistMessage())
            return false
        }
        return myTriggerPolicyUpdatedMap.computeIfAbsent(policyPath) { true }
    }

    fun setTriggerPolicyUpdated(policyDescriptor: AstronomicalTriggerPolicyDescriptor, updated: Boolean) {
        val policyPath = getTriggerPolicyFilePath(policyDescriptor) ?: run {
            myLogger.debug(policyDescriptor.policyDoesNotExistMessage())
            return
        }
        myTriggerPolicyUpdatedMap[policyPath] = updated
    }

    fun localCustomTriggers(project: SProject): Collection<AstronomicalTriggerPolicyDescriptor> =
        project.ownFeatures.toPolicyDescriptors()

    fun allUsableCustomTriggers(project: SProject): Collection<AstronomicalTriggerPolicyDescriptor> =
        project.availableFeatures.toPolicyDescriptors()

    fun inheritedCustomTriggers(project: SProject): Collection<AstronomicalTriggerPolicyDescriptor> =
        project.parentProject
            ?.let { allUsableCustomTriggers(it) }
            ?: emptyList()

    fun getUsages(policyDescriptor: AstronomicalTriggerPolicyDescriptor): List<BuildTypeIdentity> {
        val (policyName, project) = policyDescriptor

        val filteredBuildTypes = project.buildTypes.asSequence()
            .filter { it.hasUsagesOf(policyName) }
            .asSequence<BuildTypeIdentity>()

        val filteredTemplates = project.buildTypeTemplates.asSequence()
            .filter { it.hasUsagesOf(policyName) }

        return (filteredBuildTypes + filteredTemplates).toList()
    }

    private fun Collection<SProjectFeatureDescriptor>.toPolicyDescriptors() =
        filter { it.isPolicyFeature }
            .mapNotNull { feature ->
                myProjectManager.findProjectById(feature.projectId)
                    ?.to(feature)
            }
            .map { (definingProject, feature) ->
                AstronomicalTriggerPolicyDescriptor(feature.policyName, definingProject)
            }

    /*private fun SProject.getPathOfOwnPolicy(policyName: String): String =
        getPluginDataDirectory(myPluginName)
            .resolve(myPolicyFileManager.createPolicyFileName(policyName))
            .absolutePath*/

    private fun AstronomicalTriggerPolicyDescriptor.policyDoesNotExistMessage() =
        "Policy '$policyName' does not exist in project '${project.externalId}' or its ancestors"
}

private val SProjectFeatureDescriptor.isPolicyFeature: Boolean get() = type.startsWith(myFeaturePrefix)
private val SProjectFeatureDescriptor.policyName: String get() = type.substring(myFeaturePrefix.length)

private fun AstronomicalTriggerPolicyDescriptor.getOrCreatePolicyFeature() =
    getPolicyFeature()
        ?: project
            .addFeature(myFeaturePrefix + policyName, emptyMap())
            .also { project.persist() }

private fun AstronomicalTriggerPolicyDescriptor.getPolicyFeature(): SProjectFeatureDescriptor? =
    project.getAvailableFeaturesOfType(myFeaturePrefix + policyName).firstOrNull()

private fun AstronomicalTriggerPolicyDescriptor.updatePolicyFeature(vararg entries: Pair<String, String>) {
    val feature = getPolicyFeature() ?: return

    val params = feature.parameters.toMutableMap()
    params.putAll(entries)
    project.updateFeature(feature.id, feature.type, params)
    project.persist()
}

private fun AstronomicalTriggerPolicyDescriptor.removePolicyFeature() {
    val feature = getPolicyFeature() ?: return
    project.removeFeature(feature.id)
    project.persist()
}

private fun BuildTypeSettings.hasUsagesOf(triggerPolicyName: String) = buildTriggersCollection.asSequence()
    .filter { it.buildTriggerService is AstronomicalTriggerService }
    .any { AstronomicalTriggerUtil.getTargetTriggerPolicyName(it.properties) == triggerPolicyName }
