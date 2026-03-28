package com.linksink.data

import com.linksink.model.LinkMetadata

interface MetadataFetcherPort {
    suspend fun fetch(url: String): Result<LinkMetadata>
}

