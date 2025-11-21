package com.codebattle.server.repository

import com.codebattle.model.Task

class TaskRepository {
    private val tasks = listOf(
        Task(
            id = "1",
            title = "Sum of Two",
            description = "Write a function that takes two integers and returns their sum.",
            inputExample = "a = 1, b = 2",
            outputExample = "3",
            templateCode = """
                fun sum(a: Int, b: Int): Int {
                    // Your code here
                    return 0
                }
            """.trimIndent()
        ),
        Task(
            id = "2",
            title = "Reverse String",
            description = "Write a function that takes a string and returns it reversed.",
            inputExample = "\"hello\"",
            outputExample = "\"olleh\"",
            templateCode = """
                fun reverse(s: String): String {
                    // Your code here
                    return ""
                }
            """.trimIndent()
        ),
        Task(
            id = "3",
            title = "Factorial",
            description = "Calculate the factorial of a non-negative integer n.",
            inputExample = "n = 5",
            outputExample = "120",
            templateCode = """
                fun factorial(n: Int): Long {
                    // Your code here
                    return 1
                }
            """.trimIndent()
        ),
        Task(
            id = "4",
            title = "Check Palindrome",
            description = "Check if the given string is a palindrome (reads the same forwards and backwards).",
            inputExample = "\"madam\"",
            outputExample = "true",
            templateCode = """
                fun isPalindrome(s: String): Boolean {
                    // Your code here
                    return false
                }
            """.trimIndent()
        ),
        Task(
            id = "5",
            title = "Find Max",
            description = "Find the maximum number in a list of integers.",
            inputExample = "[1, 5, 3, 9, 2]",
            outputExample = "9",
            templateCode = """
                fun findMax(numbers: List<Int>): Int {
                    // Your code here
                    return 0
                }
            """.trimIndent()
        )
    )

    fun getRandomTask(): Task {
        return tasks.random()
    }
}

