package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AutoModFilter
import com.example.data.AutoModSeverity
import com.example.data.MonetizationCalculator
import com.example.data.StreamCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TwitchFlex", appName)
    }

    @Test
    fun `verify monetization calculation tiers and zyro tax`() {
        // Less than 100 subs
        assertEquals(2.50, MonetizationCalculator.getPayoutPerSub(10), 0.01)
        // 100 to 10k
        assertEquals(5.00, MonetizationCalculator.getPayoutPerSub(500), 0.01)
        // Verified badge > 10k
        assertEquals(11.50, MonetizationCalculator.getPayoutPerSub(15000), 0.01)
        // Partner base > 100k
        assertEquals(33.50, MonetizationCalculator.getPayoutPerSub(150000), 0.01)
        // Official partner > 1M
        assertEquals(55.20, MonetizationCalculator.getPayoutPerSub(1500000), 0.01)
        // Fixed Zyro Tax
        assertEquals(2.30, MonetizationCalculator.ZYRO_FIXED_TAX, 0.01)
    }

    @Test
    fun `verify hourly earnings calculation`() {
        // Base: 2.50 euro every 4 min (240s)
        val earnings4Min = MonetizationCalculator.calculateHourlyEarnings(240, 50)
        assertEquals(2.50, earnings4Min, 0.01)

        // Over 100 viewers: 5.00 euro every 5 min (300s)
        val earnings5MinHighViewers = MonetizationCalculator.calculateHourlyEarnings(300, 150)
        assertEquals(5.00, earnings5MinHighViewers, 0.01)
    }

    @Test
    fun `verify stream categories exist`() {
        assertTrue(StreamCategory.ALL.isNotEmpty())
        assertTrue(StreamCategory.ALL.any { it.id == "irl" })
        assertTrue(StreamCategory.ALL.any { it.id == "trending" })
        assertTrue(StreamCategory.ALL.any { it.id == "giochi" })
        assertTrue(StreamCategory.ALL.any { it.id == "meccanico" })
        assertTrue(StreamCategory.ALL.any { it.id == "lavoro" })
        assertTrue(StreamCategory.ALL.any { it.id == "maratona" })
    }

    @Test
    fun `verify automod censorship with asterisks`() {
        val (filtered, censored) = AutoModFilter.filterMessage(
            text = "Sei un idiota totale",
            severity = AutoModSeverity.MEDIUM,
            customWords = emptySet()
        )
        assertTrue(censored)
        assertTrue(filtered.contains("******"))

        val (customFiltered, customCensored) = AutoModFilter.filterMessage(
            text = "Guarda questo spamlink adesso",
            severity = AutoModSeverity.LOW,
            customWords = setOf("spamlink")
        )
        assertTrue(customCensored)
        assertTrue(customFiltered.contains("********"))
    }
}
