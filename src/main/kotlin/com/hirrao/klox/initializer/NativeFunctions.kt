package com.hirrao.klox.initializer

import com.hirrao.klox.interpreter.Interpreter
import com.hirrao.klox.interpreter.LoxCallable

import kotlin.Any
import kotlin.math.floor

fun Interpreter.defineNativeFunction(name: String, arity: Int, fn: (Interpreter, List<Any?>) -> Any?) =
    this.globals.define(name, createNativeFunction(arity, fn))

fun createNativeFunction(arity: Int, fn: (Interpreter, List<Any?>) -> Any?) = object : LoxCallable {
    override val arity = arity
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? = fn(interpreter, arguments)
    override fun toString() = "<native fn>"
}

fun Interpreter.initNativeFunctions() {
    with(this) {
        defineNativeFunction(
            "clock",
            0,
        ) { _, _ ->
            System.currentTimeMillis() / 1000.0
        }
        defineNativeFunction("print", 1) { _, args ->
            print(
                when (val value = args[0]) {
                    null -> "nil"
                    is Double -> if (floor(value) == value) value.toInt().toString() else value.toString()
                    else -> value.toString()
                },
            )
        }
        defineNativeFunction("println", 1) { _, args ->
            println(
                when (val value = args[0]) {
                    null -> "nil"
                    is Double -> if (floor(value) == value) value.toInt().toString() else value.toString()
                    else -> value.toString()
                },
            )
        }
    }
}
