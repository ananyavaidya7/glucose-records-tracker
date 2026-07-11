package com.example.glucoserecordbook.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReadingInputTest {
    @Test fun `empty value needs a neutral prompt`() {
        assertEquals("Enter a reading before saving.", ReadingInput.validationMessage(""))
    }

    @Test fun `zero and accidental long input are rejected`() {
        assertEquals("Please check the number entered.", ReadingInput.validationMessage("0"))
        assertEquals("Please check the number entered.", ReadingInput.validationMessage("1000"))
    }

    @Test fun `one to three digit positive integers are accepted`() {
        assertNull(ReadingInput.validationMessage("1"))
        assertNull(ReadingInput.validationMessage("999"))
    }
}
