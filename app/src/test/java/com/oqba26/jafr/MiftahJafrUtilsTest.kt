package com.oqba26.jafr

import com.oqba26.jafr.util.MiftahJafrUtils
import org.junit.Assert.*
import org.junit.Test

class MiftahJafrUtilsTest {

    @Test
    fun testExtractMuwakkil() {
        val res = MiftahJafrUtils.extractMuwakkil("حسین", "فاطمه")
        assertNotNull(res)
        assertTrue("نام موکل علوی باید با ائیل پایان یابد", res.muwakkilAlavi.endsWith("ائیل"))
        assertTrue("نام موکل سفلی باید با یوش پایان یابد", res.muwakkilSifli.endsWith("یوش"))
        assertTrue("عدد ابجد موکل باید مثبت باشد", res.abjadCount > 0)
        assertTrue("متن عزیمت باید شامل نام موکل باشد", res.azimatText.contains(res.muwakkilAlavi))
        assertFalse("بخور نباید خالی باشد", res.bukhoor.isEmpty())
    }

    @Test
    fun testKawakibHours() {
        val info = MiftahJafrUtils.calculateCurrentKawakibHour()
        assertNotNull(info)
        assertEquals(12, info.dayHoursList.size)
        assertEquals(12, info.nightHoursList.size)
        assertTrue("شماره ساعت باید بین ۱ تا ۱۲ باشد", info.currentHourIndex in 1..12)
        assertFalse("نام کوکب حاکم نباید خالی باشد", info.rulerKawkab.isEmpty())
    }

    @Test
    fun testRijalAlGhaib() {
        for (day in 1..30) {
            val r = MiftahJafrUtils.calculateRijalAlGhaib(day)
            assertEquals(day, r.lunarDay)
            assertTrue("زاویه باید بین ۰ تا ۳۶۰ باشد", r.compassAngle in 0f..360f)
            assertFalse("جهت نباید خالی باشد", r.directionName.isEmpty())
            assertEquals(4, r.roussArbaa.size)
        }
    }

    @Test
    fun testTabayeMizan() {
        // متن با حروف آتشی: ا ه ط م
        val res = MiftahJafrUtils.calculateTabayeMizan("اهطم")
        assertEquals(Element.FIRE, res.dominantElement)
        assertEquals(Element.WATER, res.counterElement)
        assertTrue("حروف متمم آتش باید آبی باشند", res.balancingLetters.contains('ج'))
    }

    @Test
    fun testAncientScriptConversion() {
        val res = MiftahJafrUtils.convertToAncientScript("علی")
        assertFalse(res.symbolsText.isEmpty())
        assertTrue("باید شامل نماد کهن باشد", res.symbolsText.contains("𐤏"))
    }

    @Test
    fun testExtendedOfaq4x4() {
        val total = 100
        val ofaq4 = MiftahJafrUtils.generateOfaqExtended(total, 4)
        assertEquals(4, ofaq4.matrix.size)
        assertEquals(4, ofaq4.matrix[0].size)
        // بررسی مجموع ردیف اول
        val row1Sum = ofaq4.matrix[0].sum()
        assertEquals("مجموع ردیف ۱ باید برابر با مجموع هدف باشد", total, row1Sum)
    }

    @Test
    fun testExtendedOfaq5x5() {
        val total = 150
        val ofaq5 = MiftahJafrUtils.generateOfaqExtended(total, 5)
        assertEquals(5, ofaq5.matrix.size)
        assertEquals(5, ofaq5.matrix[0].size)
        val row1Sum = ofaq5.matrix[0].sum()
        assertEquals("مجموع ردیف ۱ باید برابر با مجموع هدف باشد", total, row1Sum)
    }
}
