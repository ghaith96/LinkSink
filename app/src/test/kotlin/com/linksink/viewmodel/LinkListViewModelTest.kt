package com.linksink.viewmodel

import org.junit.Test

class LinkListViewModelTest {

    @Test
    fun `LinkListViewModel has linkFilter property`() {
        val viewModelClass = LinkListViewModel::class.java
        val field = viewModelClass.declaredFields.find { it.name.contains("linkFilter") || it.name.contains("filter") }
        assert(field != null) { "linkFilter property should exist" }
    }

    @Test
    fun `LinkListViewModel has setLinkFilter method`() {
        val viewModelClass = LinkListViewModel::class.java
        val method = viewModelClass.methods.find { it.name == "setLinkFilter" || it.name.contains("Filter") }
        assert(method != null) { "setLinkFilter method should exist" }
    }
}
