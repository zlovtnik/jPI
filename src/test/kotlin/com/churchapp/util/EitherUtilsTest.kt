package com.churchapp.util

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("EitherUtils")
class EitherUtilsTest {

    @Nested
    @DisplayName("left(value)")
    inner class LeftCreation {

        @Test
        @DisplayName("creates an Either.Left with the provided value (happy path)")
        fun createsLeft() {
            val e: Either<String, Int> = EitherUtils.left("error")
            assertTrue(e.isLeft(), "Expected isLeft() to be true")
            assertFalse(e.isRight(), "Expected isRight() to be false")
            e.fold(
                { l -> assertEquals("error", l) },
                { _ -> fail("Expected Left but was Right") }
            )
        }

        @Test
        @DisplayName("supports generic types and preserves the left type parameter")
        fun genericTypes() {
            data class Err(val code: Int, val message: String)
            val err = Err(404, "Not Found")
            val e: Either<Err, String> = EitherUtils.left(err)

            assertTrue(e.isLeft())
            e.fold(
                { l -> assertEquals(err, l) },
                { _ -> fail("Expected Left but was Right") }
            )
        }

        @Test
        @DisplayName("Left instances do not accidentally become Right after operations")
        fun leftStaysLeft() {
            val e = EitherUtils.left<Int, String>(123)
            assertTrue(e.isLeft())
            // map should not change a Left
            val mapped = e.map { it.length }
            assertTrue(mapped.isLeft(), "Mapping a Left should remain a Left")
            // mapLeft should transform the Left
            val mappedLeft = e.mapLeft { it + 1 }
            mappedLeft.fold(
                { l -> assertEquals(124, l) },
                { _ -> fail("Expected Left after mapLeft") }
            )
        }

        @Test
        @DisplayName("allows null as a left value when the type admits null")
        fun allowsNullLeftIfTypeAllows() {
            val e: Either<String?, Int> = EitherUtils.left(null)
            assertTrue(e.isLeft())
            e.fold(
                { l -> assertNull(l) },
                { _ -> fail("Expected Left(null)") }
            )
        }
    }

    @Nested
    @DisplayName("right(value)")
    inner class RightCreation {

        @Test
        @DisplayName("creates an Either.Right with the provided value (happy path)")
        fun createsRight() {
            val e: Either<String, Int> = EitherUtils.right(42)
            assertTrue(e.isRight(), "Expected isRight() to be true")
            assertFalse(e.isLeft(), "Expected isLeft() to be false")
            e.fold(
                { _ -> fail("Expected Right but was Left") },
                { r -> assertEquals(42, r) }
            )
        }

        @Test
        @DisplayName("supports generic types and preserves the right type parameter")
        fun genericTypes() {
            data class User(val id: String, val name: String)
            val user = User("u1", "Alex")
            val e: Either<Int, User> = EitherUtils.right(user)

            assertTrue(e.isRight())
            e.fold(
                { _ -> fail("Expected Right but was Left") },
                { r -> assertEquals(user, r) }
            )
        }

        @Test
        @DisplayName("Right instances remain Right under map; mapLeft should not affect Right")
        fun rightStaysRight() {
            val e = EitherUtils.right<String, String>("ok")
            assertTrue(e.isRight())
            val mapped = e.map { it.uppercase() }
            mapped.fold(
                { _ -> fail("Expected Right after map") },
                { r -> assertEquals("OK", r) }
            )
            val leftMapped = e.mapLeft { it.length }
            assertTrue(leftMapped.isRight(), "mapLeft on Right should keep it Right")
            leftMapped.fold(
                { _ -> fail("Expected Right after mapLeft") },
                { r -> assertEquals("ok", r.lowercase()) }
            )
        }

        @Test
        @DisplayName("allows null as a right value when the type admits null")
        fun allowsNullRightIfTypeAllows() {
            val e: Either<Int, String?> = EitherUtils.right(null)
            assertTrue(e.isRight())
            e.fold(
                { _ -> fail("Expected Right(null)") },
                { r -> assertNull(r) }
            )
        }
    }

    @Nested
    @DisplayName("Equality and hashCode basics")
    inner class EqualityAndHashCode {

        @Test
        @DisplayName("Left equality with same content")
        fun leftEquality() {
            val a = EitherUtils.left<String, Int>("err")
            val b = "err".left()
            assertEquals(a, b)
            assertEquals(a.hashCode(), b.hashCode())
        }

        @Test
        @DisplayName("Right equality with same content")
        fun rightEquality() {
            val a = EitherUtils.right<String, Int>(7)
            val b = 7.right()
            assertEquals(a, b)
            assertEquals(a.hashCode(), b.hashCode())
        }

        @Test
        @DisplayName("Left and Right with same underlying value should not be equal")
        fun leftRightNotEqual() {
            val left = EitherUtils.left<String, String>("x")
            val right = EitherUtils.right<String, String>("x")
            assertNotEquals(left, right)
        }
    }

    @Nested
    @DisplayName("Fold and interop behavior")
    inner class FoldInterop {

        @Test
        @DisplayName("Fold on Left path returns left-mapped result")
        fun foldLeft() {
            val e = EitherUtils.left<String, Int>("bad")
            val result = e.fold(
                { l -> "Left($l)" },
                { r -> "Right($r)" }
            )
            assertEquals("Left(bad)", result)
        }

        @Test
        @DisplayName("Fold on Right path returns right-mapped result")
        fun foldRight() {
            val e = EitherUtils.right<String, Int>(10)
            val result = e.fold(
                { l -> "Left($l)" },
                { r -> "Right($r)" }
            )
            assertEquals("Right(10)", result)
        }
    }

    @Nested
    @DisplayName("Edge cases")
    inner class EdgeCases {

        @Test
        @DisplayName("Large payloads are handled without mutation or loss")
        fun largePayloads() {
            val largeText = "a".repeat(100_000)
            val e = EitherUtils.right<String, String>(largeText)
            e.fold(
                { _ -> fail("Expected Right for large payload") },
                { r -> assertEquals(100_000, r.length) }
            )
        }

        @Test
        @DisplayName("Type inference with explicit type parameters still compiles and behaves correctly")
        fun explicitTypeParameters() {
            val l: Either<Int, String> = EitherUtils.left<Int, String>(-1)
            val r: Either<Int, String> = EitherUtils.right<Int, String>("done")
            assertTrue(l.isLeft())
            assertTrue(r.isRight())
        }
    }
}