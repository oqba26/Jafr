package com.oqba26.jafr

import saman.zamani.persiandate.PersianDate

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

data class ManzelInfo(
    val name: String,
    val meaning: String,
    val disposition: String // سعد / نحس / متوسط
)

data class PersonTale(
    val firstName: String,
    val motherName: String,
    val burj: String,
    val element: String,
    val kawkab: String,
    val day: String
)

data class SpellAnalysis(
    val indicators: List<String>,
    val score: Int,
    val level: String,       // ضعیف / متوسط / قوی
    val verdict: String,
    val khalesLetters: String,
    val factorBurj: String,
    val factorElement: String,
    val factorGender: String,
    val factorKawkab: String,
    val factorDay: String,
    val factorRelation: String
)

enum class TopicType(val label: String) {
    MARRIAGE("ازدواج"),
    TRAVEL("سفر"),
    BUSINESS("کسب‌وکار"),
    CHILDREN("فرزند"),
    ILLNESS("بیماری و درمان"),
    TRANSACTION("خرید و فروش")
}

data class TopicAnalysis(
    val topic: String,
    val indicators: List<String>,
    val score: Int,
    val level: String,       // سعد / نحس / متعادل
    val verdict: String,
    val highlights: List<Pair<String, String>>,
    val notice: String? = null
)

data class DivisionsPath(
    val label: String,       // جمع سوال / مستحصله / عدد حروف
    val total: Int,
    val direction: String,
    val day: String,
    val kawkab: String,
    val burj: String,
    val manzel: ManzelInfo,
    val polarity: String     // بله / نه / مردد
)

data class CrossChecks(
    val paths: List<DivisionsPath>,
    val consensus: String,   // بله / نه / مردد
    val confidence: String,  // همسو / نسبتاً همسو / ناسازگار
    val agreeCount: Int,
    val nadhiraPolarities: List<Pair<String, String>> // (نوع نظیره، قطبیت)
)

data class TimeReading(
    val weekday: String,
    val dayKawkab: String,
    val hourKawkab: String,
    val note: String
)

data class JafrTaqsimat(
    val total: Int,
    val direction: String,
    val day: String,
    val kawkab: String,
    val burj: String,
    val manzel: ManzelInfo,
    val mustahsalah: String,
    val saadCount: Int,
    val nahsCount: Int,
    val dominant: String,
    val verdict: String,
    val person: PersonTale? = null,
    val spell: SpellAnalysis? = null,
    val topics: List<TopicAnalysis> = emptyList(),
    val cross: CrossChecks? = null,
    val time: TimeReading? = null
)

data class Jafr15Result(
    val rows: List<JafrRow>,
    val answer: String,
    val taqsimat: JafrTaqsimat? = null
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
        'م' to 40,
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
        'ا' to "الف", 'ب' to "با", 'ج' to "جیم", 'د' to "دال", 'ه' to "ها", 'و' to "واو",
        'ز' to "زا", 'ح' to "حا", 'ط' to "طا", 'ی' to "یا", 'ک' to "کاف", 'ل' to "لام",
        'م' to "میم", 'ن' to "نون", 'س' to "سین", 'ع' to "عین", 'ف' to "فا", 'ص' to "صاد",
        'ق' to "قاف", 'ر' to "را", 'ش' to "شین", 'ت' to "تا", 'ث' to "ثا", 'خ' to "خا",
        'ذ' to "ذال", 'ض' to "ضاد", 'ظ' to "ظا", 'غ' to "غین"
    )

    private const val ABJAD_SEQ = "ابجدهوزحطیکلمنسعفصقرشتثخذضظغ"
    private const val ABTATH_SEQ = "ابتثجحخدذرژسشصضطظعغفقکلمنوهی"
    private const val AHTAM_SEQ = "اهطمفشذبوینصتضجژکسقثظدحلعرخغ"

    fun calculate(text: String, type: AbjadType): AbjadResult {
        val breakdown = mutableListOf<Pair<Char, Int>>()
        var total = 0
        for (char in normalizeText(text)) {
            val kabirValue = kabirMap[char]
            if (kabirValue != null) {
                val value = when (type) {
                    AbjadType.KABIR -> kabirValue
                    AbjadType.SAGHIR -> { val v = kabirValue % 12; if (v == 0) 12 else v }
                    AbjadType.WASAIT -> { val v = kabirValue % 9; if (v == 0) 9 else v }
                    AbjadType.JAFR_15 -> kabirValue
                }
                total += value
                breakdown.add(char to value)
            }
        }
        return AbjadResult(total, breakdown)
    }

    // ─── هنجارسازی املا (مورد ۱) ───
    // شکل‌های مختلف یک حرف را هم‌ارز می‌کند تا همان سوال با هر املاهایی یک جواب بدهد.
    private fun normalizeLetter(c: Char): Char = when (c) {
        'ي', 'ى' -> 'ی'
        'أ', 'إ', 'ٱ', 'آ' -> 'ا'
        'ك' -> 'ک'
        'ھ', 'ہ' -> 'ه'
        'ؤ' -> 'و'
        else -> c
    }

    private fun normalizeText(text: String): String = text.map(::normalizeLetter).joinToString("")

    private fun computeMustahsalah(cleanText: String, type: NadhiraType): String {
        val nazira = cleanText.map { getNaziraChar(it, type) }.joinToString("")
        return cleanText.mapIndexed { i, c ->
            val v1 = kabirMap[c] ?: 0
            val v2 = kabirMap[nazira.getOrNull(i) ?: c] ?: 0
            ABJAD_SEQ[(v1 + v2) % 28]
        }.joinToString("")
    }

    private fun countSaadNahs(mustahsalah: String): Pair<Int, Int> {
        var saad = 0
        var nahs = 0
        for (char in mustahsalah) {
            when {
                char in SAAD_LETTERS -> saad++
                char in NAHS_LETTERS -> nahs++
            }
        }
        return saad to nahs
    }

    // قطبیت هر قرینه از ترکیب منزل قمر و غالب حروف مستحصله
    private fun polarityOf(manzel: ManzelInfo, dominant: String): String = when (manzel.disposition) {
        "سعد" -> "بله"
        "نحس" -> if (dominant == "نحس") "نه" else "مردد"
        else -> "مردد"
    }

    fun calculateJafr15(
        question: String,
        nadhiraType: NadhiraType = NadhiraType.ABJAD,
        now: PersianDate? = null
    ): Jafr15Result {
        val cleanText = normalizeText(question).filter { it in kabirMap.keys }.replace(" ", "")
        if (cleanText.isEmpty()) return Jafr15Result(emptyList(), "سوال خالی است")

        // مستحصله برای هر سه نوع نظیره (برای سنجش همسویی)
        val mustahsalahByType = NadhiraType.entries.associateWith { computeMustahsalah(cleanText, it) }
        val mustahsalah = mustahsalahByType.getValue(nadhiraType)

        val rows = mutableListOf<JafrRow>()

        // 1. اساس (Asas)
        rows.add(JafrRow("سطر اول: اساس (حروف سوال)", formatLetters(cleanText)))

        // 2. نظیره (Nazira)
        val nazira = cleanText.map { getNaziraChar(it, nadhiraType) }.joinToString("")
        rows.add(JafrRow("سطر دوم: نظیره (${nadhiraType.label})", formatLetters(nazira)))

        // 3. صدر و مؤخر (تکسیر - ترکیب اول و آخر)
        val taksir = performTaksir(cleanText)
        rows.add(JafrRow("سطر سوم: تکسیر (صدر و مؤخر)", formatLetters(taksir)))

        // 4. حروف ملفوظی (باطن اساس)
        val malfuzi = cleanText.map { letterNames[it]?.substring(1) ?: "" }.joinToString("").filter { it in ABJAD_SEQ }
        rows.add(JafrRow("سطر چهارم: ملفوظی (باطن حروف)", formatLetters(malfuzi)))

        // 5. مستحصله (Mustahsalah) - منطق واقعی جفری
        rows.add(JafrRow("سطر نهایی: مستحصله (استخراج نطق)", formatLetters(mustahsalah)))

        // --- تحلیل نهایی با سیستم تقسیمات جفری ---
        val taqsimat = buildTaqsimat(question, cleanText, mustahsalah, mustahsalahByType, now)

        return Jafr15Result(rows, formatAnswer(taqsimat), taqsimat)
    }

    private fun performTaksir(text: String): String {
        val result = StringBuilder()
        var left = 0
        var right = text.length - 1
        while (left <= right) {
            result.append(text[right--])
            if (left <= right) result.append(text[left++])
        }
        return result.toString()
    }

    private fun formatLetters(text: String) = text.map { it }.joinToString("  ")

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

    // ─── سیستم تقسیمات جفری ───
    // حروف سعد و نحس: تقسیم ۲۸ حرف ابجد بر ۱۲ برج (بروج فرد سعد، بروج زوج نحس)
    private const val SAAD_LETTERS = "امذجسظهفزقطشکث"
    private const val NAHS_LETTERS = "بنضدعغوصحریتلخ"

    // منازل ۲۸گانه قمر (نام، معنی، سرنوشت)
    private val manzelTable = listOf(
        ManzelInfo("شرطان", "شروع و گشایش", "سعد"),
        ManzelInfo("بطین", "جمع شدن و فراهم شدن", "سعد"),
        ManzelInfo("ثریا", "برکت و فراوانی", "سعد"),
        ManzelInfo("دبران", "مقاومت و سنگینی", "نحس"),
        ManzelInfo("هقعه", "تغییر و جابه‌جایی", "متوسط"),
        ManzelInfo("هنعه", "کمک و یاری", "سعد"),
        ManzelInfo("ذراع", "قوت و توان", "سعد"),
        ManzelInfo("نثره", "پراکندگی و دودلی", "نحس"),
        ManzelInfo("طرف", "برخورد و تصمیم", "متوسط"),
        ManzelInfo("جبهه", "اقبال و روی‌آوردن", "سعد"),
        ManzelInfo("زبره", "ظهور و آشکار شدن", "سعد"),
        ManzelInfo("صرفه", "بازگشت و انصراف", "نحس"),
        ManzelInfo("عوا", "پایان غم", "سعد"),
        ManzelInfo("سماک", "بلندی و رفعت", "سعد"),
        ManzelInfo("غفر", "بخشش و پوشش", "سعد"),
        ManzelInfo("زبانی", "سنجش و مقایسه", "متوسط"),
        ManzelInfo("اکلیل", "تاج و پیروزی", "سعد"),
        ManzelInfo("قلب", "هیجان و تندی", "نحس"),
        ManzelInfo("شوله", "آشوب و پراکندگی", "نحس"),
        ManzelInfo("نعائم", "آسایش و نعمت", "سعد"),
        ManzelInfo("بلده", "رکود و ایستایی", "نحس"),
        ManzelInfo("سعد ذابح", "قربانی و فدا", "سعد"),
        ManzelInfo("سعد بلع", "بلعیدن و جذب", "سعد"),
        ManzelInfo("سعد سعود", "سعادت و خوشبختی", "سعد"),
        ManzelInfo("سعد اخبیه", "گنج نهان", "سعد"),
        ManzelInfo("مقدم", "پیش‌قدمی", "سعد"),
        ManzelInfo("مؤخر", "تأخیر و پایان کار", "سعد"),
        ManzelInfo("رشا", "رسیدن به آب و مقصود", "سعد")
    )

    private fun directionOf(rem: Int): String = when (rem) {
        1 -> "شرق"
        2 -> "جنوب"
        3 -> "غرب"
        else -> "شمال"
    }

    private fun dayOf(rem: Int): String = when (rem) {
        0 -> "شنبه"
        1 -> "یکشنبه"
        2 -> "دوشنبه"
        3 -> "سه‌شنبه"
        4 -> "چهارشنبه"
        5 -> "پنجشنبه"
        else -> "جمعه"
    }

    private fun burjOf(rem: Int): String = when (rem) {
        1 -> "حمل"
        2 -> "ثور"
        3 -> "جوزا"
        4 -> "سرطان"
        5 -> "اسد"
        6 -> "سنبله"
        7 -> "میزان"
        8 -> "عقرب"
        9 -> "قوس"
        10 -> "جدی"
        11 -> "دلو"
        else -> "حوت"
    }

    private fun kawkabOf(rem: Int): String = when (rem) {
        0 -> "زحل"
        1 -> "شمس"
        2 -> "قمر"
        3 -> "مریخ"
        4 -> "عطارد"
        5 -> "مشتری"
        else -> "زهره"
    }

    private fun elementOfBurjRem(rem: Int): String = when (rem) {
        1, 5, 9 -> "آتشی"
        2, 6, 10 -> "خاکی"
        3, 7, 11 -> "بادی"
        else -> "آبی"
    }

    private fun genderOfBurjRem(rem: Int): String = if (rem % 2 == 1) "مذکر" else "مونث"

    private fun buildTaqsimat(
        question: String,
        cleanText: String,
        mustahsalah: String,
        mustahsalahByType: Map<NadhiraType, String>,
        now: PersianDate?
    ): JafrTaqsimat {
        val total = calculate(cleanText, AbjadType.KABIR).total

        val direction = directionOf(total % 4)
        val day = dayOf(total % 7)
        val kawkab = kawkabOf(total % 7)
        val burj = burjOf(total % 12)
        val manzel = manzelTable[total % 28]

        // تحلیل سعد و نحس حروف مستحصله
        val (saad, nahs) = countSaadNahs(mustahsalah)
        val dominant = when {
            saad > nahs -> "سعد"
            nahs > saad -> "نحس"
            else -> "متعادل"
        }

        // ─── قرائن سه مسیر (مورد ۲ و ۵) ───
        // مسیر ۱: جمع ابجد سوال | مسیر ۲: جمع ابجد مستحصله | مسیر ۳: عدد حروف سوال
        val mustTotal = calculate(mustahsalah, AbjadType.KABIR).total
        val letterCount = cleanText.length

        val pathQuestion = DivisionsPath(
            label = "جمع سوال",
            total = total,
            direction = direction,
            day = day,
            kawkab = kawkab,
            burj = burj,
            manzel = manzel,
            polarity = polarityOf(manzel, dominant)
        )
        val pathMust = DivisionsPath(
            label = "مستحصله",
            total = mustTotal,
            direction = directionOf(mustTotal % 4),
            day = dayOf(mustTotal % 7),
            kawkab = kawkabOf(mustTotal % 7),
            burj = burjOf(mustTotal % 12),
            manzel = manzelTable[mustTotal % 28],
            polarity = polarityOf(manzelTable[mustTotal % 28], dominant)
        )
        val pathCount = DivisionsPath(
            label = "عدد حروف",
            total = letterCount,
            direction = directionOf(letterCount % 4),
            day = dayOf(letterCount % 7),
            kawkab = kawkabOf(letterCount % 7),
            burj = burjOf(letterCount % 12),
            manzel = manzelTable[letterCount % 28],
            polarity = polarityOf(manzelTable[letterCount % 28], dominant)
        )
        val paths = listOf(pathQuestion, pathMust, pathCount)

        val polarities = paths.map { it.polarity }
        val baleCount = polarities.count { it == "بله" }
        val nahCount = polarities.count { it == "نه" }
        val mordadCount = polarities.count { it == "مردد" }
        val consensus = when {
            baleCount == 3 -> "بله"
            nahCount == 3 -> "نه"
            baleCount > nahCount && baleCount >= mordadCount -> "بله"
            nahCount > baleCount && nahCount >= mordadCount -> "نه"
            else -> "مردد"
        }
        val confidence = when {
            baleCount == 3 || nahCount == 3 || mordadCount == 3 -> "همسو"
            baleCount == 2 || nahCount == 2 || mordadCount == 2 -> "نسبتاً همسو"
            else -> "ناسازگار"
        }
        val agreeCount = maxOf(baleCount, nahCount, mordadCount)

        // حکم نهایی: همسویی سه مسیر تعیین‌کننده است؛ در ناسازگاری، پاسخ مردد با ذکر دلیل
        val verdict = when {
            consensus == "بله" && pathQuestion.polarity == "بله" -> when (dominant) {
                "سعد" -> "بله — منزل سعد و حروف مستحصله غالباً سعدند. با اطمینان پیگیری کنید."
                "نحس" -> "بله ولی با احتیاط — منزل سعد است اما حروف نحس سنگینی دارند؛ صبر و دقت کنید."
                else -> "بله — منزل سعد است و حروف متعادل؛ نتیجه خوب خواهد بود."
            }
            consensus == "نه" && pathQuestion.polarity == "نه" ->
                "نه در حال حاضر — منزل نحس و حروف غالباً نحس‌اند؛ تغییر زمان یا مسیر توصیه می‌شود."
            else -> "پاسخ مردد — قرائن سه مسیر همسو نیستند (جمع سوال: ${pathQuestion.manzel.name} ${pathQuestion.manzel.disposition}، مستحصله: ${pathMust.manzel.name} ${pathMust.manzel.disposition}، عدد حروف: ${pathCount.manzel.name} ${pathCount.manzel.disposition}). برای قرائن نزدیک‌تر، سوال را با املای دقیق و نیت روشن دوباره بپرسید."
        }

        // ─── سنجش سه نظیره (مورد ۳) ───
        val nadhiraPolarities = NadhiraType.entries.map { type ->
            val m = mustahsalahByType.getValue(type)
            val (s2, n2) = countSaadNahs(m)
            val dom2 = when { s2 > n2 -> "سعد"; n2 > s2 -> "نحس"; else -> "متعادل" }
            val mz = manzelTable[calculate(m, AbjadType.KABIR).total % 28]
            type.label to polarityOf(mz, dom2)
        }

        // ─── قرینه زمان (مورد ۴) ───
        val time = now?.let { buildTimeReading(it) }

        val cross = CrossChecks(
            paths = paths,
            consensus = consensus,
            confidence = confidence,
            agreeCount = agreeCount,
            nadhiraPolarities = nadhiraPolarities
        )

        // طالع شخص از نام و نام مادر (تقسیم بر ۱۲ برای برج، بر ۷ برای کوکب)
        val names = extractNames(question)
        val person = if (names.first != null && names.second != null) {
            val personAbjad = calculate(names.first!!, AbjadType.KABIR).total +
                calculate(names.second!!, AbjadType.KABIR).total
            PersonTale(
                firstName = names.first!!,
                motherName = names.second!!,
                burj = burjOf(personAbjad % 12),
                element = elementOfBurjRem(personAbjad % 12),
                kawkab = kawkabOf(personAbjad % 7),
                day = dayOf(personAbjad % 7)
            )
        } else null

        // تحلیل طلسم / سحر در صورت وجود کلیدواژه
        val spell = buildSpellAnalysis(
            question = question,
            cleanText = cleanText,
            mustahsalah = mustahsalah,
            saad = saad,
            nahs = nahs,
            total = total,
            burj = burj,
            manzelDisposition = manzel.disposition,
            person = person,
            direction = direction
        )

        // ماژول‌های موضوعی (ازدواج، سفر، کسب‌وکار، فرزند، بیماری، خرید و فروش)
        val topics = buildTopicAnalyses(
            question = question,
            ctx = TopicContext(
                total = total,
                burjRem = total % 12,
                direction = direction,
                day = day,
                kawkab = kawkab,
                burj = burj,
                manzel = manzel,
                saad = saad,
                nahs = nahs,
                person = person
            )
        )

        return JafrTaqsimat(
            total = total,
            direction = direction,
            day = day,
            kawkab = kawkab,
            burj = burj,
            manzel = manzel,
            mustahsalah = mustahsalah,
            saadCount = saad,
            nahsCount = nahs,
            dominant = dominant,
            verdict = verdict,
            person = person,
            spell = spell,
            topics = topics,
            cross = cross,
            time = time
        )
    }

    // ─── قرینه زمان (مورد ۴) ───
    // در سنت، ساعت‌های شبانه‌روز به ترتیب زحل، مشتری، مریخ، شمس، زهره، عطارد، قمر چرخش می‌کنند
    // و ساعت اول هر روز متعلق به کوکب همان روز است.
    private val hourKawkabOrder = listOf("زحل", "مشتری", "مریخ", "شمس", "زهره", "عطارد", "قمر")

    private fun buildTimeReading(now: PersianDate): TimeReading {
        val weekday = now.dayName()
        val dayRuler = when (weekday) {
            "شنبه" -> "زحل"
            "یکشنبه" -> "شمس"
            "دوشنبه" -> "قمر"
            "سه‌شنبه" -> "مریخ"
            "چهارشنبه" -> "عطارد"
            "پنجشنبه" -> "مشتری"
            else -> "زهره"
        }
        val dayIdx = hourKawkabOrder.indexOf(dayRuler)
        val hourKawkab = hourKawkabOrder[(dayIdx + now.getHour()) % 7]

        val saadKawkab = setOf("مشتری", "زهره", "شمس", "قمر")
        val nahsKawkab = setOf("زحل", "مریخ")
        val note = when {
            dayRuler in nahsKawkab || hourKawkab in nahsKawkab ->
                "قرینه زمان از کواکب نحس است ($dayRuler/$hourKawkab)؛ در انجام کار تأمل و احتیاط کنید."
            dayRuler in saadKawkab && hourKawkab in saadKawkab ->
                "قرینه زمان از کواکب سعد است ($dayRuler/$hourKawkab)؛ موافق انجام کار."
            else ->
                "قرینه زمان متعادل است ($dayRuler/$hourKawkab)."
        }
        return TimeReading(weekday = weekday, dayKawkab = dayRuler, hourKawkab = hourKawkab, note = note)
    }

    private fun buildSpellAnalysis(
        question: String,
        cleanText: String,
        mustahsalah: String,
        saad: Int,
        nahs: Int,
        total: Int,
        burj: String,
        manzelDisposition: String,
        person: PersonTale?,
        direction: String
    ): SpellAnalysis? {
        val spellKeywords = listOf("طلسم", "سحر", "جادو", "چشم", "بست", "گره")
        if (spellKeywords.none { question.contains(it) }) return null

        val indicators = mutableListOf<String>()
        var score = 0

        // ۱. غلبه حروف نحس در مستحصله
        if (nahs > saad) {
            score += 2
            indicators.add("حروف نحس در مستحصله بر حروف سعد غلبه دارد")
        }
        // ۲. برج سوال از بروج زوج (نحس)
        if (total % 12 % 2 == 0) {
            score += 1
            indicators.add("برج سوال از بروج نحس است ($burj)")
        }
        // ۳. منزل قمر نحس
        if (manzelDisposition == "نحس") {
            score += 1
            indicators.add("منزل قمر نحس است")
        }
        // ۴. حروف بست (ب، س، ت)
        val bstCount = listOf('ب', 'س', 'ت').count { it in mustahsalah }
        if (bstCount >= 2) {
            score += 1
            indicators.add("حروف «بست» (ب، س، ت) در مستحصله حاضر است")
        }
        // ۵. حروف سحر (س، ح، ر)
        val shrCount = listOf('س', 'ح', 'ر').count { it in mustahsalah }
        if (shrCount >= 2) {
            score += 1
            indicators.add("حروف «سحر» (س، ح، ر) در مستحصله حاضر است")
        }
        // ۶. تکرار چشمگیر یک حرف نحس
        val maxNahsRepeat = mustahsalah.groupingBy { it }.eachCount()
            .filterKeys { it in NAHS_LETTERS }
            .values.maxOrNull() ?: 0
        if (maxNahsRepeat >= 3) {
            score += 1
            indicators.add("تکرار چشمگیر یک حرف نحس در مستحصله")
        }

        val level = when {
            score >= 4 -> "قوی"
            score >= 2 -> "متوسط"
            else -> "ضعیف"
        }

        val verdict = when (level) {
            "قوی" -> "بله — قرائن حروف قویاً بر وجود طلسم/سحر دلالت دارد و اثر خارجی تأیید می‌شود."
            "متوسط" -> "دلالت حروف بر وجود طلسم/سحر متوسط است؛ اثر محتمل است و بررسی بیشتر (زمان شروع، خواب‌ها، مکان‌ها) توصیه می‌شود."
            else -> "دلالت حروف بر طلسم/سحر ضعیف است؛ قرائن، اثر خارجی را تأیید نمی‌کند و بیشتر حالات روانی یا عادی است."
        }

        // استخراج حروف عامل: تخلیص (حذف حروف مکرر) سوال و استخراج شاخص‌ها از جمع ابجد آن
        val khales = buildString {
            val seen = mutableSetOf<Char>()
            for (c in cleanText) if (seen.add(c)) append(c)
        }
        val khalesAbjad = khales.sumOf { kabirMap[it] ?: 0 }
        val factorRem = khalesAbjad % 12
        val factorDayRem = khalesAbjad % 7

        val factorRelation = if (person != null) {
            if (elementOfBurjRem(factorRem) == person.element) {
                "عامل با شخص هم‌طبع است — از نزدیکان یا آشنایان"
            } else {
                "عامل با شخص غیر هم‌طبع است — از بیرون و غیر نزدیکان"
            }
        } else ""

        return SpellAnalysis(
            indicators = indicators,
            score = score,
            level = level,
            verdict = verdict,
            khalesLetters = khales,
            factorBurj = burjOf(factorRem),
            factorElement = elementOfBurjRem(factorRem),
            factorGender = genderOfBurjRem(factorRem),
            factorKawkab = kawkabOf(factorDayRem),
            factorDay = dayOf(factorDayRem),
            factorRelation = factorRelation
        )
    }

    // ─── ماژول‌های موضوعی (ازدواج، سفر، کسب‌وکار، فرزند) ───
    // دلایل سنتی هر موضوع از قرائن حروف و تقسیمات استخراج می‌شود:
    // سعد/نحس کواکب (مشتری سعد اکبر، زهره سعد اصغر، زحل نحس اکبر، مریخ نحس اصغر)،
    // دلالت بروج بر هر باب (میزان برج نکاح، سرطان منزل قمر و باروری و...)،
    // روزهای هفته متعلق به کواکب و سازگاری طبایع.

    private val marriageKeywords = listOf("ازدواج", "عروسی", "خواستگاری", "نامزدی", "وصلت", "عقد", "همسر", "زوج")
    private val travelKeywords = listOf("سفر", "مسافرت", "مهاجرت", "مسافر")
    private val businessKeywords = listOf("کسب", "تجارت", "شغل", "بیزنس", "بازار", "فروش", "خرید", "رزق", "کار")
    private val childrenKeywords = listOf("فرزند", "بچه", "نوزاد", "اولاد", "حامله", "باردار")
    private val illnessKeywords = listOf("مریض", "بیمار", "بیماری", "ناخوش", "تب", "درد", "درمان", "شفا", "سلامت", "بستری")
    private val transactionKeywords = listOf(
        "ملک", "خودرو", "ماشین", "آپارتمان", "خانه", "زمین", "معامله", "قرارداد", "قیمت",
        "بخرم", "بخرد", "بفروشم", "بفروشد", "خریداری"
    )

    private fun detectTopics(question: String): List<TopicType> = buildList {
        if (marriageKeywords.any { question.contains(it) }) add(TopicType.MARRIAGE)
        if (childrenKeywords.any { question.contains(it) }) add(TopicType.CHILDREN)
        if (travelKeywords.any { question.contains(it) }) add(TopicType.TRAVEL)
        if (businessKeywords.any { question.contains(it) }) add(TopicType.BUSINESS)
        if (illnessKeywords.any { question.contains(it) }) add(TopicType.ILLNESS)
        if (transactionKeywords.any { question.contains(it) }) add(TopicType.TRANSACTION)
    }

    private data class TopicContext(
        val total: Int,
        val burjRem: Int,
        val direction: String,
        val day: String,
        val kawkab: String,
        val burj: String,
        val manzel: ManzelInfo,
        val saad: Int,
        val nahs: Int,
        val person: PersonTale?
    )

    private fun buildTopicAnalyses(question: String, ctx: TopicContext): List<TopicAnalysis> =
        detectTopics(question).map { topic ->
            when (topic) {
                TopicType.MARRIAGE -> buildMarriageAnalysis(ctx)
                TopicType.TRAVEL -> buildTravelAnalysis(ctx)
                TopicType.BUSINESS -> buildBusinessAnalysis(ctx)
                TopicType.CHILDREN -> buildChildrenAnalysis(ctx)
                TopicType.ILLNESS -> buildIllnessAnalysis(ctx)
                TopicType.TRANSACTION -> buildTransactionAnalysis(ctx)
            }
        }

    private fun levelOfTopicScore(score: Int): String = when {
        score >= 2 -> "سعد"
        score <= -2 -> "نحس"
        else -> "متعادل"
    }

    // ─── وزن‌دهی مستند و شفاف (مورد ۶) ───
    // هر قرینه با وزن ثابت سنتی امتیاز می‌گیرد و وزن همان‌جا در متن شاخص نمایش داده می‌شود.
    // جدول وزن‌ها (خودکار، بدون نیاز به تنظیم):
    //   کوکب: مشتری +۲ (سعد اکبر) | زهره/شمس/عطارد +۱ | زحل −۲ (نحس اکبر) | مریخ −۱ (نحس اصغر)
    //   برج: برج‌های خاص هر موضوع (میزان +۲ عدل، عقرب −۲ فریب و...)
    //   منزل: سعد +۱ | نحس −۱ | بلده −۲ (رکود) | نثره −۱ (دودلی)
    //   حروف مستحصله: غالب سعد +۱ | غالب نحس −۱
    //   روز: روز کوکب سعد +۱ | روز زحل −۱ | سازگاری طبایع ±۱
    // سطح نهایی: مجموع ≥۲ → سعد | ≤−۲ → نحس | در میانه → متعادل
    private class Scorer {
        val indicators = mutableListOf<String>()
        var score = 0
        fun add(text: String, weight: Int) {
            score += weight
            val w = if (weight > 0) "+$weight" else weight.toString()
            indicators.add("$text ($w)")
        }
    }

    private fun topicVerdict(topic: TopicType, level: String): String = when (topic) {
        TopicType.MARRIAGE -> when (level) {
            "سعد" -> "بله — قرائن حروف بر نیکویی و گشایش این پیوند دلالت دارد؛ الفت و سازگاری در کار است."
            "نحس" -> "نه در حال حاضر — قرائن حروف بر مانع، سردی یا ناسازگاری در این پیوند دلالت دارد؛ با تأمل و مشورت اقدام کنید."
            else -> "پاسخ مردد — قرائن حروف متعادل است؛ با صبر و مشورت با بزرگان اقدام کنید."
        }
        TopicType.TRAVEL -> when (level) {
            "سعد" -> "بله — قرائن حروف بر سفری بی‌خطر و پربرکت دلالت دارد؛ به جهت و روز استخراج‌شده توجه کنید."
            "نحس" -> "نه در حال حاضر — قرائن حروف بر دشواری یا خطر در این سفر دلالت دارد؛ تغییر زمان یا مسیر توصیه می‌شود."
            else -> "پاسخ مردد — قرائن حروف متعادل است؛ با احتیاط و آمادگی کامل سفر کنید."
        }
        TopicType.BUSINESS -> when (level) {
            "سعد" -> "بله — قرائن حروف بر سود و گشایش در این کار دلالت دارد؛ رزق در آن است."
            "نحس" -> "نه در حال حاضر — قرائن حروف بر رکود یا ضرر دلالت دارد؛ شروع این کار را به وقت سعدتر موکول کنید."
            else -> "پاسخ مردد — قرائن حروف متعادل است؛ با برنامه‌ریزی دقیق و سرمایه کم شروع کنید."
        }
        TopicType.CHILDREN -> when (level) {
            "سعد" -> "بله — قرائن حروف بر گشایش در این باب دلالت دارد؛ امید را نگه دارید و اسباب طبیعی را پی بگیرید."
            "نحس" -> "نه در حال حاضر — قرائن حروف بر مانع یا تأخیر دلالت دارد؛ صبر و پی‌گیری اسباب طبیعی توصیه می‌شود."
            else -> "پاسخ مردد — قرائن حروف متعادل است؛ این باب به زمان و صبر بستگی دارد."
        }
        TopicType.ILLNESS -> when (level) {
            "سعد" -> "قرائن حروف بر سلامت و بهبود دلالت دارد؛ در صورت وجود ناخوشی، امید شفا هست — درمان را پی‌گیری کنید."
            "نحس" -> "قرائن حروف بر ناخوشی یا طول کشیدن آن دلالت دارد؛ مراجعه زودهنگام به پزشک و پی‌گیری جدی درمان ضروری است."
            else -> "پاسخ مردد — قرائن حروف متعادل است؛ وضعیت به پی‌گیری درمان بستگی دارد؛ حتماً با پزشک مشورت کنید."
        }
        TopicType.TRANSACTION -> when (level) {
            "سعد" -> "بله — قرائن حروف بر معامله‌ای عادلانه و پرنفع دلالت دارد؛ با استعلام و کارشناسی قیمت اقدام کنید."
            "نحس" -> "نه در حال حاضر — قرائن حروف بر ضرر، فریب یا تأخیر در این معامله دلالت دارد؛ پیش از هر اقدام، کارشناسی و استعلام دقیق بگیرید."
            else -> "پاسخ مردد — قرائن حروف متعادل است؛ معامله را با کارشناسی قیمت و مشاوره حقوقی پیش ببرید."
        }
    }

    private fun buildMarriageAnalysis(ctx: TopicContext): TopicAnalysis {
        val s = Scorer()

        when (ctx.kawkab) {
            "زهره" -> s.add("کوکب سوال زهره است — کوکب محبت و الفت (سعد اصغر)", 2)
            "مشتری" -> s.add("کوکب سوال مشتری است — سعد اکبر و مایه خوشبختی", 1)
            "زحل" -> s.add("کوکب سوال زحل است — نحس اکبر؛ سردی و تأخیر در وصلت", -2)
            "مریخ" -> s.add("کوکب سوال مریخ است — تندی و نزاع در پیوند", -1)
        }

        when (ctx.burj) {
            "میزان" -> s.add("برج سوال میزان است — برج زهره و دلالت بر نکاح", 2)
            "ثور", "اسد", "قوس" -> s.add("برج سوال ${ctx.burj} است — دلالت بر شور و الفت در پیوند", 1)
            "عقرب" -> s.add("برج سوال عقرب است — دلالت بر رازداری و حسادت", -1)
        }

        when (ctx.manzel.disposition) {
            "سعد" -> s.add("منزل قمر سعد است (${ctx.manzel.name}) — گشایش در کار وصلت", 1)
            "نحس" -> s.add("منزل قمر نحس است (${ctx.manzel.name}) — مانع و تأخیر در پیوند", -1)
        }

        when {
            ctx.saad > ctx.nahs -> s.add("حروف مستحصله غالباً سعدند — میل و محبت در حروف حاضر است", 1)
            ctx.nahs > ctx.saad -> s.add("حروف مستحصله غالباً نحس‌اند — کدورت و دلسردی در حروف", -1)
        }

        if (ctx.day == "جمعه") s.add("روز سوال جمعه است — روز زهره و محبت", 1)

        if (ctx.person != null) {
            val questionElement = elementOfBurjRem(ctx.burjRem)
            if (questionElement == ctx.person.element) {
                s.add("طبع سوال ($questionElement) با طبع شخص (${ctx.person.element}) هم‌سوست — سازگاری مزاج", 1)
            } else {
                s.add("طبع سوال ($questionElement) با طبع شخص (${ctx.person.element}) هم‌سو نیست — ناسازگاری مزاج", -1)
            }
        }

        val level = levelOfTopicScore(s.score)
        return TopicAnalysis(
            topic = TopicType.MARRIAGE.label,
            indicators = s.indicators,
            score = s.score,
            level = level,
            verdict = topicVerdict(TopicType.MARRIAGE, level),
            highlights = listOf(
                "کوکب" to ctx.kawkab,
                "برج" to ctx.burj,
                "روز" to ctx.day,
                "منزل قمر" to "${ctx.manzel.name} — ${ctx.manzel.disposition}"
            )
        )
    }

    private fun buildTravelAnalysis(ctx: TopicContext): TopicAnalysis {
        val s = Scorer()

        when (ctx.kawkab) {
            "عطارد" -> s.add("کوکب سوال عطارد است — کوکب حرکت و سفر", 2)
            "قمر" -> s.add("کوکب سوال قمر است — سفر کوتاه و جابه‌جایی", 1)
            "مشتری" -> s.add("کوکب سوال مشتری است — سفر خوش و پربرکت", 1)
            "زحل" -> s.add("کوکب سوال زحل است — سفر سخت و پرزحمت", -2)
            "مریخ" -> s.add("کوکب سوال مریخ است — خطر و درگیری در راه", -1)
        }

        when (ctx.burj) {
            "جوزا", "قوس" -> s.add("برج سوال ${ctx.burj} است — دلالت بر سفر دور", 1)
            "سرطان" -> s.add("برج سوال سرطان است — دلالت بر سفر آبی و کنار آب", 1)
            "عقرب" -> s.add("برج سوال عقرب است — دشواری و خطر در راه", -1)
        }

        when (ctx.manzel.disposition) {
            "سعد" -> s.add("منزل قمر سعد است (${ctx.manzel.name}) — سفر بی‌خطر", 1)
            "نحس" -> s.add("منزل قمر نحس است (${ctx.manzel.name}) — ناملایمات در راه", -1)
        }

        when {
            ctx.saad > ctx.nahs -> s.add("حروف مستحصله غالباً سعدند — گشایش در راه", 1)
            ctx.nahs > ctx.saad -> s.add("حروف مستحصله غالباً نحس‌اند — احتیاط در راه", -1)
        }

        when (ctx.day) {
            "چهارشنبه" -> s.add("روز سوال چهارشنبه است — روز عطارد و حرکت", 1)
            "پنجشنبه" -> s.add("روز سوال پنجشنبه است — روز مشتری؛ سفر خوش", 1)
            "شنبه" -> s.add("روز سوال شنبه است — روز زحل؛ سفر با زحمت", -1)
        }

        val level = levelOfTopicScore(s.score)
        return TopicAnalysis(
            topic = TopicType.TRAVEL.label,
            indicators = s.indicators,
            score = s.score,
            level = level,
            verdict = topicVerdict(TopicType.TRAVEL, level),
            highlights = listOf(
                "جهت سفر" to ctx.direction,
                "روز حرکت" to ctx.day,
                "کوکب" to ctx.kawkab,
                "منزل قمر" to "${ctx.manzel.name} — ${ctx.manzel.disposition}"
            )
        )
    }

    private fun buildBusinessAnalysis(ctx: TopicContext): TopicAnalysis {
        val s = Scorer()

        when (ctx.kawkab) {
            "مشتری" -> s.add("کوکب سوال مشتری است — سعد اکبر؛ سود و رزق", 2)
            "شمس" -> s.add("کوکب سوال شمس است — ترقی و فتوحات در کار", 1)
            "عطارد" -> s.add("کوکب سوال عطارد است — تدبیر و دادوستد", 1)
            "زهره" -> s.add("کوکب سوال زهره است — سعد اصغر؛ رونق و جلب مشتری", 1)
            "زحل" -> s.add("کوکب سوال زحل است — رکود و تأخیر در کار", -2)
            "مریخ" -> s.add("کوکب سوال مریخ است — رقابت تند و ریسک ضرر", -1)
        }

        when (ctx.burj) {
            "ثور" -> s.add("برج سوال ثور است — برج مال و اندوخته", 1)
            "سنبله" -> s.add("برج سوال سنبله است — برج کار و خدمت", 1)
            "جدی" -> s.add("برج سوال جدی است — پایداری و صبر در کار", 1)
            "حمل" -> s.add("برج سوال حمل است — شتاب و بی‌نظمی در کار", -1)
            "عقرب" -> s.add("برج سوال عقرب است — مال پنهان و ریسک", -1)
        }

        when (ctx.manzel.name) {
            "بلده" -> s.add("منزل قمر بلده است — رکود و ایستایی کار", -2)
            "نثره" -> s.add("منزل قمر نثره است — پراکندگی و دودلی در کار", -1)
            else -> when (ctx.manzel.disposition) {
                "سعد" -> s.add("منزل قمر سعد است (${ctx.manzel.name}) — گشایش در رزق", 1)
                "نحس" -> s.add("منزل قمر نحس است (${ctx.manzel.name}) — مانع در کار", -1)
            }
        }

        when {
            ctx.saad > ctx.nahs -> s.add("حروف مستحصله غالباً سعدند — برکت در کار", 1)
            ctx.nahs > ctx.saad -> s.add("حروف مستحصله غالباً نحس‌اند — نفع کم و زحمت زیاد", -1)
        }

        when (ctx.day) {
            "پنجشنبه" -> s.add("روز سوال پنجشنبه است — روز مشتری؛ شروع کار نیکو", 1)
            "شنبه" -> s.add("روز سوال شنبه است — روز زحل؛ کار با زحمت و تأخیر", -1)
        }

        val level = levelOfTopicScore(s.score)
        return TopicAnalysis(
            topic = TopicType.BUSINESS.label,
            indicators = s.indicators,
            score = s.score,
            level = level,
            verdict = topicVerdict(TopicType.BUSINESS, level),
            highlights = listOf(
                "کوکب" to ctx.kawkab,
                "برج" to ctx.burj,
                "روز" to ctx.day,
                "منزل قمر" to "${ctx.manzel.name} — ${ctx.manzel.disposition}"
            )
        )
    }

    private fun buildChildrenAnalysis(ctx: TopicContext): TopicAnalysis {
        val s = Scorer()

        when (ctx.kawkab) {
            "قمر" -> s.add("کوکب سوال قمر است — کوکب فرزند و نسل", 2)
            "مشتری" -> s.add("کوکب سوال مشتری است — فراوانی و برکت اولاد", 1)
            "زهره" -> s.add("کوکب سوال زهره است — مهر و الفت در فرزند", 1)
            "زحل" -> s.add("کوکب سوال زحل است — تأخیر و مانع در این باب", -2)
            "مریخ" -> s.add("کوکب سوال مریخ است — سختی و ناآرامی", -1)
        }

        when (ctx.burj) {
            "سرطان" -> s.add("برج سوال سرطان است — منزل قمر و دلالت بر باروری", 2)
            "حمل" -> s.add("برج سوال حمل است — آغاز و شروع", 1)
            "حوت" -> s.add("برج سوال حوت است — دلالت بر فرزند و نسل", 1)
        }

        when (ctx.manzel.name) {
            "بطین" -> s.add("منزل قمر بطین است — جمع شدن و فراهم شدن", 2)
            else -> when (ctx.manzel.disposition) {
                "سعد" -> s.add("منزل قمر سعد است (${ctx.manzel.name}) — گشایش در این باب", 1)
                "نحس" -> s.add("منزل قمر نحس است (${ctx.manzel.name}) — مانع و تأخیر", -1)
            }
        }

        when {
            ctx.saad > ctx.nahs -> s.add("حروف مستحصله غالباً سعدند — امید در این باب", 1)
            ctx.nahs > ctx.saad -> s.add("حروف مستحصله غالباً نحس‌اند — نگرانی و احتیاط", -1)
        }

        if (ctx.day == "دوشنبه") s.add("روز سوال دوشنبه است — روز قمر؛ مناسب این باب", 1)

        val level = levelOfTopicScore(s.score)
        return TopicAnalysis(
            topic = TopicType.CHILDREN.label,
            indicators = s.indicators,
            score = s.score,
            level = level,
            verdict = topicVerdict(TopicType.CHILDREN, level),
            highlights = listOf(
                "کوکب" to ctx.kawkab,
                "برج" to ctx.burj,
                "روز" to ctx.day,
                "منزل قمر" to "${ctx.manzel.name} — ${ctx.manzel.disposition}"
            )
        )
    }

    private fun buildIllnessAnalysis(ctx: TopicContext): TopicAnalysis {
        val s = Scorer()

        when (ctx.kawkab) {
            "شمس" -> s.add("کوکب سوال شمس است — سعد و شفای امراض", 2)
            "عطارد" -> s.add("کوکب سوال عطارد است — تدبیر درمان و شفای امراض", 1)
            "زحل" -> s.add("کوکب سوال زحل است — نحس اکبر؛ بیماری مزمن و سخت", -2)
            "مریخ" -> s.add("کوکب سوال مریخ است — تب و التهاب؛ بیماری حاد", -1)
        }

        when (ctx.burj) {
            "عقرب" -> s.add("برج سوال عقرب است — خانه مریخ؛ بیماری نهان و دشوار", -2)
            "حمل" -> s.add("برج سوال حمل است — خانه مریخ؛ سردرد و تب", -1)
            "اسد" -> s.add("برج سوال اسد است — خانه شمس؛ قوت حیات", 1)
        }

        when (ctx.manzel.disposition) {
            "سعد" -> s.add("منزل قمر سعد است (${ctx.manzel.name}) — بهبود و آسایش", 1)
            "نحس" -> s.add("منزل قمر نحس است (${ctx.manzel.name}) — ضعف و ناخوشی", -1)
        }

        when {
            ctx.saad > ctx.nahs -> s.add("حروف مستحصله غالباً سعدند — قوت بدن و شفا", 1)
            ctx.nahs > ctx.saad -> s.add("حروف مستحصله غالباً نحس‌اند — ضعف مزاج و بیماری", -1)
        }

        when (ctx.day) {
            "یکشنبه" -> s.add("روز سوال یکشنبه است — روز شمس؛ امید شفا", 1)
            "شنبه" -> s.add("روز سوال شنبه است — روز زحل؛ بیماری با سستی", -1)
        }

        val level = levelOfTopicScore(s.score)
        return TopicAnalysis(
            topic = TopicType.ILLNESS.label,
            indicators = s.indicators,
            score = s.score,
            level = level,
            verdict = topicVerdict(TopicType.ILLNESS, level),
            highlights = listOf(
                "کوکب" to ctx.kawkab,
                "برج" to ctx.burj,
                "روز" to ctx.day,
                "منزل قمر" to "${ctx.manzel.name} — ${ctx.manzel.disposition}"
            ),
            notice = "این تحلیل جایگزین تشخیص و درمان پزشکی نیست؛ در صورت بیماری حتماً به پزشک متخصص مراجعه کنید."
        )
    }

    private fun buildTransactionAnalysis(ctx: TopicContext): TopicAnalysis {
        val s = Scorer()

        when (ctx.kawkab) {
            "مشتری" -> s.add("کوکب سوال مشتری است — سعد اکبر؛ برکت و سود در معامله", 2)
            "شمس" -> s.add("کوکب سوال شمس است — سعد؛ موفقیت در معامله", 1)
            "عطارد" -> s.add("کوکب سوال عطارد است — قرارداد و مذاکره؛ تدبیر معامله", 1)
            "زهره" -> s.add("کوکب سوال زهره است — سعد اصغر؛ مال و قیمت منصفانه", 1)
            "زحل" -> s.add("کوکب سوال زحل است — نحس اکبر؛ ضرر، فریب یا تأخیر در معامله", -2)
            "مریخ" -> s.add("کوکب سوال مریخ است — نزاع و خسران در معامله", -1)
        }

        when (ctx.burj) {
            "میزان" -> s.add("برج سوال میزان است — برج عدل و تعادل؛ دلالت بر معامله منصفانه", 2)
            "ثور" -> s.add("برج سوال ثور است — برج مال؛ دلالت بر سود و اندوخته", 1)
            "جدی" -> s.add("برج سوال جدی است — دلالت بر املاک و پایداری معامله", 1)
            "عقرب" -> s.add("برج سوال عقرب است — دلالت بر پنهان‌کاری و فریب در معامله", -2)
            "حمل" -> s.add("برج سوال حمل است — شتاب و بی‌تأملی در معامله", -1)
        }

        when (ctx.manzel.name) {
            "بلده" -> s.add("منزل قمر بلده است — رکود و ایستایی معامله", -2)
            "نثره" -> s.add("منزل قمر نثره است — پراکندگی و دودلی در معامله", -1)
            else -> when (ctx.manzel.disposition) {
                "سعد" -> s.add("منزل قمر سعد است (${ctx.manzel.name}) — گشایش و نفع در معامله", 1)
                "نحس" -> s.add("منزل قمر نحس است (${ctx.manzel.name}) — مانع و زیان در معامله", -1)
            }
        }

        when {
            ctx.saad > ctx.nahs -> s.add("حروف مستحصله غالباً سعدند — راستی و برکت در معامله", 1)
            ctx.nahs > ctx.saad -> s.add("حروف مستحصله غالباً نحس‌اند — خدعه و بی‌برکتی در معامله", -1)
        }

        when (ctx.day) {
            "پنجشنبه" -> s.add("روز سوال پنجشنبه است — روز مشتری؛ معامله پربرکت", 1)
            "چهارشنبه" -> s.add("روز سوال چهارشنبه است — روز عطارد؛ قرارداد و مذاکره نیکو", 1)
            "شنبه" -> s.add("روز سوال شنبه است — روز زحل؛ معامله با زحمت و تأخیر", -1)
        }

        val level = levelOfTopicScore(s.score)
        return TopicAnalysis(
            topic = TopicType.TRANSACTION.label,
            indicators = s.indicators,
            score = s.score,
            level = level,
            verdict = topicVerdict(TopicType.TRANSACTION, level),
            highlights = listOf(
                "کوکب" to ctx.kawkab,
                "برج" to ctx.burj,
                "روز" to ctx.day,
                "منزل قمر" to "${ctx.manzel.name} — ${ctx.manzel.disposition}"
            ),
            notice = "این تحلیل جایگزین کارشناسی و مشاوره حقوقی نیست؛ پیش از معامله استعلام سند، بازرسی فنی و مقایسه قیمت بازار را فراموش نکنید."
        )
    }

    private fun formatAnswer(t: JafrTaqsimat): String = buildString {
        appendLine("جمع ابجد کبیر: ${toPersianNumber(t.total)}")
        appendLine("جهت وقوع (÷۴): ${t.direction}")
        appendLine("روز (÷۷): ${t.day}")
        appendLine("کوکب سوال (÷۷): ${t.kawkab}")
        appendLine("برج (÷۱۲): ${t.burj}")
        appendLine("منزل قمر (÷۲۸): ${t.manzel.name} — «${t.manzel.meaning}» (${t.manzel.disposition})")
        appendLine()
        t.person?.let { p ->
            appendLine("طالع شخص (${p.firstName} زاده ${p.motherName}): برج ${p.burj} | طبع ${p.element} | کوکب ${p.kawkab}")
            appendLine()
        }
        appendLine("مستحصله: ${formatLetters(t.mustahsalah)}")
        appendLine("حروف سعد: ${toPersianNumber(t.saadCount)} | حروف نحس: ${toPersianNumber(t.nahsCount)} → غالب: ${t.dominant}")
        appendLine()
        t.cross?.let { c ->
            appendLine("قرائن سه مسیر:")
            c.paths.forEach { p ->
                appendLine("مسیر ${p.label} (${toPersianNumber(p.total)}): جهت ${p.direction} | روز ${p.day} | برج ${p.burj} | منزل ${p.manzel.name} (${p.manzel.disposition})")
            }
            appendLine("همسویی: ${c.confidence} — ${toPersianNumber(c.agreeCount)} از ۳ مسیر ${c.consensus}")
            appendLine("نظیره‌ها: ${c.nadhiraPolarities.joinToString(" | ") { (label, pol) -> "$label: $pol" }}")
            appendLine()
        }
        t.time?.let { tm ->
            appendLine("قرینه زمان (${tm.weekday}): کوکب روز ${tm.dayKawkab} | کوکب ساعت ${tm.hourKawkab}")
            appendLine(tm.note)
            appendLine()
        }
        t.topics.forEach { topic ->
            appendLine("تحلیل موضوع: ${topic.topic}")
            topic.highlights.forEach { (key, value) -> appendLine("$key: $value") }
            topic.indicators.forEach { appendLine("• $it") }
            appendLine("نتیجه ${topic.topic}: ${topic.verdict}")
            appendLine()
        }
        t.spell?.let { s ->
            appendLine("تحلیل طلسم / سحر:")
            s.indicators.forEach { appendLine("• $it") }
            appendLine("سطح دلالت: ${s.level}")
            appendLine("حروف استخراجی عامل (تخلیص): ${formatLetters(s.khalesLetters)}")
            appendLine("عامل: برج ${s.factorBurj} | طبع ${s.factorElement} | ${s.factorGender} | کوکب ${s.factorKawkab}")
            if (s.factorRelation.isNotBlank()) appendLine("نسبت: ${s.factorRelation}")
            appendLine("جهت اثر: ${t.direction}")
            appendLine("حکم طلسم: ${s.verdict}")
            appendLine()
            appendLine("یادآوری: در منابع سنتی تصریح شده که جفر علم غیب نیست؛ تطبیق نهایی اسم بر حروف، با بصیرت سائل است.")
            appendLine()
        }
        append("حکم نطق: ${t.verdict}")
    }

    fun extractNames(text: String): Pair<String?, String?> {
        val keywords = listOf("زاده", "فرزند", "بن", "ابن", "بنت")
        val cleanText = normalizeText(text).trim()
            .replace("^آیا\\s+".toRegex(), "")
            .replace("^ایا\\s+".toRegex(), "")
        val words = cleanText.split("\\s+".toRegex())
        for (keyword in keywords) {
            val index = words.indexOf(keyword)
            if (index > 0 && index < words.size - 1) {
                return Pair(words[index - 1], words[index + 1])
            }
        }
        return Pair(null, null)
    }

    fun toPersianNumber(number: Any): String {
        val str = number.toString()
        val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        return str.map { char -> if (char.isDigit()) persianDigits[char - '0'] else char }.joinToString("")
    }
}
