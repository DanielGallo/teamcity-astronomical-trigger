import jetbrains.buildServer.buildTriggers.astronomical.data.AstronomicalEventQuery
import jetbrains.buildServer.buildTriggers.astronomical.helper.Utils
import kotlin.test.Test
import kotlin.test.assertEquals

internal class UtilsTest {
    @Test
    fun `Generate hash value from astronomical event object`() {
        val query = AstronomicalEventQuery(
            1.111,
            2.111,
            "sunrise",
            5
        )

        assertEquals("210759E5F19CADA5F81248B38B13261E", Utils.generateHash(query))
    }
}