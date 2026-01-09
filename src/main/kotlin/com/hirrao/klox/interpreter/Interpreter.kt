package com.hirrao.klox.interpreter

import com.hirrao.klox.Lox
import com.hirrao.klox.ast.Expr
import com.hirrao.klox.token.Token
import com.hirrao.klox.token.TokenType.*

import kotlin.math.floor

object Interpreter {
    fun interpret(expression: Expr) {
        try {
            val value = evaluate(expression)
            println(stringify(value))
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
                    !((right as? Boolean) ?: (right != null))
                }

                else -> null
            }
        }
        else -> null
    }

    private fun checkNumberOperand(operator: Token, vararg operand: Any?) {
        for (i in operand) {
            if (i !is Double) throw LoxRuntimeError(operator, "Operand must be a number.")
        }
    }
}
