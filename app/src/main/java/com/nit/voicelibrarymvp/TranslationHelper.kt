package com.nit.voicelibrarymvp

import android.util.Log
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

object TranslationHelper {
    private const val TAG = "TranslationHelper"

    // Use a lazy approach to avoid crashing during static initialization if "ml" is not supported
    private val translatorOptions: TranslatorOptions? by lazy {
        try {
            val targetLang = TranslateLanguage.fromLanguageTag("ml")
            if (targetLang != null) {
                TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(targetLang)
                    .build()
            } else {
                Log.e(TAG, "Malayalam (ml) is not supported by this ML Kit version")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing translation options", e)
            null
        }
    }

    private val englishToMalayalamTranslator by lazy {
        translatorOptions?.let { Translation.getClient(it) }
    }

    /**
     * Translates English text to Malayalam using on-device ML Kit.
     * Falls back to original text if translation fails or language not supported.
     */
    suspend fun translateToMalayalam(text: String): String {
        if (text.isBlank()) return text
        
        // Ensure only English text is sent to translator (basic check)
        if (LanguageUtils.isMalayalam(text)) return text

        val client = englishToMalayalamTranslator
        if (client == null) {
            Log.w(TAG, "Translator client not available, returning original text")
            return text
        }

        return try {
            client.downloadModelIfNeeded().await()
            val result = client.translate(text).await()
            Log.d(TAG, "Translation success: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Translation failed for: $text", e)
            text
        }
    }
}
