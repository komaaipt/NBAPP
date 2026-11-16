package com.nb.app.presentation.players

import androidx.paging.PagingSource.LoadResult
import androidx.paging.testing.TestPager
import com.nb.app.domain.model.Page
import com.nb.app.domain.model.Player
import com.nb.app.testing.TestData.warriors
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class PlayersPagingSourceTest {

    private val repository = FakePlayerRepository()
    private val pager = TestPager(PlayersPagingConfig, PlayersPagingSource(query = "curry", playerRepository = repository))

    private fun player(id: Int) = Player(id, "Player$id", "Curry", warriors)

    @Test
    fun pages_followTheApiCursor() = runTest {
        repository.results["curry" to null] = Result.success(Page(listOf(player(1)), nextCursor = 25))
        repository.results["curry" to 25] = Result.success(Page(listOf(player(2)), nextCursor = null))

        val first = assertIs<LoadResult.Page<Int, Player>>(pager.refresh())
        assertEquals(25, first.nextKey)

        val second = assertIs<LoadResult.Page<Int, Player>>(pager.append())
        assertEquals(listOf(2), second.data.map { it.id })
        assertNull(second.nextKey)
        assertEquals(listOf<Pair<String, Int?>>("curry" to null, "curry" to 25), repository.requests)
    }
}
