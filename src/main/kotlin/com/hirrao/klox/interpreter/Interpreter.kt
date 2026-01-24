package com.hirrao.klox.interpreter

import com.hirrao.klox.Lox
import com.hirrao.klox.ast.Expr
import com.hirrao.klox.ast.Stmt
import com.hirrao.klox.parser.Environment
import com.hirrao.klox.token.Token
import com.hirrao.klox.token.TokenType.*

import kotlin.math.floor

class Interpreter {
    var environment = Environment()

    fun interpret(statements: List<Stmt>) {
        try {
            statements.forEach { execute(it) }
        } catch (error: LoxRuntimeError) {
            Lox.runtimeError(error)
        }
    }

    private fun stringify(obj: Any?) = when (obj) {
        null -> "nil"
        is Double -> if (floor(obj) == obj) obj.toInt().toString() else obj.toString()
        else -> obj.toString()
    }

    fun evaluate(expr: Expr): Any? = when (expr) {
        is Expr.Binary -> {
            val left = evaluate(expr.left)
            val right = evaluate(expr.right)
            when (expr.operator.type) {
                MINUS -> {
                    checkNumberOperand(expr.operator, left, right)
                    left as Double - right as Double
                }

                SLASH -> {
                    checkNumberOperand(expr.operator, left, right)
                    left as Double / right as Double
                }

                STAR -> {
                    checkNumberOperand(expr.operator, left, right)
                    left as Double * right as Double
                }

                PLUS -> when (left) {
                    is Double if right is Double -> left + right
                    is String if right is String -> left + right
                    else -> throw LoxRuntimeError(expr.operator, "Operands must be two numbers or two strings.")
                }

                GREATER -> {
                    checkNumberOperand(expr.operator, left, right)
                    left as Double > right as Double
                }

                GREATER_EQUAL -> {
                    checkNumberOperand(expr.operator, left, right)
                    left as Double >= right as Double
                }

                LESS -> {
                    checkNumberOperand(expr.operator, left, right)
                    (left as Double) < right as Double
                }

                LESS_EQUAL -> {
                    checkNumberOperand(expr.operator, left, right)
                    left as Double <= right as Double
                }

                BANG_EQUAL -> left != right
                EQUAL_EQUAL -> left == right
                else -> null
            }
        }

        is Expr.Grouping -> evaluate(expr.expression)
        is Expr.Literal -> expr.value
        is Expr.Unary -> {
            val right = evaluate(expr.right)
            when (expr.operator.type) {
                MINUS -> {
                    checkNumberOperand(expr.operator, right)
                    -(right as Double)
                }

                BANG -> {
                    right.isTruthy()
                }

                else -> null
            }
        }

        is Expr.Variable -> environment[expr.name]
        is Expr.Assign -> {
            val value = evaluate(expr.value)
            environment[expr.name] = value
            return value
        }

        else -> null
    }

    fun execute(stmt: Stmt) {
        when (stmt) {
            is Stmt.Expression -> evaluate(stmt.expression)
            is Stmt.Print -> {
                val obj = evaluate(stmt.expression)
                println(stringify(obj))
            }

            is Stmt.Var -> {
                val value = if (stmt.initializer != null) evaluate(stmt.initializer) else null
                environment.define(stmt.name.lexeme, value)
            }

            is Stmt.Block -> {
                val previous = this.environment
                try {
                    this.environment = Environment(environment)
                    stmt.statements.forEach {
                        execute(it)
                    }
                } finally {
                    this.environment = previous
                }
            }

            is Stmt.If -> {
                if (evaluate(stmt.condition).isTruthy()) {
                    execute(stmt.thenBranch)
                } else if (stmt.elseBranch != null) {
                    execute(stmt.elseBranch)
                }
            }

            else -> TODO()
        }
    }

    private fun checkNumberOperand(operator: Token, vararg operand: Any?) {
        for (i in operand) {
            if (i !is Double) throw LoxRuntimeError(operator, "Operand must be a number.")
        }
    }

    private fun Any?.isTruthy(): Boolean = when (this) {
        null -> false
        is Boolean -> this
        else -> true
    }
}
