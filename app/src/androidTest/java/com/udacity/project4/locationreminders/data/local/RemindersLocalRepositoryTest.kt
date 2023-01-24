package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersRepository: RemindersLocalRepository

    private lateinit var database: RemindersDatabase

    val reminder1 = ReminderDTO("Tit1", "Desc1", "location1", 1.1, 1.1)
    val reminder2 = ReminderDTO("Tit2", "Desc2", "location2", 2.2, 2.2)
    val reminder3 = ReminderDTO("Tit3", "Desc3", "location3", 2.2, 3.3)

    @Before
    fun setup() {
        // testing with an in-memory database because it won't survive stopping the process
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun deleteAndGetReminders_deleteReminders_listIsEmpty() = runBlocking {
        remindersRepository.deleteAllReminders()
        val result = remindersRepository.getReminders()

        result as Result.Success
        Assert.assertThat(result.data.isEmpty(), `is`(true))
    }

    @Test
    fun saveAndGetReminder_giveWrongId_ReturnError() = runBlocking {
        // GIVEN - save reminer
        remindersRepository.saveReminder(reminder1)

        // WHEN  - check the reminder Id by wrong one
        val result = remindersRepository.getReminder("1001")
        result as Result.Error

        // THEN - can not find reminder
        Assert.assertThat(result.message, `is`("Reminder not found!"))
    }



    @Test
    fun saveReminder_giveWrongId_retriveTheRightReminder() = runBlocking {
        // GIVEN - save reminder
         remindersRepository.saveReminder(reminder1)

        // WHEN  - get reminder
        val result = remindersRepository.getReminder(reminder1.id)
        result as Result.Success

        // THEN - reminder is the same with the one i created
        Assert.assertThat(result.data.id, `is`(reminder1.id))
        Assert.assertThat(result.data.title, `is`(reminder1.title))
        Assert.assertThat(result.data.description, `is`(reminder1.description))
        Assert.assertThat(result.data.latitude, `is`(reminder1.latitude))
        Assert.assertThat(result.data.longitude, `is`(reminder1.longitude))
    }
}