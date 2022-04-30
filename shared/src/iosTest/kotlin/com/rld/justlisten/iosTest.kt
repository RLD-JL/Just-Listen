package com.rld.justlisten

import kotlin.test.Test
import kotlin.test.assertTrue

class IosGreetingTest {

    @Test
    fun testExample() {
        assertTrue(com.rld.justlisten.Greeting().greeting().contains("iOS"), "Check iOS is mentioned")
    }
}