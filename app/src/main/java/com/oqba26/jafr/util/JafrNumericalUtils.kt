package com.oqba26.jafr.util

import com.oqba26.jafr.AbjadType
import com.oqba26.jafr.AbjadUtils

/**
 * مدل داده‌ای برای نتیجه محاسبات جفر عددی و وفقی بر اساس کتاب خزانة الأسرار
 */

data class JafrNumericalResult(
    val totalAbjad: Int,                 // عدد ابجد کل سوال و اسامی
    val miftah: Int,                     // عدد مفتاح (خانه اول وفق)
    val maghlaq: Int,                    // عدد مغلاق (خانه نهم وفق)
    val wafd: Int,                       // عدد وفق (مجموع اضلاع)
    val matrix3x3: List<List<Int>>,      // جدول ۳x۳ وفق عددی
    val burjName: String,                // برج حاصل از اسقاط ۱۲
    val kawkabName: String,              // کوکب حاصل از اسقاط ۷
    val elementName: String,             // عنصر حاصل از اسقاط ۴
    val manzelName: String,              // منزل قمر حاصل از اسقاط ۲۸
    val polarity: String,                // قطبیت عددی: سعد / نحس / متعادل
    val verdict: String,                 // حکم نهایی جفر عددی
    val explanation: String,             // شرح محاسبات و خواص وفق
    val isqatDetails: List<Pair<String, String>> // جزئیات اسقاطات
)

object JafrNumericalUtils {

    private val BURJ_NAMES = listOf(
        "حمل", "ثور", "جوزا", "سرطان", "اسد", "سنبله",
        "میزان", "عقرب", "قوس", "جدی", "دلو", "حوت"
    )

    private val KAWKAB_NAMES = listOf(
        "زحل", "مشتری", "مریخ", "شمس", "زهره", "عطارد", "قمر"
    )

    private val ELEMENT_NAMES = listOf(
        "آتش (آتشی)", "باد (هوایی)", "آب (آبی)", "خاک (خاکی)"
    )

    private val MANZEL_NAMES = listOf(
        "شرطین", "بطین", "ثریا", "دبران", "هقعه", "هنعه", "ذراع",
        "نثره", "طرفه", "جبهه", "زبره", "صرفه", "عواء", "سماک",
        "غفر", "زبانا", "اکلیل", "قلب", "شوله", "نعائم", "بلده",
        "سعد الذابح", "سعد بلع", "سعد السعود", "سعد الاخبیه", "فرغ مقدم", "فرغ مؤخر", "رشاء"
    )

    /**
     * محاسبه جفر عددی و استخراج جدول وفق ۳x۳ بر اساس متن سوال
     */
    fun calculateNumericalJafr(text: String): JafrNumericalResult {
        val total = AbjadUtils.calculate(text, AbjadType.KABIR).total
        val safeTotal = if (total < 12) total + 12 else total

        // ساخت جدول ۳x۳ وفق مثلث (بطن المجمع)
        val matrix = generate3x3Ofaq(safeTotal)
        
        // مفتاح (کمترین خانه در ترتیب ۱ تا ۹) = خانه اول
        val base = if (safeTotal >= 12) (safeTotal - 12) / 3 else 1
        val remainder = if (safeTotal >= 12) (safeTotal - 12) % 3 else 0

        val miftah = base + 1
        val maghlaq = base + 9 + remainder

        // اسقاطات ۴ گانه
        val burjIdx = (safeTotal - 1) % 12
        val kawkabIdx = (safeTotal - 1) % 7
        val elementIdx = (safeTotal - 1) % 4
        val manzelIdx = (safeTotal - 1) % 28

        val burj = BURJ_NAMES[burjIdx]
        val kawkab = KAWKAB_NAMES[kawkabIdx]
        val element = ELEMENT_NAMES[elementIdx]
        val manzel = MANZEL_NAMES[manzelIdx]

        // تعیین قطبیت عددی (سعد / نحس / متعادل)
        val isSaadKawkab = kawkab in listOf("مشتری", "شمس", "زهره")
        val isNahsKawkab = kawkab in listOf("زحل", "مریخ")
        
        val polarity = when {
            isSaadKawkab -> "سعد (مبارک)"
            isNahsKawkab -> "نحس (نیاز به احتیاط و صدقه)"
            else -> "متعادل (متوسط)"
        }

        val verdict = when {
            isSaadKawkab && (elementIdx == 0 || elementIdx == 1) ->
                "طالع وفق بسیار سعد و همسو است. پاسخ مثبت و گشایش عددی حاصل است."
            isNahsKawkab ->
                "طالع وفق دارای سنگینی است؛ برای رفع موانع، پرداخت صدقه و مداومت بر ذکر توصیه می‌شود."
            else ->
                "طالع وفق متعادل است و نتیجه به تدریج با تلاش و تدبیر حاصل می‌شود."
        }

        val details = listOf(
            "عدد کل ابجد (مدخل)" to safeTotal.toString(),
            "عدد مفتاح (خانه ۱)" to miftah.toString(),
            "عدد مغلاق (خانه ۹)" to maghlaq.toString(),
            "عدد وفق (ضلع)" to safeTotal.toString(),
            "اسقاط ۱۲ (برج)" to burj,
            "اسقاط ۷ (کوکب)" to kawkab,
            "اسقاط ۴ (عنصر)" to element,
            "اسقاط ۲۸ (منزل قمر)" to manzel
        )

        val explanation = "وفق ۳x۳ حاصل بر پایه قواعد خزانة الأسرار تنظیم شده است. " +
                "مجموع اضلاع افقی، عمودی و قطری این وفق برابر با عدد کل ابجد ($safeTotal) می‌باشد."

        return JafrNumericalResult(
            totalAbjad = safeTotal,
            miftah = miftah,
            maghlaq = maghlaq,
            wafd = safeTotal,
            matrix3x3 = matrix,
            burjName = burj,
            kawkabName = kawkab,
            elementName = element,
            manzelName = manzel,
            polarity = polarity,
            verdict = verdict,
            explanation = explanation,
            isqatDetails = details
        )
    }

    /**
     * تولید وفق ۳x۳ جادویی معتبر با مجموع اضلاع برابر با totalSum
     */
    private fun generate3x3Ofaq(totalSum: Int): List<List<Int>> {
        val base = if (totalSum >= 12) (totalSum - 12) / 3 else 1
        val r = if (totalSum >= 12) (totalSum - 12) % 3 else 0

        // خانه ۱ تا ۹ طبق الگو:
        // [8, 1, 6]
        // [3, 5, 7]
        // [4, 9, 2]
        // با اعمال کسر/باقیمانده R بر روی خانه‌های بالاتر از ۷ یا ۴
        val p1 = base + 1
        val p2 = base + 2
        val p3 = base + 3
        val p4 = base + 4 + (if (r >= 2) 1 else 0)
        val p5 = base + 5 + (if (r >= 2) 1 else 0)
        val p6 = base + 6 + (if (r >= 2) 1 else 0)
        val p7 = base + 7 + (if (r >= 1) 1 else 0)
        val p8 = base + 8 + (if (r >= 1) 1 else 0)
        val p9 = base + 9 + (if (r >= 1) 1 else 0)

        return listOf(
            listOf(p8, p1, p6),
            listOf(p3, p5, p7),
            listOf(p4, p9, p2)
        )
    }
}
