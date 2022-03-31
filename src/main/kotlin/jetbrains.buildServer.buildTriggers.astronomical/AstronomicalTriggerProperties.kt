package jetbrains.buildServer.buildTriggers.astronomical

@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class AstronomicalTriggerProperties(vararg val properties: AstronomicalTriggerProperty)

@Target(AnnotationTarget.CLASS)
@Repeatable
@MustBeDocumented
annotation class AstronomicalTriggerProperty(
    val name: String,
    val type: PropertyType,
    val description: String,
    val required: Boolean = false
)

enum class PropertyType(val typeName: String) {
    TEXT("text"), BOOLEAN("boolean")
}
