package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {

    private lateinit var viewModel: SaveReminderViewModel

    private lateinit var saveReminderDataSource: FakeDataSource

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        saveReminderDataSource = FakeDataSource(mutableListOf())
        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            saveReminderDataSource
        )
    }

    @Test
    fun check_loading() {
        mainCoroutineRule.pauseDispatcher()
        viewModel.saveReminder(
            ReminderDataItem(
                "test title",
                "test description",
                "test location",
                0.1,
                0.2
            )
        )
        Assert.assertEquals(viewModel.showLoading.getOrAwaitValue(), true)

        mainCoroutineRule.resumeDispatcher()
        Assert.assertEquals(viewModel.showLoading.getOrAwaitValue(), false)
    }

    @Test
    fun saveEmptyTitle_shouldReturnError() {
        viewModel.validateAndSaveReminder(
            ReminderDataItem(
                "",
                "test description",
                "test location",
                0.1,
                0.2
            )
        )
        Assert.assertEquals(viewModel.showSnackBarInt.getOrAwaitValue(), R.string.err_enter_title)
    }

    @Test
    fun saveEmptyLocation_shouldReturnError() {
        viewModel.validateAndSaveReminder(
            ReminderDataItem(
                "test title",
                "test Description",
                "",
                0.1,
                0.2
            )
        )
        Assert.assertEquals(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            R.string.err_select_location
        )
    }

    @After
    fun clear() {
        stopKoin()
    }
}