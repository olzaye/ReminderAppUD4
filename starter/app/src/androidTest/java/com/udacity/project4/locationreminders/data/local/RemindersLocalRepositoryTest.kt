package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var localReminderRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localReminderRepository =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        val reminder = ReminderDTO(
            "test title",
            "test description",
            "test location",
            0.1,
            0.2
        )

        localReminderRepository.saveReminder(reminder)

        val result = localReminderRepository.getReminder(reminder.id)
        Assert.assertThat(result.succeeded, `is`(true))
        val successResult = result as Result.Success
        Assert.assertThat(successResult.data.title, `is`("test title"))
        Assert.assertThat(successResult.data.description, `is`("test description"))
        Assert.assertThat(successResult.data.location, `is`("test location"))
        Assert.assertThat(successResult.data.latitude, `is`(0.1))
        Assert.assertThat(successResult.data.longitude, `is`(0.2))
    }

    @Test
    fun deleteReminder_retriveNonReminder() = runBlocking {

        localReminderRepository.deleteAllReminders()

        val result = localReminderRepository.getReminders()
        Assert.assertThat(result.succeeded, `is`(true))
        val successResult = result as Result.Success
        Assert.assertThat(successResult.data.isEmpty(), `is`(true))
    }

    @After
    fun cleanUp() {
        database.close()
    }
}