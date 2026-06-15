package cz.kaboom.connectioninfo.data.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExternalIpValidationTest {

    @Test
    fun validIpv4PassesThroughUnchanged() {
        assertEquals("203.0.113.55", "203.0.113.55".requireValidExternalIp())
    }

    @Test
    fun validIpv6PassesThroughUnchanged() {
        assertEquals("2001:db8::1", "2001:db8::1".requireValidExternalIp())
    }

    @Test
    fun leadingAndTrailingWhitespaceIsTrimmed() {
        assertEquals("203.0.113.55", "  203.0.113.55  ".requireValidExternalIp())
    }

    @Test
    fun emptyStringThrows() {
        assertFailsWith<IllegalArgumentException> { "".requireValidExternalIp() }
    }

    @Test
    fun stringsUpToEightCharsThrow() {
        assertFailsWith<IllegalArgumentException> { "1.2.3.48".requireValidExternalIp() } // exactly 8
        assertFailsWith<IllegalArgumentException> { "1.2.3.4".requireValidExternalIp() }  // 7
        assertFailsWith<IllegalArgumentException> { "abc".requireValidExternalIp() }       // 3
    }

    @Test
    fun nineCharStringIsMinimumValid() {
        assertEquals("1.2.3.4.5", "1.2.3.4.5".requireValidExternalIp())
    }

    @Test
    fun whitespaceOnlyStringThrowsAfterTrim() {
        assertFailsWith<IllegalArgumentException> { "   ".requireValidExternalIp() }
    }

    @Test
    fun whitespacePaddedShortAddressThrowsAfterTrim() {
        // "1.1.1.1" is 7 chars after trim → should throw
        assertFailsWith<IllegalArgumentException> { "  1.1.1.1  ".requireValidExternalIp() }
    }
}
