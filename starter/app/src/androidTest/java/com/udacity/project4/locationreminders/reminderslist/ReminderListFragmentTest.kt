package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {


    private lateinit var repository: FakeAndroidDataSource

    @Before
    fun init() {
        stopKoin()
        val appContext: Application = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as FakeAndroidDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as FakeAndroidDataSource
                )
            }

            single { FakeAndroidDataSource() }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        repository = get() as FakeAndroidDataSource
    }

    @Test
    fun navigationToSaveFragment_andCheckToast() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )

        launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText(R.string.err_enter_title)).check(ViewAssertions.matches(isDisplayed()))
    }

    @Test
    fun checkDisplayedReminderItem() = runBlockingTest {

        val reminder = ReminderDTO(
            "test title",
            "test description",
            "test location",
            0.1,
            0.2
        )
        repository.saveReminder(reminder)
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.reminderssRecyclerView)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(reminder.title)), scrollTo()
            )
        )
    }

    @Test
    fun checkSnackBar() = runBlockingTest {
        repository.deleteAllReminders()
        repository.reminders = null

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withText("no reminder found")).check(ViewAssertions.matches(isDisplayed()))
    }
}