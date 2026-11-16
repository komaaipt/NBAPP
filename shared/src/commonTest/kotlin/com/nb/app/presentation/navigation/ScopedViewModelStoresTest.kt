package com.nb.app.presentation.navigation

import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class ScopedViewModelStoresTest {

    @Test
    fun sameKey_sharesTheStoreUntilCleared() {
        val stores = ScopedViewModelStores()

        val first = stores.ownerFor("team_games_1").viewModelStore
        assertSame(first, stores.ownerFor("team_games_1").viewModelStore)
        assertNotSame(first, stores.ownerFor("team_games_2").viewModelStore)

        stores.clear("team_games_1")
        assertNotSame(first, stores.ownerFor("team_games_1").viewModelStore)
    }
}
