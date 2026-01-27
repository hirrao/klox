package com.hirrao.klox.interpreter

const val MAX_PARAMETERS = 32

/**
 * Interface for callable objects in Lox (functions, classes, etc.).
 *
 * Note: As of the latest implementation, arity checking is performed at compile time
 * by the Resolver (see resolver/Resolver.kt), not at runtime. This eliminates the
 * performance overhead of validating argument counts on every function call.
 *
 * Previously, the arity property was checked at runtime before each call, which was
 * necessary because function names are independent of parameter count. The static
 * analysis approach provides the same safety guarantees without the runtime cost.
 *
 * See docs/smalltalk-arity-answer.md for comparison with Smalltalk's approach.
 */
interface LoxCallable {
    val arity: Int
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}
