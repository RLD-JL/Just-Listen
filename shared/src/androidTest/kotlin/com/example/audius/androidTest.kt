package com.example.justlisten

import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidGreetingTest {

    @Test
    fun testExample() {
        assertTrue("Check Android is mentioned", com.example.justlisten.Greeting().greeting().contains("Android"))
    }
}