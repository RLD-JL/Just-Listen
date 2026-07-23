package com.rld.justlisten.datalayer.models

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ReactCommentRequestBodyTest {
    @Test
    fun serializesUsingAudiusReactionContract() {
        val request = ReactCommentRequestBody(
            entityType = "Track",
            entityId = 12_345,
        )

        val json = Json.parseToJsonElement(Json.encodeToString(request)).jsonObject

        assertEquals("Track", json.getValue("entityType").jsonPrimitive.content)
        assertEquals("12345", json.getValue("entityId").jsonPrimitive.content)
        assertFalse("entity_type" in json)
        assertFalse("entity_id" in json)
    }
}
