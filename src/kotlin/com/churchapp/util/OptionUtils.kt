package com.churchapp.util

import arrow.core.Option
import arrow.core.some
import arrow.core.none as arrowNone

object OptionUtils {
    @JvmStatic
    fun <T> some(value: T): Option<T> = value.some()

    @JvmStatic
    fun <T> none(): Option<T> = arrowNone()

    @JvmStatic
    fun <T> of(value: T?): Option<T> = Option.fromNullable(value)
}
