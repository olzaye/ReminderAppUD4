package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeAndroidDataSource : ReminderDataSource {

    var reminders: MutableList<ReminderDTO>? = mutableListOf()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return reminders?.let { Result.Success(it.toList()) } ?: Result.Error("no reminder found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminders?.find { it.id == id }
            ?.let { return Result.Success(it) }
        return Result.Error("no reminder found")
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}