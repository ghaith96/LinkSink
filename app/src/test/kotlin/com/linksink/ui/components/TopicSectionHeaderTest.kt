package com.linksink.ui.components

import com.linksink.model.HookMode
import org.junit.Assert.assertEquals
import org.junit.Test

class TopicSectionHeaderTest {

    @Test
    fun `hookModeLabel for LOCAL_ONLY returns Local Only`() {
        assertEquals("Local Only", hookModeLabel(HookMode.LOCAL_ONLY))
    }

    @Test
    fun `hookModeLabel for USE_GLOBAL returns Global`() {
        assertEquals("Global", hookModeLabel(HookMode.USE_GLOBAL))
    }

    @Test
    fun `hookModeLabel for CUSTOM returns Custom`() {
        assertEquals("Custom", hookModeLabel(HookMode.CUSTOM))
    }
}
