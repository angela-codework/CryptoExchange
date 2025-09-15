package com.demo.feature_market.core.di

import com.demo.core.di.RestClient
import com.demo.core.di.RetrofitMarket
import com.demo.feature_market.core.common.Constants
import com.demo.feature_market.data.remote.api.MarketApi
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    @RetrofitMarket
    fun provideMarketRetrofit(@RestClient okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_BTSE_SERVER_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideMarketApi(@RetrofitMarket retrofit: Retrofit): MarketApi {
        return retrofit.create(MarketApi::class.java)
    }
}