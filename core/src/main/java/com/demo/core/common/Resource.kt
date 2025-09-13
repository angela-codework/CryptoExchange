package com.demo.core.common

sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Fail<T>(val msg: String) : Resource<T>()
}