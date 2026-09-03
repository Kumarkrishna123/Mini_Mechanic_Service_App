package com.minimechanicserviceapp.domain.utill

import com.minimechanicserviceapp.domain.model.OpenStatus
import com.minimechanicserviceapp.domain.model.WorkingHours
import java.time.DayOfWeek
import java.time.LocalDateTime

object OpenStatusResolver {

    fun resolve(hours: List<WorkingHours>, now: LocalDateTime): OpenStatus {
        if(hours.isEmpty()) return OpenStatus.Closed(opensAt = null , opensOn = null)

        val byDay = hours.associateBy { it.day }
        val today = byDay[now.dayOfWeek]

        if(today != null){
            val time = now.toLocalTime()
            if(!time.isBefore(today.opensAt) && time.isBefore(today.closesAt) ){
                return OpenStatus.Open(closesAt = today.closesAt)
            }
            if(time.isBefore(today.opensAt)){
                return OpenStatus.Closed(opensAt = today.opensAt , opensOn = today.day)
            }
        }

        val next  =  nextOpenDay(byDay,now.dayOfWeek)
        return OpenStatus.Closed(opensAt = next?.opensAt , opensOn = next?.day)


    }

}

private fun nextOpenDay(
    byDay: Map<DayOfWeek, WorkingHours>,
    from: DayOfWeek,
): WorkingHours? {
    for (offset in 1..7) {
        val candidate = from.plus(offset.toLong())
        byDay[candidate]?.let { return it }
    }
    return null
}