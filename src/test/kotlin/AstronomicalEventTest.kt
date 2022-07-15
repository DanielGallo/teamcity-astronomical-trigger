import jetbrains.buildServer.buildTriggers.astronomical.data.AstronomicalEventQuery
import jetbrains.buildServer.buildTriggers.astronomical.helper.AstronomicalEvent
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AstronomicalEventTest {
    @Test
    fun `Generate event description from query`() {
        val query = AstronomicalEventQuery(
            1.11,
            -2.11,
            "sunrise",
            5
        )

        assertEquals("5 minutes after sunrise", AstronomicalEvent.generateEventDescription(query))
    }

    @Test
    fun `Get event name from event key`() {
        assertEquals("solar noon", AstronomicalEvent.getEventNameFromKey("solar_noon"))
    }
}