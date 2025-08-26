package com.churchapp.util

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class OptionUtilsTest_Unit {

    // Happy path: some(value) should wrap the exact value in Some
    @Test
    fun some_wraps_value_in_Some() {
        val value = "hello"
        val opt: Option<String> = OptionUtils.some(value)

        assertIs<Some<String>>(opt, "Expected Some for a non-null value")
        assertEquals(value, (opt as Some<String>).value, "Wrapped value should match input")
    }

    // Edge cases: some with different types (Int, Boolean), including zero and false
    @Test
    fun some_handles_primitive_edge_values() {
        val zero = OptionUtils.some(0)
        val negative = OptionUtils.some(-1)
        val falseBool = OptionUtils.some(false)

        assertIs<Some<Int>>(zero)
        assertEquals(0, (zero as Some<Int>).value)

        assertIs<Some<Int>>(negative)
        assertEquals(-1, (negative as Some<Int>).value)

        assertIs<Some<Boolean>>(falseBool)
        assertFalse((falseBool as Some<Boolean>).value, "Expected false preserved inside Some")
    }

    // none() should produce an empty Option (None)
    @Test
    fun none_produces_None() {
        val opt: Option<String> = OptionUtils.none()

        assertIs<None>(opt, "Expected None for none()")
        assertTrue(opt.isEmpty(), "none() should be empty")
        assertFalse(opt.isDefined(), "none() should not be defined")
    }

    // of(value) with non-null should be Some
    @Test
    fun of_nonNull_is_Some() {
        val value = 42
        val opt = OptionUtils.of(value)

        assertIs<Some<Int>>(opt)
        assertEquals(42, (opt as Some<Int>).value)
    }

    // of(value) with null should be None
    @Test
    fun of_null_is_None() {
        val opt: Option<String> = OptionUtils.of(null)

        assertIs<None>(opt)
        assertTrue(opt.isEmpty())
    }

    // of(value) with boundary-like objects (empty string, empty list)
    @Test
    fun of_boundary_values_are_Still_Some() {
        val emptyString = OptionUtils.of("")
        val emptyList = OptionUtils.of(listOf<Int>())

        assertIs<Some<String>>(emptyString, "Empty string is non-null and should be Some")
        assertEquals("", (emptyString as Some<String>).value)

        assertIs<Some<List<Int>>>(emptyList, "Empty list is non-null and should be Some")
        assertTrue((emptyList as Some<List<Int>>).value.isEmpty(), "List inside Some should be empty")
    }

    // Round-trip: some(value) then fold should recover the value
    @Test
    fun some_fold_recovers_value() {
        val value = "roundtrip"
        val opt = OptionUtils.some(value)

        val recovered = opt.fold({ "missing" }) { it }
        assertEquals(value, recovered, "Fold over Some should yield the wrapped value")
    }

    // Round-trip: none() then fold should use default
    @Test
    fun none_fold_uses_default() {
        val opt: Option<String> = OptionUtils.none()
        val recovered = opt.fold({ "default" }) { it }
        assertEquals("default", recovered, "Fold over None should yield default")
    }

    // Chaining behavior to ensure produced Options integrate properly with Arrow ops
    @Test
    fun produced_options_chain_correctly_with_map_and_flatMap() {
        val opt = OptionUtils.some(10)
            .map { it + 5 }
            .flatMap { v -> OptionUtils.of(if (v > 12) v * 2 else null) }

        assertIs<Some<Int>>(opt)
        assertEquals(30, (opt as Some<Int>).value)
    }

    // Chaining with None branch
    @Test
    fun chaining_from_None_stays_None() {
        val opt = OptionUtils.none<Int>()
            .map { it + 1 }
            .flatMap { OptionUtils.some(it * 2) }

        assertIs<None>(opt)
    }

    // Type inference smoke test across generics
    @Test
    fun type_inference_works_for_various_generics() {
        data class User(val id: String, val name: String?)

        val user = User("u1", null)
        val userOpt = OptionUtils.some(user)
        val nameOpt = userOpt.flatMap { OptionUtils.of(it.name) }

        assertIs<Some<User>>(userOpt)
        assertIs<None>(nameOpt, "User.name is null -> OptionUtils.of(null) should yield None")
    }

    // Verify equality semantics for Options produced by utils
    @Test
    fun option_equality_semantics() {
        val a = OptionUtils.some("x")
        val b = OptionUtils.some("x")
        val c = OptionUtils.none<String>()

        assertEquals(a, b, "Two Some with same value should be equal")
        assertTrue(a != c, "Some should not equal None")
    }
}