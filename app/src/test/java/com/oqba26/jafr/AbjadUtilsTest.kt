package com.oqba26.jafr

import saman.zamani.persiandate.PersianDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AbjadUtilsTest {

    private val qWork = "آیا علی زاده زهرا در کار خود موفق میشود؟"
    private val qCurse = "آیا محمد زاده مریم طلسم شده است؟"
    private val qIllness = "آیا حسن زاده فاطمه مریض است؟"
    private val qLost = "آیا رضا زاده زینب چیزی گم شده است؟"
    private val qWorkInsistent = "آیا علی زاده زهرا حتماً در کار خود موفق میشود؟"

    private val verdictPositive = "بله — منزل سعد و حروف مستحصله غالباً سعدند. با اطمینان پیگیری کنید."
    private val verdictCautiousPositive = "بله ولی با احتیاط — منزل سعد است اما حروف نحس سنگینی دارند؛ صبر و دقت کنید."
    private val verdictBalanced = "بله — منزل سعد است و حروف متعادل؛ نتیجه خوب خواهد بود."
    private val mixedVerdictPrefix = "پاسخ مردد — قرائن سه مسیر همسو نیستند"

    // ─── جمع ابجد ───

    @Test
    fun `جمع ابجد کبیر سوالات نمونه`() {
        assertEquals(1973, AbjadUtils.calculate(qWork, AbjadType.KABIR).total)
        assertEquals(1320, AbjadUtils.calculate(qCurse, AbjadType.KABIR).total)
        assertEquals(1793, AbjadUtils.calculate(qIllness, AbjadType.KABIR).total)
        assertEquals(1959, AbjadUtils.calculate(qLost, AbjadType.KABIR).total)
        // «حتماً» = ح(8) + ت(400) + م(40) + ا(1) = 449
        assertEquals(2422, AbjadUtils.calculate(qWorkInsistent, AbjadType.KABIR).total)
    }

    @Test
    fun `انواع ابجد - کبیر صغیر و وسایط`() {
        assertEquals(1000, AbjadUtils.calculate("غ", AbjadType.KABIR).total)
        assertEquals(4, AbjadUtils.calculate("غ", AbjadType.SAGHIR).total)
        assertEquals(1, AbjadUtils.calculate("غ", AbjadType.WASAIT).total)
        assertEquals(4, AbjadUtils.calculate("م", AbjadType.SAGHIR).total)
        assertEquals(4, AbjadUtils.calculate("م", AbjadType.WASAIT).total)
        assertEquals(8, AbjadUtils.calculate("ک", AbjadType.SAGHIR).total)
        assertEquals(9, AbjadUtils.calculate("ط", AbjadType.SAGHIR).total)
        assertEquals(2, AbjadUtils.calculate("ب", AbjadType.JAFR_15).total)
    }

    // ─── سیستم تقسیمات جفری ───

    @Test
    fun `تقسیمات جفری - منازل قمر سوالات نمونه`() {
        assertTrue(AbjadUtils.calculateJafr15(qWork).answer.contains("منزل قمر (÷۲۸): سماک"))
        assertTrue(AbjadUtils.calculateJafr15(qCurse).answer.contains("منزل قمر (÷۲۸): هقعه"))
        assertTrue(AbjadUtils.calculateJafr15(qIllness).answer.contains("منزل قمر (÷۲۸): بطین"))
        assertTrue(AbjadUtils.calculateJafr15(qLost).answer.contains("منزل قمر (÷۲۸): رشا"))
        assertTrue(AbjadUtils.calculateJafr15(qWorkInsistent).answer.contains("منزل قمر (÷۲۸): غفر"))
    }

    @Test
    fun `تقسیمات جفری - جمع و شمارش سعد نحس`() {
        val aWork = AbjadUtils.calculateJafr15(qWork).answer
        assertTrue(aWork.contains("جمع ابجد کبیر: ۱۹۷۳"))
        assertTrue(aWork.contains("حروف سعد: ۱۹ | حروف نحس: ۱۲"))

        val aCurse = AbjadUtils.calculateJafr15(qCurse).answer
        assertTrue(aCurse.contains("جمع ابجد کبیر: ۱۳۲۰"))
        assertTrue(aCurse.contains("حروف سعد: ۱۴ | حروف نحس: ۱۱"))

        val aIllness = AbjadUtils.calculateJafr15(qIllness).answer
        assertTrue(aIllness.contains("جمع ابجد کبیر: ۱۷۹۳"))
        assertTrue(aIllness.contains("حروف سعد: ۱۰ | حروف نحس: ۱۲"))

        val aLost = AbjadUtils.calculateJafr15(qLost).answer
        assertTrue(aLost.contains("جمع ابجد کبیر: ۱۹۵۹"))
        assertTrue(aLost.contains("حروف سعد: ۱۴ | حروف نحس: ۱۲"))

        val aInsistent = AbjadUtils.calculateJafr15(qWorkInsistent).answer
        assertTrue(aInsistent.contains("جمع ابجد کبیر: ۲۴۲۲"))
        assertTrue(aInsistent.contains("حروف سعد: ۲۲ | حروف نحس: ۱۳"))
    }

    @Test
    fun `تقسیمات جفری - حکم نطق`() {
        assertEquals(verdictPositive, extractVerdict(AbjadUtils.calculateJafr15(qWork).answer))
        // سه مسیر طلسم هم‌سو نیستند → پاسخ مردد با ذکر دلیل
        assertTrue(extractVerdict(AbjadUtils.calculateJafr15(qCurse).answer).startsWith(mixedVerdictPrefix))
        assertEquals(verdictCautiousPositive, extractVerdict(AbjadUtils.calculateJafr15(qIllness).answer))
        assertEquals(verdictPositive, extractVerdict(AbjadUtils.calculateJafr15(qLost).answer))
        // افزودن «حتماً» دیگر جواب را از «بله» به «نه» برنمی‌گرداند
        assertEquals(verdictPositive, extractVerdict(AbjadUtils.calculateJafr15(qWorkInsistent).answer))
    }

    @Test
    fun `سیستم جدید - تعیینگر بودن و حذف قالب های کلیدواژه ای`() {
        // همان سوال، همان جواب
        assertEquals(
            AbjadUtils.calculateJafr15(qLost).answer,
            AbjadUtils.calculateJafr15(qLost).answer
        )
        // قالب‌های قدیمی و کلیدواژه‌ای دیگر وجود ندارند
        assertFalse(AbjadUtils.calculateJafr15(qLost).answer.contains("سارق"))
        assertFalse(AbjadUtils.calculateJafr15(qLost).answer.contains("طالع نیت"))
        assertFalse(AbjadUtils.calculateJafr15(qCurse).answer.contains("در اوج"))
        assertFalse(AbjadUtils.calculateJafr15(qIllness).answer.contains("نذر نان"))
    }

    @Test
    fun `سطر های جفر ۱۵ - پنج سطر ثابت`() {
        val result = AbjadUtils.calculateJafr15(qWork)
        assertEquals(5, result.rows.size)
        assertEquals("سطر اول: اساس (حروف سوال)", result.rows[0].title)
        assertEquals("سطر دوم: نظیره (ابجدی)", result.rows[1].title)
        assertEquals("سطر سوم: تکسیر (صدر و مؤخر)", result.rows[2].title)
        assertEquals("سطر چهارم: ملفوظی (باطن حروف)", result.rows[3].title)
        assertEquals("سطر نهایی: مستحصله (استخراج نطق)", result.rows[4].title)
    }

    @Test
    fun `تقسیمات جفری - داده ساختار یافته`() {
        val t = AbjadUtils.calculateJafr15(qWork).taqsimat
        assertNotNull(t)
        assertEquals(1973, t!!.total)
        assertEquals("شرق", t.direction)
        assertEquals("جمعه", t.day)
        assertEquals("زهره", t.kawkab)
        assertEquals("اسد", t.burj)
        assertEquals("سماک", t.manzel.name)
        assertEquals("بلندی و رفعت", t.manzel.meaning)
        assertEquals("سعد", t.manzel.disposition)
        assertEquals(19, t.saadCount)
        assertEquals(12, t.nahsCount)
        assertEquals("سعد", t.dominant)
        assertEquals(verdictPositive, t.verdict)
        // قرائن سه مسیر
        val cross = t.cross
        assertNotNull(cross)
        assertEquals(3, cross!!.paths.size)
        assertEquals("جمع سوال", cross.paths[0].label)
        assertEquals("مستحصله", cross.paths[1].label)
        assertEquals("عدد حروف", cross.paths[2].label)
        assertEquals("سماک", cross.paths[0].manzel.name)
        assertEquals("ذراع", cross.paths[1].manzel.name)
        assertEquals("دبران", cross.paths[2].manzel.name)
        assertEquals("بله", cross.consensus)
        assertEquals("نسبتاً همسو", cross.confidence)
        assertEquals(2, cross.agreeCount)
        assertEquals(3, cross.nadhiraPolarities.size)
        // سوال خالی داده ساختار یافته ندارد
        assertNull(AbjadUtils.calculateJafr15("").taqsimat)
    }

    @Test
    fun `تحلیل طلسم - سوال طلسم شده`() {
        val spell = AbjadUtils.calculateJafr15(qCurse).taqsimat?.spell
        assertNotNull(spell)
        assertEquals(2, spell!!.score)
        assertEquals("متوسط", spell.level)
        assertEquals(2, spell.indicators.size)
        assertTrue(spell.indicators[0].contains("برج سوال"))
        assertTrue(spell.indicators[1].contains("تکرار"))
        assertEquals("ایمحدزهرطلسشت", spell.khalesLetters)
        assertEquals("سنبله", spell.factorBurj)
        assertEquals("خاکی", spell.factorElement)
        assertEquals("مونث", spell.factorGender)
        assertEquals("مریخ", spell.factorKawkab)
        assertEquals("سه‌شنبه", spell.factorDay)
        assertTrue(spell.factorRelation.contains("هم‌طبع"))
        assertTrue(spell.verdict.contains("متوسط"))
        // در متن گزارش هم بخش طلسم هست
        assertTrue(AbjadUtils.calculateJafr15(qCurse).answer.contains("تحلیل طلسم / سحر:"))
        assertTrue(AbjadUtils.calculateJafr15(qCurse).answer.contains("حکم طلسم:"))
    }

    @Test
    fun `تحلیل طلسم - سوالات غیر طلسم`() {
        assertNull(AbjadUtils.calculateJafr15(qWork).taqsimat?.spell)
        assertNull(AbjadUtils.calculateJafr15(qIllness).taqsimat?.spell)
        assertNull(AbjadUtils.calculateJafr15(qLost).taqsimat?.spell)
    }

    @Test
    fun `طالع شخص از نام و نام مادر`() {
        val p = AbjadUtils.calculateJafr15(qWork).taqsimat?.person
        assertNotNull(p)
        assertEquals("علی", p!!.firstName)
        assertEquals("زهرا", p.motherName)
        assertEquals("دلو", p.burj)
        assertEquals("بادی", p.element)
        assertEquals("شمس", p.kawkab)
        assertEquals("یکشنبه", p.day)

        val p2 = AbjadUtils.calculateJafr15(qCurse).taqsimat?.person
        assertEquals("جدی", p2!!.burj)
        assertEquals("خاکی", p2.element)
        assertEquals("عطارد", p2.kawkab)

        // در متن گزارش هم هست
        assertTrue(AbjadUtils.calculateJafr15(qWork).answer.contains("طالع شخص (علی زاده زهرا): برج دلو | طبع بادی | کوکب شمس"))
    }

    @Test
    fun `کوکب سوال`() {
        assertTrue(AbjadUtils.calculateJafr15(qWork).answer.contains("کوکب سوال (÷۷): زهره"))
        assertTrue(AbjadUtils.calculateJafr15(qCurse).answer.contains("کوکب سوال (÷۷): عطارد"))
    }

    @Test
    fun `استخراج نام از متن`() {
        assertEquals(Pair("علی", "زهرا"), AbjadUtils.extractNames("آیا علی زاده زهرا طلسم هست؟"))
        assertEquals(Pair("علی", "زهرا"), AbjadUtils.extractNames("آیا علی بن زهرا طلسم هست؟"))
        assertEquals(Pair("علی", "زهرا"), AbjadUtils.extractNames("آیا علی ابن زهرا طلسم هست؟"))
        assertEquals(Pair("علی", "زهرا"), AbjadUtils.extractNames("آیا علی فرزند زهرا طلسم هست؟"))
        assertEquals(Pair(null, null), AbjadUtils.extractNames("آیا باران می‌بارد؟"))
    }

    // ─── ماژول‌های موضوعی ───

    @Test
    fun `ماژول ازدواج - تشخیص و دلایل سنتی`() {
        val q = "آیا علی زاده زهرا با مریم ازدواج می‌کند؟"
        val t = AbjadUtils.calculateJafr15(q).taqsimat
        assertNotNull(t)
        val marriage = t!!.topics.firstOrNull { it.topic == "ازدواج" }
        assertNotNull(marriage)
        assertTrue(marriage!!.indicators.isNotEmpty())
        assertTrue(marriage.highlights.any { it.first == "کوکب" && it.second == "زحل" })
        assertTrue(marriage.highlights.any { it.first == "برج" && it.second == "دلو" })
        assertEquals("متعادل", marriage.level)
        // در متن گزارش هم بخش موضوع هست و قبل از حکم نطق
        val answer = AbjadUtils.calculateJafr15(q).answer
        assertTrue(answer.contains("تحلیل موضوع: ازدواج"))
        assertTrue(answer.contains("نتیجه ازدواج:"))
        assertTrue(answer.indexOf("تحلیل موضوع: ازدواج") < answer.indexOf("حکم نطق:"))
    }

    @Test
    fun `ماژول سفر - تشخیص جهت و روز سفر`() {
        val q = "آیا علی زاده زهرا به مشهد سفر کند؟"
        val t = AbjadUtils.calculateJafr15(q).taqsimat
        assertNotNull(t)
        val travel = t!!.topics.firstOrNull { it.topic == "سفر" }
        assertNotNull(travel)
        assertEquals("سعد", travel!!.level)
        assertTrue(travel.highlights.any { it.first == "جهت سفر" && it.second == "جنوب" })
        assertTrue(travel.highlights.any { it.first == "روز حرکت" && it.second == "دوشنبه" })
        assertTrue(AbjadUtils.calculateJafr15(q).answer.contains("جهت سفر: جنوب"))
    }

    @Test
    fun `ماژول فرزند - تشخیص و دلایل`() {
        val q = "آیا مریم زاده فاطمه فرزنددار می‌شود؟"
        val t = AbjadUtils.calculateJafr15(q).taqsimat
        assertNotNull(t)
        val child = t!!.topics.firstOrNull { it.topic == "فرزند" }
        assertNotNull(child)
        assertEquals("سعد", child!!.level)
        assertTrue(child.indicators.any { it.contains("سرطان") })
        assertTrue(child.indicators.any { it.contains("قمر") })
        assertTrue(AbjadUtils.calculateJafr15(q).answer.contains("تحلیل موضوع: فرزند"))
    }

    @Test
    fun `ماژول کسب و کار - سوال کار موفق`() {
        val t = AbjadUtils.calculateJafr15(qWork).taqsimat
        assertNotNull(t)
        val biz = t!!.topics.firstOrNull { it.topic == "کسب‌وکار" }
        assertNotNull(biz)
        assertEquals("سعد", biz!!.level)
        assertTrue(AbjadUtils.calculateJafr15(qWork).answer.contains("تحلیل موضوع: کسب‌وکار"))
        // کسب‌وکار روی حکم نطق تأثیری ندارد
        assertEquals(verdictPositive, extractVerdict(AbjadUtils.calculateJafr15(qWork).answer))
    }

    @Test
    fun `ماژول بیماری - تشخیص و توصیه پزشکی`() {
        val t = AbjadUtils.calculateJafr15(qIllness).taqsimat
        assertNotNull(t)
        val illness = t!!.topics.firstOrNull { it.topic == "بیماری و درمان" }
        assertNotNull(illness)
        assertEquals("سعد", illness!!.level)
        assertTrue(illness.indicators.any { it.contains("شمس") })
        assertTrue(illness.verdict.contains("پزشک") || illness.verdict.contains("درمان"))
        assertNotNull(illness.notice)
        assertTrue(illness.notice!!.contains("پزشک"))
        val answer = AbjadUtils.calculateJafr15(qIllness).answer
        assertTrue(answer.contains("تحلیل موضوع: بیماری و درمان"))
        assertTrue(answer.indexOf("تحلیل موضوع: بیماری و درمان") < answer.indexOf("حکم نطق:"))
    }

    @Test
    fun `ماژول بیماری - قرائن نحس در سوال ناخوشی`() {
        val q = "آیا علی زاده زهرا تب شدید دارد؟"
        val illness = AbjadUtils.calculateJafr15(q).taqsimat!!.topics.firstOrNull { it.topic == "بیماری و درمان" }
        assertNotNull(illness)
        assertTrue(illness!!.indicators.isNotEmpty())
        assertTrue(illness.level in setOf("سعد", "نحس", "متعادل"))
        assertTrue(illness.verdict.contains("پزشک") || illness.verdict.contains("درمان"))
    }

    @Test
    fun `ماژول معامله - تشخیص و سطح`() {
        val q = "آیا علی زاده زهرا این خانه را بخرد؟"
        val t = AbjadUtils.calculateJafr15(q).taqsimat
        assertNotNull(t)
        val txn = t!!.topics.firstOrNull { it.topic == "خرید و فروش" }
        assertNotNull(txn)
        assertEquals("سعد", txn!!.level)
        assertTrue(txn.indicators.any { it.contains("عطارد") })
        assertTrue(txn.highlights.any { it.first == "کوکب" && it.second == "عطارد" })
        assertTrue(txn.verdict.startsWith("بله"))
        assertNotNull(txn.notice)
        assertTrue(txn.notice!!.contains("کارشناسی"))
        val answer = AbjadUtils.calculateJafr15(q).answer
        assertTrue(answer.contains("تحلیل موضوع: خرید و فروش"))
        assertTrue(answer.indexOf("تحلیل موضوع: خرید و فروش") < answer.indexOf("حکم نطق:"))
    }

    @Test
    fun `ماژول معامله - سوال ملک و ماشین`() {
        val t1 = AbjadUtils.calculateJafr15("آیا علی زاده زهرا این ملک را بخرد؟").taqsimat!!
        assertTrue(t1.topics.any { it.topic == "خرید و فروش" })
        val t2 = AbjadUtils.calculateJafr15("آیا علی زاده زهرا این ماشین را بخرد؟").taqsimat!!
        assertTrue(t2.topics.any { it.topic == "خرید و فروش" })
        val t3 = AbjadUtils.calculateJafr15("آیا قیمت این خانه منصفانه است؟").taqsimat!!
        val txn = t3.topics.firstOrNull { it.topic == "خرید و فروش" }
        assertNotNull(txn)
        assertTrue(txn!!.indicators.any { it.contains("مشتری") })
    }

    @Test
    fun `ماژول معامله - عدم تشخیص در سوال کسب و کار`() {
        val t = AbjadUtils.calculateJafr15(qWork).taqsimat!!
        assertTrue(t.topics.any { it.topic == "کسب‌وکار" })
        assertTrue(t.topics.none { it.topic == "خرید و فروش" })
    }

    @Test
    fun `ماژول های موضوعی - عدم تشخیص در سوالات بی ربط`() {
        assertTrue(AbjadUtils.calculateJafr15(qCurse).taqsimat!!.topics.isEmpty())
        assertTrue(AbjadUtils.calculateJafr15(qLost).taqsimat!!.topics.isEmpty())
        assertTrue(AbjadUtils.calculateJafr15("آیا فردا باران می‌بارد؟").taqsimat!!.topics.isEmpty())
    }

    @Test
    fun `ماژول های موضوعی - تشخیص چند موضوع در یک سوال`() {
        val q = "آیا علی زاده زهرا در سفر با مریم ازدواج می‌کند؟"
        val topics = AbjadUtils.calculateJafr15(q).taqsimat!!.topics
        assertTrue(topics.any { it.topic == "ازدواج" })
        assertTrue(topics.any { it.topic == "سفر" })
    }

    @Test
    fun `سوال خالی`() {
        val result = AbjadUtils.calculateJafr15("")
        assertEquals("سوال خالی است", result.answer)
        assertTrue(result.rows.isEmpty())
    }

    @Test
    fun `اعداد فارسی`() {
        assertEquals("۱۹۷۳", AbjadUtils.toPersianNumber(1973))
        assertEquals("۰", AbjadUtils.toPersianNumber(0))
        assertEquals("۱۳:۰۵", AbjadUtils.toPersianNumber("13:05"))
    }

    // ─── هنجارسازی، قرائن سه مسیر، قرینه زمان، وزن‌دهی شفاف ───

    @Test
    fun `هنجارسازی املا - هم ارزی شکل های مختلف حروف`() {
        // ي/ى با ی، ك با ک، آ با ا هم‌ارزند
        assertEquals(
            AbjadUtils.calculate("علی", AbjadType.KABIR).total,
            AbjadUtils.calculate("علي", AbjadType.KABIR).total
        )
        assertEquals(
            AbjadUtils.calculate("مکتب", AbjadType.KABIR).total,
            AbjadUtils.calculate("مكتب", AbjadType.KABIR).total
        )
        assertEquals(
            AbjadUtils.calculate("آیا", AbjadType.KABIR).total,
            AbjadUtils.calculate("ايا", AbjadType.KABIR).total
        )
        // همان سوال با املای عربی، جواب یکسان می‌دهد
        val a = AbjadUtils.calculateJafr15("آیا علی زاده زهرا طلسم شده است؟")
        val b = AbjadUtils.calculateJafr15("ايا علي زاده زهرا طلسم شده است؟")
        assertEquals(a.taqsimat!!.total, b.taqsimat!!.total)
        assertEquals(a.answer, b.answer)
    }

    @Test
    fun `قرائن سه مسیر - بیماری هر سه مسیر همسوست`() {
        val c = AbjadUtils.calculateJafr15(qIllness).taqsimat!!.cross
        assertNotNull(c)
        assertEquals("بله", c!!.consensus)
        assertEquals("همسو", c.confidence)
        assertEquals(3, c.agreeCount)
        assertTrue(c.paths.all { it.polarity == "بله" })
        assertTrue(AbjadUtils.calculateJafr15(qIllness).answer.contains("قرائن سه مسیر:"))
    }

    @Test
    fun `قرینه زمان - با تاریخ ثابت`() {
        val pdate = PersianDate(1700000000000L)
        pdate.setHour(14)
        val time = AbjadUtils.calculateJafr15(qWork, NadhiraType.ABJAD, pdate).taqsimat?.time
        assertNotNull(time)
        assertTrue(time!!.weekday.isNotBlank())
        assertTrue(time.dayKawkab in setOf("زحل", "شمس", "قمر", "مریخ", "عطارد", "مشتری", "زهره"))
        assertTrue(time.hourKawkab in setOf("زحل", "شمس", "قمر", "مریخ", "عطارد", "مشتری", "زهره"))
        assertTrue(time.note.isNotBlank())
        assertTrue(AbjadUtils.calculateJafr15(qWork, NadhiraType.ABJAD, pdate).answer.contains("قرینه زمان"))
        // بدون تاریخ، قرینه زمان null است
        assertNull(AbjadUtils.calculateJafr15(qWork).taqsimat?.time)
    }

    @Test
    fun `وزن دهی شفاف - وزن در متن شاخص ها`() {
        val biz = AbjadUtils.calculateJafr15(qWork).taqsimat!!.topics.first { it.topic == "کسب‌وکار" }
        assertTrue(biz.indicators.isNotEmpty())
        assertTrue(biz.indicators.all { it.contains("(+1)") || it.contains("(-1)") || it.contains("(+2)") || it.contains("(-2)") })
        assertTrue(biz.indicators.any { it.endsWith("(+1)") })

        val qMarriage = "آیا علی زاده زهرا با مریم ازدواج می‌کند؟"
        val marriage = AbjadUtils.calculateJafr15(qMarriage).taqsimat!!.topics.first { it.topic == "ازدواج" }
        assertTrue(marriage.indicators.any { it.contains("زحل") && it.endsWith("(-2)") })
    }

    private fun extractVerdict(answer: String): String {
        return answer.substringAfter("حکم نطق: ")
    }
}
