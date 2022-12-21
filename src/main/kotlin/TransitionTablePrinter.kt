/**
 * utlity class to print transition table of an automaton
 */
object TransitionTablePrinter {
    private const val HORIZONTAL_BORDER_KNOT = "+"
    private const val HORIZONTAL_BORDER_PATTERN = "-"
    private const val VERTICAL_BORDER_PATTERN = "|"

    /**
     * Pretty-prints transition table of an automaton (2D array)
     */
    fun Array<Array<String>>.print() = takeIf { isNotEmpty() }?.let {
        val numberOfColumns = maxOfOrNull(Array<String>::size) ?: 0
        val maxColumnWidth = flatten().maxOfOrNull(String::length) ?: 0
        val horizontalBorder = createHorizontalBorder(numberOfColumns, maxColumnWidth)
        println(horizontalBorder)
        forEach { row ->
            println(row.asString(maxColumnWidth))
            println(horizontalBorder)
        }
    } ?: Unit

    /**
     * Converts row to pretty-printed string
     */
    private fun Array<String>.asString(width: Int) = VERTICAL_BORDER_PATTERN.plus(joinToString("") {
        padCell(it, width)
    })

    /**
     * Creates horizontal border for a row
     */
    private fun createHorizontalBorder(numberOfColumns: Int, width: Int) =
        HORIZONTAL_BORDER_KNOT + HORIZONTAL_BORDER_PATTERN
            .repeat(width)
            .plus(HORIZONTAL_BORDER_KNOT)
            .repeat(numberOfColumns)

    /**
     * Pads cell left to particular length
     */
    private fun padCell(text: String, length: Int) = text.padStart(length).plus(VERTICAL_BORDER_PATTERN)
}
