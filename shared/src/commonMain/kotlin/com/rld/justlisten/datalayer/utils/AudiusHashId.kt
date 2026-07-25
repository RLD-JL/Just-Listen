package com.rld.justlisten.datalayer.utils

import kotlin.math.ceil

/** Decodes the public hash IDs used by the Audius API into entity IDs used by write endpoints. */
object AudiusHashId {
    private const val SALT = "azowernasdfoia"
    private const val SEPARATOR_DIV = 3.5
    private const val GUARD_DIV = 12.0
    private const val DEFAULT_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
    private const val DEFAULT_SEPARATORS = "cfhistuCFHISTU"

    private val salt = SALT.toList()
    private val separators: List<Char>
    private val guards: Set<Char>
    private val alphabet: List<Char>
    private val allowedCharacters: Set<Char>

    init {
        val uniqueAlphabet = DEFAULT_ALPHABET.toList().distinct()
        val separatorCharacters = DEFAULT_SEPARATORS.toList()
        var workingAlphabet = uniqueAlphabet.filterNot { it in separatorCharacters }
        var workingSeparators = shuffle(
            separatorCharacters.filter { it in uniqueAlphabet },
            salt,
        )

        if (workingSeparators.isEmpty() || workingAlphabet.size.toDouble() / workingSeparators.size > SEPARATOR_DIV) {
            val separatorCount = ceil(workingAlphabet.size / SEPARATOR_DIV).toInt()
            if (separatorCount > workingSeparators.size) {
                val difference = separatorCount - workingSeparators.size
                workingSeparators = workingSeparators + workingAlphabet.take(difference)
                workingAlphabet = workingAlphabet.drop(difference)
            }
        }

        workingAlphabet = shuffle(workingAlphabet, salt)
        val guardCount = ceil(workingAlphabet.size / GUARD_DIV).toInt()
        val workingGuards = if (workingAlphabet.size < 3) {
            val result = workingSeparators.take(guardCount)
            workingSeparators = workingSeparators.drop(guardCount)
            result
        } else {
            val result = workingAlphabet.take(guardCount)
            workingAlphabet = workingAlphabet.drop(guardCount)
            result
        }

        separators = workingSeparators
        guards = workingGuards.toSet()
        alphabet = workingAlphabet
        allowedCharacters = (alphabet + separators + guards).toSet()
    }

    fun decode(id: String): Long? {
        if (id.isEmpty() || id.any { it !in allowedCharacters }) return null

        val guardParts = splitOn(id, guards)
        val relevantPartIndex = if (guardParts.size == 2 || guardParts.size == 3) 1 else 0
        val relevantPart = guardParts.getOrNull(relevantPartIndex).orEmpty()
        if (relevantPart.isEmpty()) return null

        val lottery = relevantPart.first()
        val encodedNumbers = splitOn(relevantPart.drop(1), separators.toSet())
        if (encodedNumbers.size != 1 || encodedNumbers.first().isEmpty()) return null

        val shuffledAlphabet = shuffle(
            alphabet,
            (listOf(lottery) + salt + alphabet).take(alphabet.size),
        )
        return fromAlphabet(encodedNumbers.first(), shuffledAlphabet)
    }

    private fun shuffle(input: List<Char>, salt: List<Char>): List<Char> {
        if (salt.isEmpty()) return input
        val result = input.toMutableList()
        var saltIndex = 0
        var accumulator = 0
        for (index in result.lastIndex downTo 1) {
            saltIndex %= salt.size
            val codePoint = salt[saltIndex].code
            accumulator += codePoint
            val swapIndex = (codePoint + saltIndex + accumulator) % index
            val value = result[index]
            result[index] = result[swapIndex]
            result[swapIndex] = value
            saltIndex++
        }
        return result
    }

    private fun splitOn(value: String, delimiters: Set<Char>): List<String> {
        if (delimiters.isEmpty()) return listOf(value)
        val parts = mutableListOf<String>()
        var start = 0
        value.forEachIndexed { index, character ->
            if (character in delimiters) {
                parts += value.substring(start, index)
                start = index + 1
            }
        }
        parts += value.substring(start)
        return parts
    }

    private fun fromAlphabet(value: String, alphabet: List<Char>): Long? {
        var result = 0L
        for (character in value) {
            val index = alphabet.indexOf(character)
            if (index < 0 || result > (Long.MAX_VALUE - index) / alphabet.size) return null
            result = result * alphabet.size + index
        }
        return result
    }
}
