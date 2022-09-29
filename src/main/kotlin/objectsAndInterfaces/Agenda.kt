package objectsAndInterfaces

import java.time.Instant
import java.time.ZoneId

object Agenda {
    fun dayToday():String {
        val today = Instant.now().atZone(ZoneId.of("UTC")).dayOfWeek.toString()
        return today.substring(0,3).lowercase().replaceFirstChar { it.uppercase() }
    }
    fun dateToday(): String {
        return Instant.now().toString().substringBefore("T")
    }
    fun timestamp(): String {
        return Instant.now().toString().substringBefore(".")
    }
    fun timestampForFileName(): String {
        return Instant.now().toString().substringBefore(".").replace(":","")
    }

    fun timeNow(): String {
        return timestamp().substringAfter("T").substringBeforeLast(":")
    }
}