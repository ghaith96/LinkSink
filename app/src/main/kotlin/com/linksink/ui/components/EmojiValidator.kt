package com.linksink.ui.components

import java.text.BreakIterator

/**
 * Returns true if [input] is null, empty, or a single emoji (including
 * ZWJ sequences like 👨‍👩‍👧, flag pairs like 🇺🇸, and variation-selector
 * forms like ❤️).
 *
 * Strategy: use a simple state machine over code points.
 * - Requires the first code point to be an emoji.
 * - Variation selectors and skin-tone modifiers may follow any emoji.
 * - A second regional-indicator letter may follow the first (flag pair).
 * - A ZWJ unlocks one following emoji component.
 * - Anything else terminates the sequence; extra content is invalid.
 */
fun isValidEmoji(input: String?): Boolean {
    if (input.isNullOrEmpty()) return true

    val codePoints = input.codePoints().toArray()
    if (codePoints.isEmpty()) return false
    if (!isEmojiCodePoint(codePoints[0])) return false
    if (codePoints.size == 1) return true

    var i = 1
    val isFlag = codePoints[0] in 0x1F1E0..0x1F1FF

    while (i < codePoints.size) {
        val cp = codePoints[i]
        when {
            // Variation selector or skin-tone: always allowed after emoji
            cp in 0xFE00..0xFE0F || cp in 0x1F3FB..0x1F3FF -> i++
            // Second regional-indicator letter (flag completion)
            isFlag && i == 1 && cp in 0x1F1E0..0x1F1FF -> i++
            // ZWJ: must be followed by an emoji component
            cp == 0x200D -> {
                i++
                if (i >= codePoints.size) return false
                if (!isEmojiCodePoint(codePoints[i]) && codePoints[i] != 0x2764 && codePoints[i] != 0x1F48B) return false
                i++
            }
            else -> return false
        }
    }
    return true
}

private fun isEmojiCodePoint(cp: Int): Boolean =
    cp in 0x1F600..0x1F64F   // Emoticons
    || cp in 0x1F300..0x1F5FF // Misc symbols & pictographs
    || cp in 0x1F680..0x1F6FF // Transport & map
    || cp in 0x1F700..0x1F77F // Alchemical symbols
    || cp in 0x1F780..0x1F7FF // Geometric shapes extended
    || cp in 0x1F800..0x1F8FF // Supplemental arrows
    || cp in 0x1F900..0x1F9FF // Supplemental symbols & pictographs
    || cp in 0x1FA00..0x1FA6F // Chess symbols
    || cp in 0x1FA70..0x1FAFF // Symbols & pictographs extended-A
    || cp in 0x2600..0x26FF   // Misc symbols (☀ ❤ etc)
    || cp in 0x2700..0x27BF   // Dingbats
    || cp in 0x1F1E0..0x1F1FF // Regional indicator symbols (flags)
    || cp in 0x1F466..0x1F469 // Person components (boy, girl, man, woman)
    || cp in 0x1F9D1..0x1F9D1 // Person
