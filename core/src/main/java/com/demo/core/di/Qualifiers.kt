package com.demo.core.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RestClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WsClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RetrofitMarket