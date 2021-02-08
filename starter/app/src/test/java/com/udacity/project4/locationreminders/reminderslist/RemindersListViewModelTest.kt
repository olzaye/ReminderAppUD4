package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    private lateinit var viewModel: RemindersListViewModel

    private lateinit var reminderDataSource: FakeDataSource

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        reminderDataSource = FakeDataSource(
            mutableListOf(
                ReminderDTO(
                    title = "Test Title 1",
                    description = "Test description 1",
                    location = "Home",
                    latitude = 0.1,
                    longitude = 0.2
                ),
                ReminderDTO(
                    title = "Test Title 2",
                    description = "Test description 2",
                    location = "Fav Supermarkt",
                    latitude = 0.3,
                    longitude = 0.4
                )
            )
        )
        viewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)
    }

    @Test
    fun loadReminders_returnsReminders() {
        mainCoroutineRule.runBlockingTest {
            viewModel.loadReminders()
            assertTrue(viewModel.remindersList.getOrAwaitValue().isNotEmpty())
            assertEquals(viewModel.showNoData.getOrAwaitValue(), false)
        }
    }

    @Test
    fun loadReminders_shouldReturnError() {
        mainCoroutineRule.runBlockingTest {
            reminderDataSource.setShouldReturnError(true)

            viewModel.loadReminders()

            assertEquals(viewModel.showNoData.getOrAwaitValue(), true)
            assertTrue(viewModel.showSnackBar.getOrAwaitValue().isNotEmpty())
        }
    }

    @Test
    fun check_loading() {
        mainCoroutineRule.pauseDispatcher()

        viewModel.loadReminders()
        assertEquals(viewModel.showLoading.getOrAwaitValue(), true)

        mainCoroutineRule.resumeDispatcher()
        assertEquals(viewModel.showLoading.getOrAwaitValue(), false)
    }

    @After
    fun clear() {
        stopKoin()
    }
}