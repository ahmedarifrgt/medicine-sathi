package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AuthorInfo
import com.example.data.model.FamilyMember
import com.example.data.model.Medicine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies Medicine Sathi app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Medicine Sathi", appName)
    }

    @Test
    fun `verify author information is MD ARIF`() {
        assertEquals("MD ARIF", AuthorInfo.NAME)
        assertEquals("01879524393", AuthorInfo.PHONE)
        assertEquals("ahmedarif.rgt@gmail.com", AuthorInfo.EMAIL)
        assertNotNull(AuthorInfo.ABOUT_ME)
    }

    @Test
    fun `verify medicine model initialization`() {
        val med = Medicine(
            name = "Paracetamol",
            genericName = "Acetaminophen",
            medicineType = "Tablet",
            strength = "500",
            dosage = "1",
            familyMemberId = 1L
        )
        assertEquals("Paracetamol", med.name)
        assertEquals(30, med.remainingQuantity)
    }
}
