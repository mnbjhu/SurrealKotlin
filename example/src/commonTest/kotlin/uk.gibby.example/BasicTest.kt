package uk.gibby.example

import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class BasicTest2 {
    @Test
    fun test() = runTest {
        println("Hello, World!")
    }
}