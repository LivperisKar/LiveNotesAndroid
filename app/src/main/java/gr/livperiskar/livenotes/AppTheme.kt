package gr.livperiskar.livenotes

enum class AppTheme(val prefsValue: String) {
    LIVENOTES_DARK("livenotes_dark"),
    CHATGPT_LIGHT("chatgpt_light");

    companion object {
        fun fromPrefs(value: String?): AppTheme {
            return values().firstOrNull { it.prefsValue == value } ?: LIVENOTES_DARK
        }
    }
}
