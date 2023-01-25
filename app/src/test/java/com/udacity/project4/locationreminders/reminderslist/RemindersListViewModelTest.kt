package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.hamcrest.MatcherAssert.assertThat
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private val reminder1 = ReminderDTO("Tit1", "Desc1", "location1", 1.1, 1.1)
    private val reminder2 = ReminderDTO("Tit2", "Desc2", "location2", 2.2, 2.2)
    private val reminder3 = ReminderDTO("Tit3", "Desc3", "location3", 2.2, 3.3)

    private lateinit var remindersListViewModel: RemindersListViewModel

    private lateinit var data: FakeDataSource

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() = runBlockingTest {
        stopKoin()
        data = FakeDataSource()

        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), data)
    }

    @After
    fun atTheEnd() = runBlockingTest{
        stopKoin()
        data.deleteAllReminders()
    }



    @Test
    fun invalidateShowNoData_noData_ShowNoDataIsTrue()= mainCoroutineRule.runBlockingTest{
        // Empty DB
        data.deleteAllReminders()
        // Try to load Reminders
        remindersListViewModel.loadReminders()
        // expect that our reminder list Live data size is 0 and show no data is true
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is` (0))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is` (true))
    }
    // We test retrieving the three reminders we're placing in this method.
    @Test
    fun loadReminders_addThreeReminders_LoadsThreeReminders()= mainCoroutineRule.runBlockingTest {
        //  just 3 Reminders in the DB
        data.deleteAllReminders()

        data.saveReminder(reminder1)
        data.saveReminder(reminder2)
        data.saveReminder(reminder3)
        // try to load Reminders
        remindersListViewModel.loadReminders()
        // expect to have only 3 reminders in remindersList and showNoData is false cause we have data
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is` (3))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is` (false))

    }
    // Here, we are testing checkLoading in this test.
    @Test
    fun loadReminders_deleteAndAddData_CheckLoading()= mainCoroutineRule.runBlockingTest{
        // Stop dispatcher so we may inspect initial values.
        mainCoroutineRule.pauseDispatcher()
        //  Only 1 Reminder
        data.deleteAllReminders()
        data.saveReminder(reminder1)
        // load Reminders
        remindersListViewModel.loadReminders()
        // The loading indicator is displayed, then it is hidden after we are done.
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()
        // Then loading indicator is hidden
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
    }

    // testing showing an Error
    @Test
    fun loadReminders_withoutdata_ShouldReturnError()= mainCoroutineRule.runBlockingTest{
        // give : set should return error to "true
        data.returnError(true)
        // when : we load Reminders
        remindersListViewModel.loadReminders()
        // then : We get showSnackBar in the view model giving us "not found"
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("no Reminder found"))
    }

}