package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.hamcrest.MatcherAssert.assertThat
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var saveReminder: SaveReminderViewModel

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

        saveReminder =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), data)
    }

    @After
    fun atTheEnd() = runBlockingTest{
        stopKoin()
        data.deleteAllReminders()
    }

    @Test
    fun validateEnteredData_sendEmptyTitle_checkHandle() {
        //when reminder title is empty
        val reminder = ReminderDataItem("", "desc1", "loc1", 1.1, 1.1)
        //then validEnteredData be false and the Snackbar will be please enter title
        assertThat(saveReminder.validateEnteredData(reminder)).isFalse()
        assertThat(saveReminder.showSnackBarInt.getOrAwaitValue()).isEqualTo("Please enter title")
    }
    @Test
    fun saveReminder_addReminder_showloadingIsTrueAndThenFalse()= mainCoroutineRule.runBlockingTest{
        mainCoroutineRule.pauseDispatcher()
        //when reminder added
        val reminder = ReminderDataItem("tit1", "desc1", "loc1", 1.1, 1.1)

        saveReminder.saveReminder(reminder)

        //then loading indicator is shown and then it is hidden
        assertThat(saveReminder.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()

        assertThat(saveReminder.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun validateEnteredData_sendEmptyLocation_checkHandle() {
        //when remider location is empty
        val reminder = ReminderDataItem("tit1", "desc1", "", 1.1, 1.1)
        //then validEnteredData be false and the Snackbar will be please select location
        assertThat(saveReminder.validateEnteredData(reminder)).isFalse()
        assertThat(saveReminder.showSnackBarInt.getOrAwaitValue()).isEqualTo("Please select location")
    }


}