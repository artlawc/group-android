package com.groupa.coreui.util

import java.util.Locale

object AppLanguage {
    const val SYSTEM_DEFAULT = ""
    const val FALLBACK_LANGUAGE_TAG = "en-US"

    fun resolveStoredTag(storedLanguageTag: String, systemLanguageTag: String): String {
        val resolvedTag = storedLanguageTag.ifBlank {
            systemLanguageTag.ifBlank { FALLBACK_LANGUAGE_TAG }
        }
        return Locale.forLanguageTag(resolvedTag).toLanguageTag()
    }
}
