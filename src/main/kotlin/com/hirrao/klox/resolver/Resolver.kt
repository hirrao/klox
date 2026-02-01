package com.hirrao.klox.resolver

import com.hirrao.klox.Lox
import com.hirrao.klox.interpreter.Interpreter
import com.hirrao.klox.syntax.Expressions
import com.hirrao.klox.syntax.Statements
import com.hirrao.klox.token.Token

class Resolver(val interpreter: Interpreter) {
    private val scopes = ArrayDeque<MutableMap<String, Boolean>>()
    private var currentFunction = FunctionType.NONE

    fun resolve(statements: List<Statements>) {
        statements.forEach { resolve(it) }
    }

    private fun resolve(statement: Statements) {
        when (statement) {
            is Statements.Block -> {
                beginScope()
                resolve(statement.statements)
                endScope()
            }
            is Statements.Expression -> {
                resolve(statement.expression)
            }
            is Statements.Function -> {
                declare(statement.name)
                define(statement.name)
                resolveFunction(statement.params, statement.body, FunctionType.FUNCTION)
            }
            is Statements.If -> {
                resolve(statement.condition)
                resolve(statement.thenBranch)
                if (statement.elseBranch != null) resolve(statement.elseBranch)
            }
            is Statements.Return -> {
                if (currentFunction == FunctionType.NONE) {
                    Lox.error(statement.keyword, "Can't return from top-level code.")
                }
                if (statement.value != null) resolve(statement.value)
            }
            is Statements.Var -> {
                declare(statement.name)
                if (statement.initializer != null)resolve(statement.initializer)
                define(statement.name)
            }
            is Statements.While -> {
                resolve(statement.condition)
                resolve(statement.body)
            }
            else -> Unit
        }
    }

    private fun resolve(expression: Expressions) {
        when (expression) {
            is Expressions.AnonymousFunction -> {
                resolveFunction(expression.params, expression.body, FunctionType.AnonymousFunction)
            }
            is Expressions.Assign -> {
                resolve(expression.value)
                resolveLocal(expression, expression.name)
            }
            is Expressions.Binary -> {
                resolve(expression.left)
                resolve(expression.right)
            }
            is Expressions.Call -> {
                resolve(expression.callee)
                expression.arguments.forEach { resolve(it) }
            }
            is Expressions.Grouping -> {
                resolve(expression.expression)
            }
            is Expressions.Logical -> {
                resolve(expression.left)
                resolve(expression.right)
            }
            is Expressions.Ternary -> {
                resolve(expression.left)
                resolve(expression.medium)
                resolve(expression.right)
            }
            is Expressions.Unary -> {
                resolve(expression.right)
            }
            is Expressions.Variable -> {
                if (!scopes.isEmpty() &&
                    scopes.last()[expression.name.lexeme] == false
                ) {
                    Lox.error(expression.name, "Can't read local variable in its own initializer.")
                }
                resolveLocal(expression, expression.name)
            }
            else -> Unit
        }
    }

    private fun beginScope() {
        scopes.addLast(HashMap())
    }

    private fun endScope() {
        scopes.removeLast()
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return
        val scope = scopes.last()
        if (scope.containsKey(name.lexeme)) Lox.error(name, "Already variable with this name in this scope.")
        scopes.last()[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return
        scopes.last()[name.lexeme] = true
    }

    private fun resolveLocal(expression: Expressions, name: Token) {
        scopes.asReversed().forEachIndexed { index, map ->
            if (map.containsKey(name.lexeme)) interpreter.resolve(expression, index)
        }
    }

    private fun resolveFunction(params: List<Token>, body: List<Statements>, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type
        beginScope()
        params.forEach {
            declare(it)
            define(it)
        }
        resolve(body)
        endScope()
        currentFunction = enclosingFunction
    }

    private enum class FunctionType {
        NONE,
        AnonymousFunction,
        FUNCTION,
    }
}
