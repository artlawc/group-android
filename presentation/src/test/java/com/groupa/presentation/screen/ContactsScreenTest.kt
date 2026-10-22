package com.groupa.presentation.screen

import com.groupa.data.repository.MockGroupRepository
import com.groupa.domain.model.Contact
import com.groupa.domain.usecase.AddContactUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactsScreenTest {

    @Test
    fun bezierFocusPeaksAtTheSelectedLetterAndFadesAtTheEdge() {
        val radius = 4f

        assertEquals(1f, bezierFocusAmount(0f, radius), 0.001f)
        assertEquals(0f, bezierFocusAmount(radius, radius), 0.001f)
        assertTrue(bezierFocusAmount(1f, radius) > bezierFocusAmount(2f, radius))
    }

    @Test
    fun dragProgressSnapsToTheNearestAlphabetItem() {
        assertEquals(1, nearestAlphabetIndex(1.49f))
        assertEquals(2, nearestAlphabetIndex(1.51f))
        assertEquals(0, nearestAlphabetIndex(-3f))
        assertEquals(25, nearestAlphabetIndex(30f))
    }

    @Test
    fun lettersShrinkSmoothlyAwayFromTheFocusedLetter() {
        assertEquals(2.6f, alphabetScale(0f, radius = 2f), 0.001f)
        assertTrue(
            alphabetScale(1f, radius = 2f) > alphabetScale(2f, radius = 2f),
        )
        assertEquals(1f, alphabetScale(2f, radius = 2f), 0.001f)
        assertTrue(
            alphabetScale(0f, radius = 2f) - alphabetScale(1f, radius = 2f) > 0.5f,
        )
    }

    @Test
    fun lensEffectReturnsToTheStraightLineWhenTouchIsReleased() {
        assertEquals(1f, alphabetLensValue(1f, 2.6f, 0f), 0.001f)
        assertEquals(1.8f, alphabetLensValue(1f, 2.6f, 0.5f), 0.001f)
        assertEquals(2.6f, alphabetLensValue(1f, 2.6f, 1f), 0.001f)
    }

    @Test
    fun bezierPositionMovesItemsAlongASmoothCurve() {
        val amplitude = 20f
        val radius = 4f

        assertEquals(amplitude, alphabetBezierX(0f, radius, amplitude), 0.001f)
        assertEquals(0f, alphabetBezierX(radius, radius, amplitude), 0.001f)
        assertTrue(
            alphabetBezierX(1f, radius, amplitude) >
                alphabetBezierX(2f, radius, amplitude),
        )

        val baseY = 100f
        val focusY = 120f
        assertEquals(
            baseY + bezierFocusAmount(1f, radius) * (focusY - baseY) * 0.12f,
            alphabetBezierY(baseY, focusY, 1f, radius),
            0.001f,
        )
        assertEquals(baseY, alphabetBezierY(baseY, focusY, radius, radius), 0.001f)
    }

    @Test
    fun contactsGroupIntoAlphabetSectionsAndKeepNumbersLast() {
        val sections = buildContactSections(
            listOf(
                Contact("3", "Ben Chen", "+1 100"),
                Contact("1", "Alice Chen", "+1 100"),
                Contact("2", "Ada Brown", "+1 100"),
                Contact("4", "1st Support", "+1 100"),
            ),
        )

        assertEquals(listOf("A", "B", "#"), sections.map { it.initial })
        assertEquals(listOf("Ada Brown", "Alice Chen"), sections.first().contacts.map { it.name })
    }

    @Test
    fun searchMatchesContactNameOrPhoneNumber() {
        val contacts = listOf(
            Contact("1", "Alice Chen", "+1 (555) 010-0001"),
            Contact("2", "Bruno Silva", "+1 (555) 010-0002"),
        )

        assertEquals(listOf("Alice Chen"), filterContacts(contacts, "alice").map { it.name })
        assertEquals(listOf("Bruno Silva"), filterContacts(contacts, "0002").map { it.name })
        assertTrue(filterContacts(contacts, "no match").isEmpty())
    }

    @Test
    fun addContactInsertsAndPersistsTheNewContact() = runBlocking {
        val repository = MockGroupRepository()
        val addContact = AddContactUseCase(repository)

        addContact("Tim Friend", "+1 (555) 010-9999")

        assertTrue(repository.contacts.first().any { it.name == "Tim Friend" })
    }

    @Test
    fun addContactRejectsBlankFieldsAndDuplicates() = runBlocking {
        val repository = MockGroupRepository()
        val addContact = AddContactUseCase(repository)

        assertFalse(runCatching { addContact(" ", "+1 (555) 010-9999") }.isSuccess)
        assertFalse(runCatching { addContact("Tim Friend", " ") }.isSuccess)

        addContact("Tim Friend", "+1 (555) 010-9999")
        assertFalse(
            runCatching { addContact("tim friend", "+1 (555) 010-9999") }.isSuccess,
        )
    }
}
