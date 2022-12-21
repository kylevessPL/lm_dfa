/**
 * @author Kacper Piasta, 249105
 *
 * difficulty level: 3
 */

import TransitionTablePrinter.print
import java.time.Instant
import java.util.Scanner

/**
 * Type alias for transition table row
 */
typealias Row = Map.Entry<Pair<Int, Int>, Int>

/**
 * Exception thrown if inserted coin face value is not accepted by the automaton
 *
 * (in other words: if DFA input alphabet doesn't contain inserted face value)
 */
private class FaceValueNotAcceptedException(value: Int) : Exception("Automaton doesn't accept face value of $value")

/**
 * Enumeration class representation of face values
 *
 * Used for type-safety to prevent one from inserting not accepted coin
 */
enum class Coin(val value: Int) {
    ONE(1), TWO(2), FIVE(5);

    companion object {
        /**
         * Gets enumeration object by coin value
         */
        @JvmStatic
        fun of(value: Int): Coin = when (value) {
            1 -> ONE
            2 -> TWO
            5 -> FIVE
            else -> throw FaceValueNotAcceptedException(value)
        }
    }
}

/**
 * Sealed class for automaton result
 */
sealed class AutomatonResult(val message: String) {
    /**
     * Ticket data holder, returned for accepting state
     */
    data class Ticket(private val timestamp: Instant = Instant.now()) :
        AutomatonResult("Ticket generated, timestamp: $timestamp")

    /**
     * Refund data holder, returned for rejecting state
     */
    data class Refund(private val total: Int) : AutomatonResult("Full amount refunded, total: $total")
}

/**
 * Actual implementation of car wash DFA
 */
class CarWashAutomaton {
    /**
     * Companion object containing static fields
     */
    private companion object {
        /**
         * Delta character of transition function
         */
        const val DELTA_CHARACTER = "δ"

        /**
         *  Accepted state a.k.a. standard wash price
         */
        const val acceptingState = 20

        /**
         * Transition table representation as a key-value map
         *
         * Key – pair of unique states with corresponding transition values
         *
         * Value – next state a.k.a. total value
         */
        @JvmField
        val transitionTable = mapOf(
            Pair(0, 1) to 1, Pair(0, 2) to 2, Pair(0, 5) to 5,       // q0
            Pair(1, 1) to 2, Pair(1, 2) to 3, Pair(1, 5) to 6,       // q1
            Pair(2, 1) to 3, Pair(2, 2) to 4, Pair(2, 5) to 7,       // q2
            Pair(3, 1) to 4, Pair(3, 2) to 5, Pair(3, 5) to 8,       // q3
            Pair(4, 1) to 5, Pair(4, 2) to 6, Pair(4, 5) to 9,       // q4
            Pair(5, 1) to 6, Pair(5, 2) to 7, Pair(5, 5) to 10,      // q5
            Pair(6, 1) to 7, Pair(6, 2) to 8, Pair(6, 5) to 11,      // q6
            Pair(7, 1) to 8, Pair(7, 2) to 9, Pair(7, 5) to 12,      // q7
            Pair(8, 1) to 9, Pair(8, 2) to 10, Pair(8, 5) to 13,     // q8
            Pair(9, 1) to 10, Pair(9, 2) to 11, Pair(9, 5) to 14,    // q9
            Pair(10, 1) to 11, Pair(10, 2) to 12, Pair(10, 5) to 15, // q10
            Pair(11, 1) to 12, Pair(11, 2) to 13, Pair(11, 5) to 16, // q11
            Pair(12, 1) to 13, Pair(12, 2) to 14, Pair(12, 5) to 17, // q12
            Pair(13, 1) to 14, Pair(13, 2) to 15, Pair(13, 5) to 18, // q13
            Pair(14, 1) to 15, Pair(14, 2) to 16, Pair(14, 5) to 19, // q14
            Pair(15, 1) to 16, Pair(15, 2) to 17, Pair(15, 5) to 20, // q15
            Pair(16, 1) to 17, Pair(16, 2) to 18, Pair(16, 5) to 21, // q16
            Pair(17, 1) to 18, Pair(17, 2) to 19, Pair(17, 5) to 21, // q17
            Pair(18, 1) to 19, Pair(18, 2) to 20, Pair(18, 5) to 21, // q18
            Pair(19, 1) to 20, Pair(19, 2) to 21, Pair(19, 5) to 21, // q19
            Pair(20, 1) to 20, Pair(20, 2) to 20, Pair(20, 5) to 20, // q20 - accepting state
            Pair(21, 1) to 21, Pair(21, 2) to 21, Pair(21, 5) to 21, // q21 - rejecting state
        )

        /**
         * Converts transition table to 2D array
         */
        @JvmStatic
        fun transitionTableAsMatrix(): Array<Array<String>> {
            fun List<Row>.mapRows() = (listOf(first().key.first) + map(Row::value))
                .map(Int::toString)
                .map { "q$it" }
                .toTypedArray()

            val header = (listOf(DELTA_CHARACTER) + transitionTable.keys
                .map(Pair<Int, Int>::second)
                .map(Int::toString)
                .distinct())
                .toTypedArray()
            val rows = transitionTable.entries
                .chunked(3)
                .map(List<Row>::mapRows)
                .toTypedArray()
            return arrayOf(header).plus(rows)
        }
    }

    /**
     * Representation of transition table as 2D array
     */
    val transitionTableMatrix = transitionTableAsMatrix()

    /**
     * Representation of current state a.k.a. current total value
     *
     * Contains initial q0 state
     */
    private val _states = arrayListOf(0)

    fun insert(coin: Coin): AutomatonResult? {
        /** extract coin integer face value **/
        val value = coin.value

        /** get next state from transition table **/
        val nextState = transitionTable[Pair(_states.last(), value)]!!
        /** add next state to the list **/
        _states.add(nextState)
        /** print current state information **/
        printCurrentState()
        return _states.last().let { state ->
            /** reset automaton state if finished **/
            takeIf { state >= acceptingState }?.also { finalize() }
            when {
                /** generate ticket if state meets accepted criteria **/
                state == acceptingState -> AutomatonResult.Ticket()
                /** refund full amount if state doesn't meet accepted criteria **/
                state > acceptingState -> AutomatonResult.Refund(state)
                /** otherwise continue **/
                else -> null
            }
        }
    }

    /**
     * Prints current automaton state a.k.a. current total value
     */
    private fun printCurrentState() = _states.last().let {
        println("Current automaton state: q$it, current total value: $it")
    }

    /**
     * Prints final automaton results & resets its state
     */
    private fun finalize() = with(_states) {
        println("Final automaton state: q${last()}")
        println("Total value inserted: ${last()}")
        println("State change path: ${statesToString()}")
    }

    /**
     * Joins all states to string
     */
    private fun statesToString() = _states.joinToString(separator = "→", transform = { "q$it" })
}

fun main() = with(CarWashAutomaton()) {
    println("Transition table:")
    transitionTableMatrix.print()
    Scanner(System.`in`).use { scanner ->
        /** infinite loop **/
        run loop@{
            while (true) {
                print("Insert coin: ")
                try {
                    /** get user input **/
                    if (scanner.hasNextInt()) {
                        val value = scanner.nextInt()
                        /** insert coin **/
                        insert(Coin.of(value))?.also { result ->
                            println(result.message)
                            return@loop
                        }
                    } else scanner.next()
                } catch (ex: FaceValueNotAcceptedException) {
                    /** print message if one tries to insert coin other than 1, 2 or 5 by force, automaton doesn't accept it **/
                    println(ex.message)
                }
            }
        }
    }
}
