package com.hirrao.klox.parser

import com.hirrao.klox.interpreter.LoxRuntimeError
import com.hirrao.klox.token.Token

class Environment(val enclosing: Environment? = null) {
    val values = HashMap<String, Any?>()

    operator fun get(name: Token): Any? = values.getOrElse(name.lexeme) {
        if (enclosing != null) return enclosing[name]
        throw LoxRuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    operator fun get(distance: Int, name: String) = ancestor(distance).values[name]

    operator fun set(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
        } else if (enclosing != null) {
            enclosing[name] = value
        } else {
            throw LoxRuntimeError(name, "Undefined variable '${name.lexeme}'.")
        }
    }

    operator fun set(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = value
    }

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun ancestor(distance: Int): Environment {
        var environment: Environment = this
        repeat(distance) {
            environment = environment.enclosing!!
        }
        return environment
    }
}
