// ---- Typing waveform: μόνο Bars, πάντα κίτρινο LED ----
Spacer(modifier = Modifier.height(16.dp))

Text(
    text = "Typing waveform",
    color = titleColor,
    fontSize = 13.sp,
    fontWeight = FontWeight.Medium,
    fontFamily = FontFamily.SansSerif
)

Spacer(modifier = Modifier.height(8.dp))

Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    ThemeChip(
        label = "Bars",
        selected = true, // είναι η μόνη επιλογή
        appTheme = appTheme,
        onClick = { onWaveformStyleChange(WaveformStyle.BARS) }
    )
}
