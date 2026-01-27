package com.hirrao.klox.resolver

import com.hirrao.klox.Lox
import com.hirrao.klox.syntax.Expressions
import com.hirrao.klox.syntax.Statements
import com.hirrao.klox.token.Token

/**
 * Static analyzer that resolves variables and validates function call arity at compile time.
 * This moves the arity check from runtime to compile time, eliminating the performance overhead
 * of checking argument counts on every function call.
 */
class Resolver {
    // Track known functions and their arities
    private val functionArities = mutableMapOf<String, Int>()

    fun resolve(statements: List<Statements>) {
        statements.forEach { resolve(it) }
    }

    private fun resolve(stmt: Statements) {
        when (stmt) {
            is Statements.Block -> stmt.statements.forEach { resolve(it) }
            is Statements.Expression -> resolve(stmt.expression)
            is Statements.If -> {
                resolve(stmt.condition)
                resolve(stmt.thenBranch)
                stmt.elseBranch?.let { resolve(it) }
            }
            is Statements.Function -> {
                // Register function with its arity
                functionArities[stmt.name.lexeme] = stmt.params.size
                stmt.body.forEach { resolve(it) }
            }
            is Statements.Return -> resolve(stmt.value)
            is Statements.Var -> stmt.initializer?.let { resolve(it) }
            is Statements.While -> {
                resolve(stmt.condition)
                resolve(stmt.body)
            }
            is Statements.Class -> {
                stmt.methods.forEach { resolve(it) }
            }
            is Statements.None -> {}
        }
    }

    private fun resolve(expr: Expressions) {
        when (expr) {
            is Expressions.Assign -> resolve(expr.value)
            is Expressions.Binary -> {
                resolve(expr.left)
                resolve(expr.right)
            }
            is Expressions.Call -> {
                resolve(expr.callee)
                expr.arguments.forEach { resolve(it) }
                // Validate arity at compile time
                validateCallArity(expr)
            }
            is Expressions.Get -> resolve(expr.obj)
            is Expressions.Grouping -> resolve(expr.expression)
            is Expressions.Literal -> {}
            is Expressions.Logical -> {
                resolve(expr.left)
                resolve(expr.right)
            }
            is Expressions.Set -> {
                resolve(expr.value)
                resolve(expr.obj)
            }
            is Expressions.Super -> {}
            is Expressions.Ternary -> {
                resolve(expr.left)
                resolve(expr.medium)
                resolve(expr.right)
            }
            is Expressions.This -> {}
            is Expressions.Unary -> resolve(expr.right)
            is Expressions.Variable -> {}
            is Expressions.None -> {}
        }
    }

    private fun validateCallArity(call: Expressions.Call) {
        // For variable calls, try to resolve the function name
        if (call.callee is Expressions.Variable) {
            val functionName = call.callee.name.lexeme
            val expectedArity = functionArities[functionName]

            if (expectedArity != null) {
                val actualArity = call.arguments.size
                if (actualArity != expectedArity) {
                    error(
                        call.paren,
                        "Expected $expectedArity arguments but got $actualArity.",
                    )
                }
            }
        }
    }

    /**
     * Register a native function's arity for compile-time checking.
     */
    fun registerNativeFunction(name: String, arity: Int) {
        functionArities[name] = arity
    }

    private fun error(token: Token, message: String) {
        Lox.error(token, message)
    }
}
