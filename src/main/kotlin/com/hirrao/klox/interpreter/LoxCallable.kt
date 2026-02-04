package com.hirrao.klox.interpreter

import com.hirrao.klox.parser.Environment
import com.hirrao.klox.syntax.Statements
import com.hirrao.klox.token.Token
const val MAX_PARAMETERS = 32

interface LoxCallable {
    val arity: Int
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}

class LoxFunction(val declaration: Statements.Function, val closure: Environment, val isInitializer: Boolean = false) :
    LoxCallable {
    override fun toString() = "<fn ${declaration.name.lexeme}>"
    override val arity: Int = declaration.params.size
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        declaration.params.forEachIndexed { index, token -> environment.define(token.lexeme, arguments[index]) }
        try {
            interpreter.execute(Statements.Block(declaration.body), environment)
        } catch (value: Return) {
            if (isInitializer) return closure[0, "this"]
            return value.value
        }
        return null
    }

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment, isInitializer)
    }
}

class LoxAnonymousFunction(val params: List<Token>, val body: List<Statements>, val closure: Environment) :
    LoxCallable {
    override val arity: Int = params.size
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        params.forEachIndexed { index, token -> environment.define(token.lexeme, arguments[index]) }
        try {
            interpreter.execute(Statements.Block(body), environment)
        } catch (value: Return) {
            return value.value
        }
        return null
    }

    override fun toString() = "<fn>"
}

data class Return(val value: Any?, val token: Token) : RuntimeException(null, null, false, false)
