package com.hirrao.klox.interpreter

const val MAX_PARAMETERS = 32

/**
 * Interface for callable objects in Lox (functions, classes, etc.).
 *
 * Note: The arity property must be checked at runtime before each call
 * (see Interpreter.kt). This is necessary because in Lox, function names
 * are independent of their parameter count. In contrast, Smalltalk avoids
 * this runtime overhead because method names include the parameter count
 * (e.g., "at:put:" has 2 parameters by definition).
 *
 * See docs/smalltalk-arity-answer.md for a detailed explanation.
 */
interface LoxCallable {
    val arity: Int
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}
