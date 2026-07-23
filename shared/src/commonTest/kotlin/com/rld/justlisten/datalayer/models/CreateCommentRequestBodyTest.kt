package com.rld.justlisten.datalayer.models

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CreateCommentRequestBodyTest {
    @Test
    fun serializesUsingAudiusCreateCommentContract() {
        val request = CreateCommentRequestBody(
            message = "Great track!",
            entityId = 12_345,
            entityType = "Track",
            parentId = 98_765,
            trackTimestampS = 42,
        )

        val json = Json.parseToJsonElement(Json.encodeToString(request)).jsonObject

        assertEquals("Great track!", json.getValue("body").jsonPrimitive.content)
        assertEquals("12345", json.getValue("entityId").jsonPrimitive.content)
        assertEquals("Track", json.getValue("entityType").jsonPrimitive.content)
        assertEquals("98765", json.getValue("parentId").jsonPrimitive.content)
        assertEquals("42", json.getValue("trackTimestampS").jsonPrimitive.content)
        assertFalse("message" in json)
        assertFalse("entity_id" in json)
        assertFalse("entity_type" in json)
    }
}
