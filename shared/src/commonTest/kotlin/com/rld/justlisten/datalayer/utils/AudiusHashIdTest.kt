package com.rld.justlisten.datalayer.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AudiusHashIdTest {
    @Test
    fun decodesAudiusEntityIds() {
        assertEquals(1L, AudiusHashId.decode("7eP5n"))
        assertEquals(42L, AudiusHashId.decode("XDBQL"))
        assertEquals(12_345L, AudiusHashId.decode("eP9OR"))
        assertEquals(98_765L, AudiusHashId.decode("4jxm9"))
        assertEquals(123_456_789L, AudiusHashId.decode("PapJEk"))
    }

    @Test
    fun rejectsInvalidEntityIds() {
        assertNull(AudiusHashId.decode(""))
        assertNull(AudiusHashId.decode("not valid!"))
    }
}
