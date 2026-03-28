package com.linksink.data.local

import org.junit.Test

class LinkDaoTest {

    @Test
    fun `LinkDao interface compiles with required methods`() {
        val daoClass = LinkDao::class.java
        
        val updateReadStatus = daoClass.methods.find { it.name == "updateReadStatus" }
        assert(updateReadStatus != null) { "updateReadStatus method should exist" }
        
        val updateArchivedStatus = daoClass.methods.find { it.name == "updateArchivedStatus" }
        assert(updateArchivedStatus != null) { "updateArchivedStatus method should exist" }
        
        val getUnreadLinks = daoClass.methods.find { it.name == "getUnreadLinks" }
        assert(getUnreadLinks != null) { "getUnreadLinks method should exist" }
        
        val getArchivedLinks = daoClass.methods.find { it.name == "getArchivedLinks" }
        assert(getArchivedLinks != null) { "getArchivedLinks method should exist" }
        
        val getRandomUnreadLink = daoClass.methods.find { it.name == "getRandomUnreadLink" }
        assert(getRandomUnreadLink != null) { "getRandomUnreadLink method should exist" }
    }
}
