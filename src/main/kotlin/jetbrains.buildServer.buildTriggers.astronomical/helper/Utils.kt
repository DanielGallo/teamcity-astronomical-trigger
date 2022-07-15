package jetbrains.buildServer.buildTriggers.astronomical.helper

import jetbrains.buildServer.buildTriggers.astronomical.data.AstronomicalEventQuery
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

object Utils {
    /**
     * Generates an MD5 hash from an astronomical event object.
     * This is used as an identifier when looking up cached trigger times on the server, to
     * avoid constantly calling the external Sunrise-Sunset API.
     * @param query The astronomical event object containing the latitude, longitude, event key, and offset.
     * @return The MD5 hash value.
     */
    fun generateHash(query: AstronomicalEventQuery): String {
        val str = "${query.latitude}_${query.longitude}_${query.event}_${query.offset}"
        val bytes = MessageDigest.getInstance("MD5").digest(str.toByteArray())

        return DatatypeConverter.printHexBinary(bytes).uppercase()
    }
}