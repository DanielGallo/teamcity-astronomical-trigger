package jetbrains.buildServer.buildTriggers.astronomical.helper

import jetbrains.buildServer.buildTriggers.astronomical.data.AstronomicalEventQuery
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

object Utils {
    fun generateHash(query: AstronomicalEventQuery): String {
        val str = "${query.latitude}_${query.longitude}_${query.event}_${query.offset}"
        val bytes = MessageDigest.getInstance("MD5").digest(str.toByteArray())

        return DatatypeConverter.printHexBinary(bytes).uppercase()
    }
}