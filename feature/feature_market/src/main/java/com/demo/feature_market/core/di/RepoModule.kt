package com.demo.feature_market.core.di

import com.demo.feature_market.data.remote.api.MarketApi
import com.demo.feature_market.data.remote.api.WebSocketClient
import com.demo.feature_market.data.repository.MarketRepositoryImpl
import com.demo.feature_market.domain.repository.MarketRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {
    @Provides
    @Singleton
    fun provideMarketRepository(marketApi: MarketApi, webSocketClient: WebSocketClient): MarketRepository =
        MarketRepositoryImpl(marketApi, webSocketClient)
}