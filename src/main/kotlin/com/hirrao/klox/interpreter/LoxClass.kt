package com.hirrao.klox.interpreter

import com.hirrao.klox.token.Token

class LoxClass(val name: String, val methods: MutableMap<String, LoxFunction>) : LoxCallable {
    override val arity: Int
        get() {
            val initializer = findMethod("init")
            return initializer?.arity ?: 0
        }
    override fun toString() = name
    override fun call(interpreter: Interpreter, arguments: List<Any?>): LoxInstance {
        val instance = LoxInstance(this)
        val initializer = findMethod("init")
        initializer?.bind(instance)?.call(interpreter, arguments)
        return instance
    }

    fun findMethod(name: String) = methods.getOrDefault(name, null)
}

class LoxInstance(val loxClass: LoxClass) {
    val field: MutableMap<String, Any?> = HashMap()
    override fun toString() = "${loxClass.name} instance"
    operator fun get(name: Token): Any? = field.getOrElse(name.lexeme) {
        val method = loxClass.findMethod(name.lexeme)
        if (method != null) return@getOrElse method.bind(this)
        throw LoxRuntimeError(name, "Undefined property '${name.lexeme}'.")
    }
    operator fun set(name: Token, value: Any?) {
        field[name.lexeme] = value
    }
}
