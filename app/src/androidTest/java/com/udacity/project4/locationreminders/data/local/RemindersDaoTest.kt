package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }


    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminder_addReminder_checkitsdata() = runBlockingTest {
        // GIVEN - insert a reminder
        val reminder = ReminderDTO("Tit1", "Desc1", "loc1", 1.1, 1.1)
        database.reminderDao().saveReminder(reminder)

        // WHEN - find it by id
        val load = database.reminderDao().getReminderById(reminder.id)

        // THEN - check the data
        assertThat<ReminderDTO>(load as ReminderDTO, notNullValue())
        assertThat(load.id, `is`(reminder.id))
        assertThat(load.title, `is`(reminder.title))
        assertThat(load.description, `is`(reminder.description))
        assertThat(load.location, `is`(reminder.location))
        assertThat(load.latitude, `is`(reminder.latitude))
        assertThat(load.longitude, `is`(reminder.longitude))
    }

    @Test
    fun insertReminders_addRemindersAndDeleteThem_checkListIsEmpty() = runBlockingTest {
        //Given - add some reminders to data base
        val reminder = ReminderDTO("Tit1", "Desc1", "loc1", 1.1, 1.1)
        val reminder2 = ReminderDTO("Tit2", "Desc2", "loc2", 2.2, 2.2)
        val reminder3 = ReminderDTO("Tit3", "Desc3", "loc3", 3.3, 3.3)

        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        //Then - Delete add reminders from database
        database.reminderDao().deleteAllReminders()

        //Then - check the list is empty
        val remindersList = database.reminderDao().getReminders()

        assertThat(remindersList, `is`(emptyList()))
    }


}