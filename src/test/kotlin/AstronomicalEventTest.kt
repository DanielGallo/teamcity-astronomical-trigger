import jetbrains.buildServer.buildTriggers.astronomical.helper.AstronomicalEvent
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AstronomicalEventTest {
    @Test
    fun getNextTriggerTime() {
    }

    @Test
    fun getUpcomingTriggerTimes() {
    }

    @Test
    fun getEventNameFromKey() {
        assertEquals("sunrise", AstronomicalEvent.getEventNameFromKey("sunrise"))
        assertEquals("solar noon", AstronomicalEvent.getEventNameFromKey("solar_noon"))
    }
}