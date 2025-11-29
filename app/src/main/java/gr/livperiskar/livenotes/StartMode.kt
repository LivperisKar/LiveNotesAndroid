package gr.livperiskar.livenotes

enum class StartMode(val prefsValue: String) {
    NONE("none"),
    KEYBOARD("keyboard");

    companion object {
        fun fromPrefs(value: String?): StartMode =
            values().firstOrNull { it.prefsValue == value } ?: KEYBOARD
    }
}
