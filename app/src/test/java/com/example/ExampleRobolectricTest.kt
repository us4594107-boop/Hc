package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AlfaoneDatabase
import com.example.data.local.HumanCapitalRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AlfaoneDatabase
    private lateinit var repo: HumanCapitalRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AlfaoneDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = HumanCapitalRepository(db.alfaoneDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Alfaone HC", appName)
    }

    @Test
    fun `seed signIn clockIn and submitLeave flow works`() = runTest {
        val signInResult = repo.signIn("21040892", "123456")
        assertTrue(signInResult.isSuccess)
        assertTrue(signInResult.getOrThrow().isSignedIn)

        repo.clockIn("Toko / Cabang", "Toko 1042 Kemang Selatan")
        repo.startBreak()
        repo.endBreak()
        repo.clockOut("Toko 1042 Kemang Selatan")

        val logs = repo.attendanceLogs.first()
        assertTrue(logs.isNotEmpty())
        assertNotNull(logs.first().clockInMillis)
        assertNotNull(logs.first().clockOutMillis)

        val initialCount = repo.leaveRequests.first().size
        repo.submitLeaveRequest(
            nik = "21040892",
            category = "IZIN",
            title = "Izin Urusan Keluarga",
            startDateLabel = "22 Nov 2026",
            endDateLabel = "22 Nov 2026",
            durationLabel = "1 Hari",
            reason = "Keperluan mendadak",
            attachmentUri = null
        )
        assertEquals(initialCount + 1, repo.leaveRequests.first().size)
    }
}
