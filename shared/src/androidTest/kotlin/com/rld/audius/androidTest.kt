package com.rld.justlisten

import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidGreetingTest {

    @Test
    fun testExample() {
        assertTrue("Check Android is mentioned", com.rld.justlisten.Greeting().greeting().contains("Android"))
    }
}