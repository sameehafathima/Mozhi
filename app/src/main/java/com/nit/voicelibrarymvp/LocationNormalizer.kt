package com.nit.voicelibrarymvp

object LocationNormalizer {
    private val malayalamNumbers = mapOf(
        "ഒന്ന്" to "1",
        "ഒന്നാം" to "1",
        "രണ്ട്" to "2",
        "രണ്ടാം" to "2",
        "മൂന്ന്" to "3",
        "മൂന്നാം" to "3",
        "നാല്" to "4",
        "നാലാം" to "4",
        "അഞ്ച്" to "5",
        "അഞ്ചാം" to "5",
        "ആറ്" to "6",
        "ആറാം" to "6",
        "ഏഴ്" to "7",
        "ഏഴാം" to "7",
        "എട്ട്" to "8",
        "എട്ടാം" to "8",
        "ഒൻപത്" to "9",
        "ഒൻപതാം" to "9",
        "പത്ത്" to "10",
        "പത്താം" to "10"
    )

    fun normalize(input: String): String {
        var result = input.lowercase()

        // 1. Convert Malayalam number words to digits
        malayalamNumbers.forEach { (word, digit) ->
            result = result.replace(word, digit)
        }

        // 2. Standardize common terms
        result = result.replace("ഷെൽഫ്", "shelf")
        result = result.replace("റാക്ക്", "rack")
        result = result.replace("നമ്പർ", "") // Remove "number" word if present in Mal
        result = result.replace("number", "")

        // 3. Remove spaces between letters and numbers (e.g., "A 1 2" -> "A12")
        // Use regex to find space between a letter and a digit or vice versa
        result = result.replace(Regex("([a-zA-Z])\\s+(?=\\d)"), "$1")
        result = result.replace(Regex("(\\d)\\s+(?=[a-zA-Z])"), "$1")
        result = result.replace(Regex("(\\d)\\s+(?=\\d)"), "$1")

        // 4. Capitalize first letter of standard terms
        result = result.replace("shelf", "Shelf")
        result = result.replace("rack", "Rack")
        result = result.replace("column", "Column")

        // Final trim and cleanup
        return result.trim().replace(Regex("\\s+"), " ")
    }
}
