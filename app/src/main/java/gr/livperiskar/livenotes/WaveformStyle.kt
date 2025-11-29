package gr.livperiskar.livenotes

enum class WaveformStyle(val prefsValue: String) {
    BARS("bars");

    companion object {
        fun fromPrefs(value: String?): WaveformStyle {
            return values().firstOrNull { it.prefsValue == value } ?: BARS
        }
    }
}
