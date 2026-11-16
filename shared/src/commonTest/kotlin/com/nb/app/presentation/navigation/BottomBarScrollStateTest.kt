package com.nb.app.presentation.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class BottomBarScrollStateTest {

    private val state = BottomBarScrollState().apply { heightPx = 100f }

    @Test
    fun scrollingTowardsTheEnd_hidesTheBarAsFarAsItsHeight() {
        state.onScroll(consumedY = -30f, availableY = 0f)
        assertEquals(30f, state.offsetPx)
        assertEquals(70f, state.visibleHeightPx)

        state.onScroll(consumedY = -500f, availableY = 0f)
        assertEquals(100f, state.offsetPx)
        assertEquals(0f, state.visibleHeightPx)
    }

    @Test
    fun scrollingBack_showsTheBar() {
        state.onScroll(consumedY = -100f, availableY = 0f)

        state.onScroll(consumedY = 40f, availableY = 0f)

        assertEquals(60f, state.offsetPx)
    }

    @Test
    fun reachingEitherEndOfTheList_bringsTheBarBack() {
        state.onScroll(consumedY = -100f, availableY = 0f)

        state.onScroll(consumedY = 0f, availableY = -60f)
        assertEquals(40f, state.offsetPx)

        state.onScroll(consumedY = 0f, availableY = 60f)
        assertEquals(0f, state.offsetPx)
    }

    @Test
    fun listThatDoesNotScroll_keepsTheBarVisible() {
        state.onScroll(consumedY = 0f, availableY = 0f)

        assertEquals(0f, state.offsetPx)
    }

    @Test
    fun releasingHalfway_snapsToTheClosestState() {
        state.onScroll(consumedY = -40f, availableY = 0f)
        assertEquals(0f, state.settleTarget())

        state.onScroll(consumedY = -20f, availableY = 0f)
        assertEquals(100f, state.settleTarget())
    }
}
