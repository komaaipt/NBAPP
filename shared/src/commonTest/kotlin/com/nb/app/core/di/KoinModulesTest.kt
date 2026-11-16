package com.nb.app.core.di

import com.nb.app.domain.repository.GameRepository
import com.nb.app.domain.repository.PlayerRepository
import com.nb.app.domain.repository.TeamRepository
import com.nb.app.presentation.home.HomeViewModel
import com.nb.app.presentation.players.PlayersViewModel
import com.nb.app.presentation.teamgames.TeamGamesViewModel
import com.nb.app.testing.MainDispatcherTest
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.Job
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class KoinModulesTest : MainDispatcherTest() {

    private val mockEngineModule = module {
        single<HttpClientEngine> {
            MockEngine {
                respond(
                    content = """{ "data": [] }""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        }
    }

    private fun appWithMockEngine() = koinApplication {
        modules(networkModule, dataModule, presentationModule, mockEngineModule)
    }

    @Test
    fun everyDependencyResolves() {
        val app = appWithMockEngine()
        val koin = app.koin

        koin.get<TeamRepository>()
        koin.get<GameRepository>()
        koin.get<PlayerRepository>()
        koin.get<HomeViewModel>()
        koin.get<PlayersViewModel>()
        koin.get<TeamGamesViewModel> { parametersOf(1) }

        assertSame(koin.get<TeamRepository>(), koin.get<TeamRepository>())
        app.close()
    }

    @Test
    fun platformModule_providesAnEngine() {
        val app = koinApplication { modules(platformModule) }

        assertNotNull(app.koin.get<HttpClientEngine>())
        app.close()
    }

    @Test
    fun appModule_includesEveryModule() {
        val app = koinApplication { modules(appModule) }

        assertNotNull(app.koin.getOrNull<HttpClient>())
        app.close()
    }

    @Test
    fun closingKoin_closesTheHttpClient() {
        val app = appWithMockEngine()
        val client = app.koin.get<HttpClient>()

        app.close()

        assertFalse(client.coroutineContext[Job]!!.isActive)
    }
}
