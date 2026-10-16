package it.rapidosoft.semplice

import it.rapidosoft.semplice.data.Numbers
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NumbersTest {
    @Test fun sameNumberWithDifferentPrefixes() {
        assertTrue(Numbers.same("+39 333 123 4567", "3331234567"))
        assertTrue(Numbers.same("0039 333-1234567", "+393331234567"))
        assertTrue(Numbers.same("02 1234567", "+39 02 1234567"))
    }

    @Test fun differentNumbers() {
        assertFalse(Numbers.same("3331234567", "3331234568"))
        assertFalse(Numbers.same("112", "3331234112"))
        assertFalse(Numbers.same("", "3331234567"))
        assertFalse(Numbers.same(null, null))
    }

    @Test fun shortNumbersMustMatchExactly() {
        assertTrue(Numbers.same("112", "112"))
        assertFalse(Numbers.same("4112", "112"))
    }
}
