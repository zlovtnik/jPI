package com.churchapp.util

import arrow.core.Either
import arrow.core.left
import arrow.core.right

object EitherUtils {
    @JvmStatic
    fun <L, R> left(value: L): Either<L, R> = value.left()
    
    @JvmStatic
    fun <L, R> right(value: R): Either<L, R> = value.right()
}
