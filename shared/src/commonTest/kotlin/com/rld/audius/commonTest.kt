package com.rld.justlisten

import kotlin.test.Test
import kotlin.test.assertTrue

class CommonGreetingTest {

    @Test
    fun testExample() {
        assertTrue(com.rld.justlisten.Greeting().greeting().contains("Hello"), "Check 'Hello' is mentioned")
    }
}