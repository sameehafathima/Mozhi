package com.nit.voicelibrarymvp

object LanguageDetector {
    /**
     * Checks if the string contains any Malayalam Unicode characters (U+0D00 to U+0D7F).
     */
    fun isMalayalam(text: String): Boolean {
        return text.any { it.code in 0x0D00..0x0D7F }
    }

    /**
     * Splits a mixed string into parts. 
     * For example, "Harry Potter ഒന്നാം ഷെൽഫ്" 
     * returns a Pair where the first part is likely English and the second is Malayalam.
     * This is a simple heuristic-based split.
     */
    fun splitMixedLanguage(text: String): Pair<String, String> {
        val malayalamChars = text.filter { it.code in 0x0D00..0x0D7F || it.isWhitespace() }
        val englishChars = text.filter { it.code !in 0x0D00..0x0D7F }.trim()
        
        // If the original text starts with English, return English part first
        val firstChar = text.trim().firstOrNull()
        return if (firstChar != null && firstChar.code !in 0x0D00..0x0D7F) {
            Pair(englishChars, malayalamChars.trim())
        } else {
            Pair(malayalamChars.trim(), englishChars)
        }
    }
}
