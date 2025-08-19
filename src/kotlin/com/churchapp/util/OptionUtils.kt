package com.churchapp.util

import arrow.core.Option
import arrow.core.none
import arrow.core.some

object OptionUtils {
    @JvmStatic
    fun <T> some(value: T): Option<T> = value.some()
    
    @JvmStatic
    fun <T> none(): Option<T> = none()
    
    @JvmStatic
    fun <T> of(value: T?): Option<T> = Option.fromNullable(value)
}
