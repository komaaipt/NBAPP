package com.nb.app.core.di

import com.nb.app.core.network.createHttpClient
import com.nb.app.data.remote.NbaRemoteDataSource
import com.nb.app.data.repository.GameRepositoryImpl
import com.nb.app.data.repository.PlayerRepositoryImpl
import com.nb.app.data.repository.TeamRepositoryImpl
import com.nb.app.domain.repository.GameRepository
import com.nb.app.domain.repository.PlayerRepository
import com.nb.app.domain.repository.TeamRepository
import com.nb.app.presentation.home.HomeViewModel
import com.nb.app.presentation.players.PlayersViewModel
import com.nb.app.presentation.teamgames.TeamGamesViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Provides the platform [io.ktor.client.engine.HttpClientEngine] (OkHttp on Android, Darwin on iOS). */
internal expect val platformModule: Module

internal val networkModule: Module = module {
    single<HttpClient> { createHttpClient(engine = get()) }
}

internal val dataModule: Module = module {
    single { NbaRemoteDataSource(client = get()) }
    single<TeamRepository> { TeamRepositoryImpl(remoteDataSource = get()) }
    single<GameRepository> { GameRepositoryImpl(remoteDataSource = get()) }
    single<PlayerRepository> { PlayerRepositoryImpl(remoteDataSource = get()) }
}

internal val presentationModule: Module = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::PlayersViewModel)
    viewModel { params -> TeamGamesViewModel(teamId = params.get(), gameRepository = get()) }
}

internal val appModules: List<Module> =
    listOf(platformModule, networkModule, dataModule, presentationModule)
