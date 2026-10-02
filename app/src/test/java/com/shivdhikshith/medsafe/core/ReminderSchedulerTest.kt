package com.shivdhikshith.medsafe.core

import com.shivdhikshith.medsafe.core.notifications.AndroidReminderScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ReminderSchedulerTest {

    @Test
    fun `computeNextTriggerTime schedules for tomorrow if time already passed today`() {
        val now = Calendar.getInstance()
        // Create a time 1 hour in the past today
        val pastCalendar = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, -1)
        }

        val nextTrigger = AndroidReminderScheduler.computeNextTriggerTime(
            pastCalendar.timeInMillis,
            "DAILY"
        )

        assertTrue(nextTrigger > now.timeInMillis)
        val nextCal = Calendar.getInstance().apply { timeInMillis = nextTrigger }
        assertEquals(pastCalendar.get(Calendar.HOUR_OF_DAY), nextCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(pastCalendar.get(Calendar.MINUTE), nextCal.get(Calendar.MINUTE))
    }

    @Test
    fun `computeNextTriggerTime skips weekend when recurrence is WEEKDAYS`() {
        // Find next Friday afternoon
        val fridayCalendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
        }

        val nextTrigger = AndroidReminderScheduler.computeNextTriggerTime(
            fridayCalendar.timeInMillis,
            "WEEKDAYS"
        )

        val nextCal = Calendar.getInstance().apply { timeInMillis = nextTrigger }
        val dayOfWeek = nextCal.get(Calendar.DAY_OF_WEEK)
        // Must not be Saturday (7) or Sunday (1)
        assertTrue(dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY)
    }
}
