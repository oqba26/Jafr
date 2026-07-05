package com.oqba26.jafr

// import com.samanzamani.persiandate.PersianDate // Removed due to resolution issues

enum class AbjadType(val label: String) {
    KABIR("کبیر"),
    SAGHIR("صغیر"),
    WASAIT("وسایط"),
    JAFR_15("جفر ۱۵ سطری")
}

enum class NadhiraType(val label: String) {
    ABJAD("ابجدی"),
    ABTATH("ابتثی"),
    AHTAM("اهطمی")
}

data class AbjadResult(
    val total: Int,
    val breakdown: List<Pair<Char, Int>>
)

data class JafrRow(
    val title: String,
    val letters: String
)

data class Jafr15Result(
    val rows: List<JafrRow>
)

object AbjadUtils {
    private val kabirMap = mapOf(
        'ا' to 1, 'آ' to 1, 'ء' to 1,
        'ب' to 2, 'پ' to 2,
        'ج' to 3, 'چ' to 3,
        'د' to 4,
        'ه' to 5, 'ة' to 5,
        'و' to 6,
        'ز' to 7, 'ژ' to 7,
        'ح' to 8,
        'ط' to 9,
        'ی' to 10, 'ئ' to 10, 'ى' to 10,
        'ک' to 20, 'گ' to 20,
        'ل' to 30,
        'm' to 40, 'م' to 40,
        'ن' to 50,
        'س' to 60,
        'ع' to 70,
        'ف' to 80,
        'ص' to 90,
        'ق' to 100,
        'ر' to 200,
        'ش' to 300,
        'ت' to 400,
        'ث' to 500,
        'خ' to 600,
        'ذ' to 700,
        'ض' to 800,
        'ظ' to 900,
        'غ' to 1000
    )

    private val letterNames = mapOf(
        'ا' to "الف", 'آ' to "الف", 'ء' to "همزه",
        'ب' to "با", 'پ' to "با",
        'ج' to "جیم", 'چ' to "جیم",
        'د' to "دال",
        'ه' to "ها", 'ة' to "ها",
        'و' to "واو",
        'ز' to "زا", 'ژ' to "زا",
        'ح' to "حا",
        'ط' to "طا",
        'ی' to "یا", 'ئ' to "یا", 'ى' to "یا",
        'ک' to "کاف", 'گ' to "کاف",
        'ل' to "لام",
        'م' to "میم",
        'ن' to "نون",
        'س' to "سین",
        'ع' to "عین",
        'ف' to "فا",
        'ص' to "صاد",
        'ق' to "قاف",
        'ر' to "را",
        'ش' to "شین",
        'ت' to "تا",
        'ث' to "ثا",
        'خ' to "خا",
        'ذ' to "ذال",
        'ض' to "ضاد",
        'ظ' to "ظا",
        'غ' to "غین"
    )

    private const val ABJAD_SEQ = "ابجدهوزحطیکلمنسعفصقرشتثخذضظغ"
    private const val ABTATH_SEQ = "ابتثجحخدذرژسشصضطظعغفقکلمنوهی"
    private const val AHTAM_SEQ = "اهطمفشذبوینصتضجژکسقثظدحلعرخغ"

    fun calculate(text: String, type: AbjadType): AbjadResult {
        val breakdown = mutableListOf<Pair<Char, Int>>()
        var total = 0
        
        for (char in text) {
            val kabirValue = kabirMap[char]
            if (kabirValue != null) {
                val value = when (type) {
                    AbjadType.KABIR -> kabirValue
                    AbjadType.SAGHIR -> {
                        val v = kabirValue % 12
                        if (v == 0) 12 else v
                    }
                    AbjadType.WASAIT -> {
                        val v = kabirValue % 9
                        if (v == 0) 9 else v
                    }
                    AbjadType.JAFR_15 -> kabirValue
                }
                total += value
                breakdown.add(char to value)
            }
        }
        return AbjadResult(total, breakdown)
    }

    fun calculateJafr15(question: String, nadhiraType: NadhiraType = NadhiraType.ABJAD): Jafr15Result {
        val cleanText = question.filter { it in kabirMap.keys || it == ' ' }.replace(" ", "")
        val rows = mutableListOf<JafrRow>()

        // 1. Asas
        rows.add(JafrRow("اساس (حروف سوال)", cleanText.map { it.toString() }.joinToString(" ")))

        // 2. Bayyinat
        val bayyinat = cleanText.map { char ->
            val name = letterNames[char] ?: ""
            if (name.length > 1) name.substring(1) else ""
        }.joinToString("").filter { it in kabirMap.keys }
        rows.add(JafrRow("بینات (باطن حروف)", bayyinat.map { it.toString() }.joinToString(" ")))

        // 3. Nazira
        val nazira = cleanText.map { char ->
            getNaziraChar(char, nadhiraType)
        }.joinToString(" ")
        rows.add(JafrRow("نظیره (${nadhiraType.label})", nazira))

        // 4. Mustahsalah (Simplified logic)
        val mustahsalah = nazira.filter { it != ' ' }.mapIndexed { i, char ->
            if (i % 2 == 0) char else bayyinat.getOrNull(i / 2) ?: char
        }.joinToString(" ")
        // 5. Nutq (Suggestion based on image 5)
        val nutq = mustahsalah.split(" ").filter { it.isNotEmpty() }
            .map { it.first() }
            .joinToString("")
            .replace("ص", "س") // Traditional Jafr swap for nutq
        rows.add(JafrRow("نطق (گویاسازی پاسخ احتمالی)", nutq.map { it.toString() }.joinToString(" ")))

        return Jafr15Result(rows)
    }

    private fun getNaziraChar(char: Char, type: NadhiraType): Char {
        val seq = when (type) {
            NadhiraType.ABJAD -> ABJAD_SEQ
            NadhiraType.ABTATH -> ABTATH_SEQ
            NadhiraType.AHTAM -> AHTAM_SEQ
        }
        val index = seq.indexOf(char)
        if (index == -1) return char
        val half = seq.length / 2
        return seq[(index + half) % seq.length]
    }

    fun toPersianNumber(number: Any): String {
        val str = number.toString()
        val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        
        val formatted = try {
            if (str.all { it.isDigit() }) {
                val formatter = java.text.DecimalFormat("#,###")
                formatter.format(str.toLong()).replace(',', '،')
            } else {
                str
            }
        } catch (_: Exception) {
            str
        }
        
        return formatted.map { if (it.isDigit()) persianDigits[it - '0'] else it }.joinToString("")
    }
    
}
