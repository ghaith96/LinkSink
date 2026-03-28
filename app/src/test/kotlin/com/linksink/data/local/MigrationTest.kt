package com.linksink.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class MigrationTest {

    @Test
    fun `MIGRATION_3_4 has correct start and end versions`() {
        assertEquals(3, MIGRATION_3_4.startVersion)
        assertEquals(4, MIGRATION_3_4.endVersion)
    }

    @Test
    fun `MIGRATION_4_5 has correct start and end versions`() {
        assertEquals(4, MIGRATION_4_5.startVersion)
        assertEquals(5, MIGRATION_4_5.endVersion)
    }
}
