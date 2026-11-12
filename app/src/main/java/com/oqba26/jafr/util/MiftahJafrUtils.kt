package com.oqba26.jafr.util

import com.oqba26.jafr.AbjadType
import com.oqba26.jafr.AbjadUtils
import com.oqba26.jafr.Element
import saman.zamani.persiandate.PersianDate
import java.util.Calendar

/**
 * نتایج و الگوریتم‌های استخراج‌شده از کتاب خطی «مفتاح طلاسم کنوز»
 */

data class MuwakkilResult(
    val personName: String,
    val motherName: String,
    val kawkabName: String,
    val interleavedLetters: String,
    val muwakkilAlavi: String,
    val muwakkilSifli: String,
    val vocalization: String,
    val abjadCount: Int,
    val bukhoor: String,
    val clothingAndRing: String,
    val azimatText: String
)

data class KawakibHourInfo(
    val currentHourIndex: Int,          // ۱ تا ۱۲
    val isDay: Boolean,                 // روز یا شب
    val hourTitle: String,              // مثلاً "ساعت سوم روز"
    val rulerKawkab: String,            // کوکب حاکم بر ساعت
    val dayRulerKawkab: String,         // کوکب حاکم بر کل روز
    val nature: String,                 // سعد اکبر، سعد اصغر، نحس، ...
    val natureBadge: String,            // "سعد"، "نحس"، "متعادل"
    val bukhoor: String,                // بخور منسوب (ص ۲۱۶)
    val ink: String,                    // مرکب و جوهر (ص ۲۲۱)
    val clothingAndRing: String,        // جامه و نگین (ص ۲۱۷)
    val socialClass: String,            // اصناف و طبقات مردم (ص ۲۲۲)
    val dayHoursList: List<Pair<String, String>>,   // ۱۲ ساعت روز به همراه کوکب
    val nightHoursList: List<Pair<String, String>>  // ۱۲ ساعت شب به همراه کوکب
)

data class RijalAlGhaibInfo(
    val lunarDay: Int,                  // روز ماه قمری (۱ تا ۳۰)
    val directionName: String,          // مثلاً "جنوب غربی"
    val compassAngle: Float,            // زاویه قطب‌نما (۰ = شمال)
    val sittingAdvice: String,          // توصیه جهت نشستن و رو به قبله بودن
    val roussArbaa: List<String>        // ملائکه و موکلان جهات اربعه (ص ۱۵۵-۱۵۹)
)

data class TabayeMizanResult(
    val fireLetters: List<Char>,
    val airLetters: List<Char>,
    val waterLetters: List<Char>,
    val earthLetters: List<Char>,
    val dominantElement: Element,
    val counterElement: Element,
    val balancingLetters: List<Char>,
    val balancingAdvice: String
)

data class AncientScriptResult(
    val originalText: String,
    val symbolsText: String,
    val details: List<Triple<Char, String, String>> // حرف، نام کهن، نماد یونیکد/طلسماتی
)

data class OfaqExtendedResult(
    val size: Int,                      // ۳، ۴، ۵
    val title: String,
    val totalSum: Int,
    val miftah: Int,
    val maghlaq: Int,
    val wafd: Int,
    val adl: Int,
    val kasr: Int,
    val kasrHouse: String,
    val matrix: List<List<Int>>,
    val explanation: String
)

object MiftahJafrUtils {

    // ترتیب کواکب سبعه بر اساس فلک کالدانی (کلدانی) از بالا به پایین: زحل، مشتری، مریخ، شمس، زهره، عطارد، قمر
    private val CHALDEAN_ORDER = listOf("زحل", "مشتری", "مریخ", "شمس", "زهره", "عطارد", "قمر")

    // کواکب حاکم بر روزهای هفته:
    // شنبه: زحل | یکشنبه: شمس | دوشنبه: قمر | سه‌شنبه: مریخ | چهارشنبه: عطارد | پنجشنبه: مشتری | جمعه: زهره
    private val DAY_RULERS = mapOf(
        Calendar.SATURDAY to "زحل",
        Calendar.SUNDAY to "شمس",
        Calendar.MONDAY to "قمر",
        Calendar.TUESDAY to "مریخ",
        Calendar.WEDNESDAY to "عطارد",
        Calendar.THURSDAY to "مشتری",
        Calendar.FRIDAY to "زهره"
    )

    // بخورات کواکب هفتگانه مستخرج دقیق از صفحه ۲۱۶ کتاب مفتاح طلاسم کنوز
    private val KAWKAB_BUKHOOR = mapOf(
        "زحل" to "سرب، حلتيت (انغوزه)، پوست سیر و پیاز و گوگرد",
        "مشتری" to "کافور، صندل سفید، خرده مرجان، مازو و زعفران",
        "مریخ" to "شنگرف، خون خشکیده، گوگرد سرخ و فلفل سیاه",
        "شمس" to "کندر نر، عود هندی، زعفران، صندل سرخ و گلاب خالص",
        "زهره" to "شکر سرخ، مصطکی، جاوی، مشک، کافور و عنبر",
        "عطارد" to "برگ ریحان خشک، پوست گشنیز، صندل زرد و لادن",
        "قمر" to "کافور سفید، پوست لیمو، عود خام و صندل ابیض"
    )

    // جامه، فلز و انگشتر مستخرج از صفحه ۲۱۷ کتاب
    private val KAWKAB_CLOTHING_RING = mapOf(
        "زحل" to "جامه کبود، انگشتر آهن و سرب با نقش حروفی «ن ط»",
        "مشتری" to "جامه سفید و زرد شبیه زاهدان، انگشتر عقیق با نقش «ح ج»",
        "مریخ" to "جامه سرخ و حریر، انگشتر آهن با نقش «م د»",
        "شمس" to "جامه زرد زرین ملوکانه، انگشتر طلا با نقش «ا ک»",
        "زهره" to "جامه بنفش و فاخر با جواهرات آراسته، انگشتر مس با نقش «ب ذ»",
        "عطارد" to "جامه پنبه‌ای سفید و دستار گرد، انگشتر سیم (نقره) یا برنج",
        "قمر" to "جامه نقره‌ای یا سپید پاکیزه، انگشتر نقره با نگین فیروزه یا مروارید"
    )

    // جوهر و مرکب مستخرج از صفحه ۲۲۱ کتاب
    private val KAWKAB_INK = mapOf(
        "زحل" to "مرکب سرکه، نیل و شنگرف",
        "مشتری" to "زعفران ممتاز و گلاب خالص",
        "مریخ" to "شنگرف سرخ و عصاره انار ترش",
        "شمس" to "زعفران و مشک سیاه ساییده در گلاب",
        "زهره" to "شکر، زعفران، گلاب و مشک سفید",
        "عطارد" to "گلاب و زعفران رقیق همراه صمغ عربی",
        "قمر" to "آب زمزم، گلاب، زعفران یا قلم نقره"
    )

    // اصناف مردم منسوب به کواکب مستخرج از صفحه ۲۲۲ کتاب
    private val KAWKAB_SOCIAL = mapOf(
        "زحل" to "دهقانان، ارباب زمین، کهنسالان، بنایان و ارباب بناهای دیرین",
        "مشتری" to "قاضیان، عالمان، فقیهان، دانشمندان، وزرا و اشراف اهل ورع",
        "مریخ" to "سپاهیان، ارباب سلاح، پزشکان، جراحان و صاحبان شجاعت",
        "شمس" to "پادشاهان، حاکمان، ملوک، روسا و صاحبان مجد و رفعت",
        "زهره" to "بانوان، اهل هنر، خوانندگان، مطربان و ارباب نشاط و خرمی",
        "عطارد" to "نویسندگان، اهل قلم، بازرگانان، دبیران، حسابداران و محققین",
        "قمر" to "عوام‌الناس، پیام‌رسانان، دریانوردان، سیاحان و صیادان"
    )

    // طبیعت و سعد/نحس کواکب
    private val KAWKAB_NATURE = mapOf(
        "مشتری" to Pair("سعد اکبر (فوق‌العاده مبارک برای فتوحات و عهدها)", "سعد"),
        "زهره" to Pair("سعد اصغر (بسیار نیکو برای الفت، محبت و نشاط)", "سعد"),
        "شمس" to Pair("سعد معتدل (قوی برای عزت، رفعت و امور حاکمان)", "سعد"),
        "عطارد" to Pair("ممتزج (همرنگ کوکب متصل؛ نیکو برای کتابت و تجارت)", "متعادل"),
        "قمر" to Pair("سعد سریع‌التأثیر (مناسب برای شروع کارهای روزمره و سفر)", "متعادل"),
        "مریخ" to Pair("نحس اصغر (دفع دشمنان و اعمال جلالی؛ احتیاط در شروع صلح)", "نحس"),
        "زحل" to Pair("نحس اکبر (صبر، اعمال خاکی و ثبات؛ احتیاط در امور شادمانی)", "نحس")
    )

    // موکلان جهات اربعه و اعوان (مستخرج از صفحات ۱۵۵ تا ۱۵۹ کتاب)
    val ROUSS_ARBAA = listOf(
        "صمصمائیل (موکل جهت شرق - ناری)",
        "هطمطائیل (موکل جهت غرب - مائی)",
        "همطویل (موکل جهت شمال - ترابی)",
        "مهکائیل / طلهکائیل (موکل جهت جنوب - هوایی)"
    )

    fun cleanArabicText(text: String): String {
        val allowed = "ابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهی"
        return text.map {
            when (it) {
                'ي', 'ى' -> 'ی'
                'أ', 'إ', 'ٱ', 'آ' -> 'ا'
                'ك' -> 'ک'
                'ھ', 'ہ' -> 'ه'
                'ؤ' -> 'و'
                else -> it
            }
        }.filter { it in allowed }.joinToString("")
    }

    // -------------------------------------------------------------
    // ۱. الگوریتم استخراج موکل عمل، عزیمت و عدد تکرار (صفحه ۲۱۸ کتاب)
    // -------------------------------------------------------------
    fun extractMuwakkil(
        personName: String,
        motherName: String,
        kawkabOverride: String? = null
    ): MuwakkilResult {
        val pClean = cleanArabicText(personName).ifEmpty { "شخص" }
        val mClean = cleanArabicText(motherName).ifEmpty { "حوا" }

        // تعیین کوکب: یا اختیاری، یا بر اساس اسقاط ۷ ابجد نام شخص و مادر
        val totalNames = AbjadUtils.calculate(pClean, AbjadType.KABIR).total +
                AbjadUtils.calculate(mClean, AbjadType.KABIR).total
        val kawkabIdx = ((totalNames - 1).coerceAtLeast(0)) % 7
        val kawkab = kawkabOverride?.takeIf { it in CHALDEAN_ORDER } ?: CHALDEAN_ORDER[kawkabIdx]

        // ۱. تداخل دوری حروف سه اسم (صفحه ۲۱۸: «حرف اول شخص، حرف اول مادر، حرف اول کوکب...»)
        val maxLen = maxOf(pClean.length, mClean.length, kawkab.length)
        val sbInterleaved = StringBuilder()
        for (i in 0 until maxLen) {
            if (i < pClean.length) sbInterleaved.append(pClean[i])
            if (i < mClean.length) sbInterleaved.append(mClean[i])
            if (i < kawkab.length) sbInterleaved.append(kawkab[i])
        }
        val interleaved = sbInterleaved.toString()

        // استخراج نام اصلی موکل از حروف ابتدایی متداخل (معمولاً ۴ تا ۶ حرف برای روانی نام)
        val baseLetters = interleaved.take(5)

        // ۲. افزودن پسوند ملکوتی ائیل (علوی) یا یوش/طیش (سفلی) طبق متن ص ۲۱۸
        val alavi = baseLetters + "ائیل"
        val sifli = baseLetters + "یوش"

        // ۳. اعراب‌گذاری روانی نام طبق قاعده التقاء ساکنین در ص ۲۱۸ (کسره دادن و پیوند)
        val vocalized = buildVocalization(baseLetters) + "ائیل"

        // ۴. استخراج عدد ابجد جهت تکرار ذکر
        val abjadCount = AbjadUtils.calculate(alavi, AbjadType.KABIR).total

        val bukhoor = KAWKAB_BUKHOOR[kawkab] ?: "کندر و عود خوشبو"
        val clothRing = KAWKAB_CLOTHING_RING[kawkab] ?: "جامه پاکیزه سپید"

        val azimat = "بسم الله الرحمن الرحیم. اجب یا $alavi بحق کوکب $kawkab وبحق الملک الغالب علیک " +
                "وبحق اسم الله الأعظم أن تقضی حاجتی وتوفقنی فی هذا الأمر. الوحا ۲ العجل ۲ الساعة ۲. بارک الله فیک وعلیک."

        return MuwakkilResult(
            personName = pClean,
            motherName = mClean,
            kawkabName = kawkab,
            interleavedLetters = interleaved,
            muwakkilAlavi = alavi,
            muwakkilSifli = sifli,
            vocalization = vocalized,
            abjadCount = abjadCount,
            bukhoor = bukhoor,
            clothingAndRing = clothRing,
            azimatText = azimat
        )
    }

    private fun buildVocalization(letters: String): String {
        val sb = StringBuilder()
        letters.forEachIndexed { idx, ch ->
            sb.append(ch)
            if (idx < letters.length - 1) {
                // کسره دادن یا حرکت موافق برای رفع التقاء ساکنین
                sb.append(if (idx % 2 == 0) "َ" else "ِ")
            }
        }
        return sb.toString()
    }

    // -------------------------------------------------------------
    // ۲. سیستم ساعات کواکب و تقویم اختیارات (صفحات ۲۲۰ و ۲۲۱ کتاب)
    // -------------------------------------------------------------
    fun calculateCurrentKawakibHour(calendar: Calendar = Calendar.getInstance()): KawakibHourInfo {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayRuler = DAY_RULERS[dayOfWeek] ?: "شمس"

        // فرض نجومی ساعات متساویه (یا ساعات زمانی بر اساس میانگین ۶ صبح طلوع و ۶ عصر غروب)
        val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
        val isDay = hour24 in 6..17

        val currentHourIdx = if (isDay) {
            (hour24 - 6).coerceIn(0, 11)
        } else {
            if (hour24 >= 18) (hour24 - 18) else (hour24 + 6)
        }.coerceIn(0, 11)

        // محاسبه کواکب ساعات ۱۲ گانه روز (شروع از کوکب حاکم بر روز و چرخش به ترتیب کلدانی معکوس: زحل، مشتری، مریخ...)
        val startIdx = CHALDEAN_ORDER.indexOf(dayRuler).let { if (it >= 0) it else 0 }

        val dayHours = (0 until 12).map { h ->
            val kawkab = CHALDEAN_ORDER[(startIdx + h) % 7]
            val title = "ساعت ${h + 1} روز (${h + 6}:00)"
            title to kawkab
        }

        // ساعات شب (ادامه ترتیب کلدانی بعد از ساعت دوازدهم روز)
        val nightStartIdx = (startIdx + 12) % 7
        val nightHours = (0 until 12).map { h ->
            val kawkab = CHALDEAN_ORDER[(nightStartIdx + h) % 7]
            val clock = (h + 18) % 24
            val title = "ساعت ${h + 1} شب (${clock}:00)"
            title to kawkab
        }

        val currentKawkab = if (isDay) dayHours[currentHourIdx].second else nightHours[currentHourIdx].second
        val hourTitle = if (isDay) "ساعت ${currentHourIdx + 1} روز" else "ساعت ${currentHourIdx + 1} شب"

        val natPair = KAWKAB_NATURE[currentKawkab] ?: Pair("متعادل", "متعادل")

        return KawakibHourInfo(
            currentHourIndex = currentHourIdx + 1,
            isDay = isDay,
            hourTitle = hourTitle,
            rulerKawkab = currentKawkab,
            dayRulerKawkab = dayRuler,
            nature = natPair.first,
            natureBadge = natPair.second,
            bukhoor = KAWKAB_BUKHOOR[currentKawkab] ?: "",
            ink = KAWKAB_INK[currentKawkab] ?: "",
            clothingAndRing = KAWKAB_CLOTHING_RING[currentKawkab] ?: "",
            socialClass = KAWKAB_SOCIAL[currentKawkab] ?: "",
            dayHoursList = dayHours,
            nightHoursList = nightHours
        )
    }

    // -------------------------------------------------------------
    // ۳. ماژول جهت‌یابی رجال‌الغیب و جهات اربعه (صفحه ۱۴۰ و ۱۵۹ کتاب)
    // -------------------------------------------------------------
    fun calculateRijalAlGhaib(lunarDayOverride: Int? = null): RijalAlGhaibInfo {
        // محاسبه روز ماه قمری (تقریب استاندارد با PersianDate)
        val pDate = PersianDate()
        // تقویم قمری بر اساس فرمول تقریبی یا پارامتر
        val lunarDay = lunarDayOverride ?: run {
            val d = (pDate.shDay % 30)
            if (d == 0) 30 else d
        }

        // جدول سنتی جایگاه رجال الغیب در ۳۰ روز ماه قمری (بر پایه احکام جفر و نجوم ص ۱۴۰ کتاب):
        // روزهای ۱ و ۹: شرق
        // روزهای ۲ و ۱۰ و ۱۸: شمال شرق
        // روزهای ۳ و ۱۱ و ۱۹: شمال
        // روزهای ۴ و ۱۲ و ۲۰: شمال غرب
        // روزهای ۵ و ۱۳ و ۲۱: غرب
        // روزهای ۶ و ۱۴ و ۲۲: جنوب غرب
        // روزهای ۷ و ۱۵ و ۲۳: جنوب
        // روزهای ۸ و ۱۶ و ۲۴: جنوب شرق
        // ۲۵ تا ۳۰: تکرار ادواری
        val (direction, angle) = when (lunarDay % 8) {
            1 -> "شرق" to 90f
            2 -> "شمال‌شرقی" to 45f
            3 -> "شمال" to 0f
            4 -> "شمال‌غربی" to 315f
            5 -> "غرب" to 270f
            6 -> "جنوب‌غربی" to 225f
            7 -> "جنوب" to 180f
            else -> "جنوب‌شرقی" to 135f
        }

        val advice = "در روز $lunarDay ماه قمری، رجال‌الغیب در سمت «$direction» مستقر هستند. " +
                "بر طبق تعالیم صفحه ۱۴۰ کتاب مفتاح طلاسم کنوز، در هنگام انجام محاسبات جفری، کتابت وفق یا قرائت عزایم، " +
                "شایسته است پشت به جهت $direction و رو به قبله بنشینید تا از آفات و سنگینی مصون بمانید."

        return RijalAlGhaibInfo(
            lunarDay = lunarDay,
            directionName = direction,
            compassAngle = angle,
            sittingAdvice = advice,
            roussArbaa = ROUSS_ARBAA
        )
    }

    // -------------------------------------------------------------
    // ۴. توازن طبایع و حروف متمم خنثی‌کننده (صفحات ۱۳۲ تا ۱۳۹ کتاب)
    // -------------------------------------------------------------
    fun calculateTabayeMizan(text: String): TabayeMizanResult {
        val clean = cleanArabicText(text)

        val fireLetters = mutableListOf<Char>()
        val airLetters = mutableListOf<Char>()
        val waterLetters = mutableListOf<Char>()
        val earthLetters = mutableListOf<Char>()

        clean.forEach { ch ->
            when (ch) {
                'ا', 'ه', 'ط', 'م', 'ف', 'ش', 'ذ' -> fireLetters.add(ch)
                'ب', 'و', 'ی', 'ن', 'ص', 'ت', 'ض' -> airLetters.add(ch)
                'ج', 'ز', 'ک', 'س', 'ق', 'ث', 'ظ' -> waterLetters.add(ch)
                'د', 'ح', 'ل', 'ع', 'ر', 'خ', 'غ' -> earthLetters.add(ch)
            }
        }

        val counts = listOf(
            Element.FIRE to fireLetters.size,
            Element.AIR to airLetters.size,
            Element.WATER to waterLetters.size,
            Element.EARTH to earthLetters.size
        )
        val dominant = counts.maxByOrNull { it.second }?.first ?: Element.FIRE

        // قاعده تقابل طبایع در کتاب:
        // آتش متضاد با آب است (تعدیل آتش با آب)
        // باد/هوا متضاد با خاک است (تعدیل باد با خاک)
        val (counter, balancing) = when (dominant) {
            Element.FIRE -> Element.WATER to listOf('ج', 'ز', 'ک', 'س', 'ق', 'ث', 'ظ')
            Element.WATER -> Element.FIRE to listOf('ا', 'ه', 'ط', 'م', 'ف', 'ش', 'ذ')
            Element.AIR -> Element.EARTH to listOf('د', 'ح', 'ل', 'ع', 'ر', 'خ', 'غ')
            Element.EARTH -> Element.AIR to listOf('ب', 'و', 'ی', 'ن', 'ص', 'ت', 'ض')
        }

        val advice = "بر اساس دستور صفحه ۱۳۲ و ۱۳۸ کتاب مفتاح طلاسم کنوز: از آنجا که طالع این متن دارای غلبه «${dominant.label}» است، " +
                "جهت رفع حرارت یا خشکی مفرط و ایجاد اعتدال روحانی، استفاده از حروف متمم «${counter.label}» (${balancing.joinToString("، ")}) " +
                "و ممارست بر اسماء حسنای واجد این حروف در کنار وفق توصیه می‌شود."

        return TabayeMizanResult(
            fireLetters = fireLetters,
            airLetters = airLetters,
            waterLetters = waterLetters,
            earthLetters = earthLetters,
            dominantElement = dominant,
            counterElement = counter,
            balancingLetters = balancing,
            balancingAdvice = advice
        )
    }

    // -------------------------------------------------------------
    // ۵. مبدل خطوط و اقلام رمزی باستانی (صفحات ۶۱ تا ۸۹ کتاب)
    // -------------------------------------------------------------
    private val ANCIENT_SCRIPTS_MAP = mapOf(
        'ا' to Triple("الف", "𐤀", "𐡀 (آلف)"),
        'ب' to Triple("با", "𐤁", "𐡁 (بیت)"),
        'ج' to Triple("جیم", "𐤂", "𐡂 (گیمل)"),
        'د' to Triple("دال", "𐤃", "𐡃 (دالت)"),
        'ه' to Triple("ها", "𐤄", "𐡄 (هی)"),
        'و' to Triple("واو", "𐤅", "𐡅 (واو)"),
        'ز' to Triple("زا", "𐤆", "𐡆 (زاین)"),
        'ح' to Triple("حا", "𐤇", "𐡇 (حیت)"),
        'ط' to Triple("طا", "𐤈", "𐡈 (تیت)"),
        'ی' to Triple("یا", "𐤉", "𐡉 (یود)"),
        'ک' to Triple("کاف", "𐤊", "𐡊 (کاف)"),
        'ل' to Triple("لام", "𐤋", "𐡋 (لامد)"),
        'م' to Triple("میم", "𐤌", "𐡌 (مِم)"),
        'ن' to Triple("نون", "𐤍", "𐡍 (نون)"),
        'س' to Triple("سین", "𐤎", "𐡎 (سامخ)"),
        'ع' to Triple("عین", "𐤏", "𐡏 (عین)"),
        'ف' to Triple("فا", "𐤐", "𐡐 (پی)"),
        'ص' to Triple("صاد", "𐤑", "𐡑 (صادی)"),
        'ق' to Triple("قاف", "𐤒", "𐡒 (قوف)"),
        'ر' to Triple("را", "𐤓", "𐡓 (ریش)"),
        'ش' to Triple("شین", "𐤔", "𐡔 (شین)"),
        'ت' to Triple("تا", "𐤕", "𐡕 (تاو)"),
        'ث' to Triple("ثا", "𐤕𐤔", "𐡕 (تاو ثانی)"),
        'خ' to Triple("خا", "𐤇𐤊", "𐡇 (خیت)"),
        'ذ' to Triple("ذال", "𐤃𐤆", "𐡃 (دالت زای)"),
        'ض' to Triple("ضاد", "𐤑𐤃", "𐡑 (صاد معجمه)"),
        'ظ' to Triple("ظا", "𐤈𐤆", "𐡈 (طای مشاله)"),
        'غ' to Triple("غین", "𐤏𐤂", "𐡏 (عین معجمه)")
    )

    fun convertToAncientScript(text: String): AncientScriptResult {
        val clean = cleanArabicText(text)
        val sbSymbols = StringBuilder()
        val details = mutableListOf<Triple<Char, String, String>>()

        clean.forEach { ch ->
            val mapping = ANCIENT_SCRIPTS_MAP[ch]
            if (mapping != null) {
                sbSymbols.append(mapping.second).append(" ")
                details.add(Triple(ch, mapping.first, mapping.second))
            } else {
                sbSymbols.append(ch).append(" ")
            }
        }

        return AncientScriptResult(
            originalText = clean,
            symbolsText = sbSymbols.toString().trim(),
            details = details
        )
    }

    // -------------------------------------------------------------
    // ۶. اوفاق ۴x۴ و ۵x۵ پیشرفته با کسر (صفحه ۱۱۴ و ۲۱۸ کتاب)
    // -------------------------------------------------------------
    fun generateOfaqExtended(totalSum: Int, size: Int): OfaqExtendedResult {
        return when (size) {
            4 -> generate4x4Ofaq(totalSum)
            5 -> generate5x5Ofaq(totalSum)
            else -> {
                val r3 = JafrNumericalUtils.calculateNumericalJafr(totalSum.toString())
                OfaqExtendedResult(
                    size = 3,
                    title = "وفق مثلث ۳x۳ (بطن المجمع)",
                    totalSum = r3.totalAbjad,
                    miftah = r3.miftah,
                    maghlaq = r3.maghlaq,
                    wafd = r3.wafd,
                    adl = (r3.miftah + r3.maghlaq) / 2,
                    kasr = (r3.totalAbjad - 12) % 3,
                    kasrHouse = "خانه ۷ (در صورت وجود)",
                    matrix = r3.matrix3x3,
                    explanation = r3.explanation
                )
            }
        }
    }

    private fun generate4x4Ofaq(totalSum: Int): OfaqExtendedResult {
        val safeTotal = if (totalSum < 34) totalSum + 34 else totalSum
        // در وفق مربع ۴x۴: قاعده استخراج پایه = (مجموع - ۳۰) تقسیم بر ۴
        val base = (safeTotal - 30) / 4
        val kasr = (safeTotal - 30) % 4

        // خانه های ۱ تا ۱۶ با ترتیب مربع جادویی استاندارد خوارزمی و ابن عربی:
        // اگر کسر ۱ باشد، به خانه ۱۳ اضافه می‌شود؛ اگر ۲ باشد به خانه ۹؛ اگر ۳ باشد به خانه ۵
        val b = base
        val p = IntArray(17)
        for (i in 1..16) {
            var v = b + (i - 1)
            if (kasr == 1 && i >= 13) v += 1
            if (kasr == 2 && i >= 9) v += 1
            if (kasr == 3 && i >= 5) v += 1
            p[i] = v
        }

        // جدول ۱۶ خانه‌ای الگوی کلاسیک درر منظوم:
        // [16,  2,  3, 13]
        // [ 5, 11, 10,  8]
        // [ 9,  7,  6, 12]
        // [ 4, 14, 15,  1]
        val matrix = listOf(
            listOf(p[16], p[2], p[3], p[13]),
            listOf(p[5], p[11], p[10], p[8]),
            listOf(p[9], p[7], p[6], p[12]),
            listOf(p[4], p[14], p[15], p[1])
        )

        val miftah = p[1]
        val maghlaq = p[16]
        val adl = (miftah + maghlaq) / 2

        val kasrText = when (kasr) {
            1 -> "کسر ۱ وارد بر خانه ۱۳ (جهت جبران کسری ضلع)"
            2 -> "کسر ۲ وارد بر خانه ۹"
            3 -> "کسر ۳ وارد بر خانه ۵"
            else -> "بدون کسر (وفق تام و کامل)"
        }

        return OfaqExtendedResult(
            size = 4,
            title = "وفق مربع ۴x۴ (لوح چهار در چهار)",
            totalSum = safeTotal,
            miftah = miftah,
            maghlaq = maghlaq,
            wafd = safeTotal,
            adl = adl,
            kasr = kasr,
            kasrHouse = kasrText,
            matrix = matrix,
            explanation = "وفق مربع ۴x۴ با مجموع اضلاع $safeTotal بر اساس دستور ص ۱۱۴ و ۲۱۸ کتاب مفتاح طلاسم کنوز تنظیم شده است."
        )
    }

    private fun generate5x5Ofaq(totalSum: Int): OfaqExtendedResult {
        val safeTotal = if (totalSum < 65) totalSum + 65 else totalSum
        // در وفق مخمس ۵x۵: قاعده استخراج پایه = (مجموع - ۶۰) تقسیم بر ۵
        val base = (safeTotal - 60) / 5
        val kasr = (safeTotal - 60) % 5

        val p = IntArray(26)
        for (i in 1..25) {
            var v = base + (i - 1)
            if (kasr > 0 && i >= (26 - kasr * 5)) v += 1
            p[i] = v
        }

        // الگوی کلاسیک مخمس هندی / اسلامی:
        // [11, 24,  7, 20,  3]
        // [ 4, 12, 25,  8, 16]
        // [17,  5, 13, 21,  9]
        // [10, 18,  1, 14, 22]
        // [23,  6, 19,  2, 15]
        val matrix = listOf(
            listOf(p[11], p[24], p[7], p[20], p[3]),
            listOf(p[4], p[12], p[25], p[8], p[16]),
            listOf(p[17], p[5], p[13], p[21], p[9]),
            listOf(p[10], p[18], p[1], p[14], p[22]),
            listOf(p[23], p[6], p[19], p[2], p[15])
        )

        val miftah = p[1]
        val maghlaq = p[25]
        val adl = p[13] // قلب المخمس

        return OfaqExtendedResult(
            size = 5,
            title = "وفق مخمس ۵x۵ (لوح پنج در پنج)",
            totalSum = safeTotal,
            miftah = miftah,
            maghlaq = maghlaq,
            wafd = safeTotal,
            adl = adl,
            kasr = kasr,
            kasrHouse = if (kasr == 0) "بدون کسر (تام)" else "کسر $kasr وارد بر خانه‌های تکمیلی",
            matrix = matrix,
            explanation = "وفق مخمس ۵x۵ حاصل با قلب $adl و مجموع اضلاع $safeTotal بر طبق قواعد اوفاق کتاب مفتاح طلاسم کنوز."
        )
    }
}
