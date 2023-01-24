package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    private lateinit var remindersListViewModel: RemindersListViewModel

    private lateinit var data: FakeDataSource

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() = runBlockingTest {
        //stopKoin()
        data = FakeDataSource()
        val reminder1 = ReminderDTO("Tit1", "Desc1", "location1", 1.1, 1.1)
        val reminder2 = ReminderDTO("Tit2", "Desc2", "location2", 2.2, 2.2)
        val reminder3 = ReminderDTO("Tit3", "Desc3", "location3", 2.2, 3.3)
        data.saveReminder(reminder1)
        data.saveReminder(reminder2)
        data.saveReminder(reminder3)

        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), data)
    }

    @After
    fun atTheEnd() = runBlockingTest{
        //stopKoin()
        data.deleteAllReminders()
    }

    @Test
    fun     loadReminders_showLoading_valueIsTrues() {
        Assert.assertEquals(4,2+2)
    }

    @Test
    fun loadReminders_showLoading_valueIsTrue() {
        mainCoroutineRule.pauseDispatcher()

        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isTrue()

      //  mainCoroutineRule.resumeDispatcher()

     //   assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isFalse()

    }

}