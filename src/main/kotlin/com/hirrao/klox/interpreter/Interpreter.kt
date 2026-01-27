package com.hirrao.klox.interpreter

import com.hirrao.klox.Lox
import com.hirrao.klox.parser.Environment
import com.hirrao.klox.syntax.Expressions
import com.hirrao.klox.syntax.Statements
import com.hirrao.klox.token.Token
import com.hirrao.klox.token.TokenType.*

class Interpreter {
    val globals = Environment()

    fun interpret(statements: List<Statements>) {
        try {
            statements.forEach { execute(it, globals) }
        } catch (error: LoxRuntimeError) {
            Lox.runtimeError(error)
        } catch (error: Return) {
            Lox.runtimeError(LoxRuntimeError(error.token,"Return must be used within a function"))
        }
    }

    fun evaluate(expr: Expressions, environment: Environment): Any? {
        when (expr) {
            is Expressions.Assign -> {
                val value = evaluate(expr.value, environment)
                environment[expr.name] = value
                return value
            }

            is Expressions.Binary -> {
                val left = evaluate(expr.left, environment)
                val right = evaluate(expr.right, environment)
                return when (expr.operator.type) {
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
                    COMMA -> right
                    else -> null
                }
            }

            is Expressions.Call -> {
                val callee = evaluate(expr.callee, environment)
                val arguments = expr.arguments.map { evaluate(it, environment) }
                val function = callee as? LoxCallable
                    ?: throw LoxRuntimeError(
                        expr.paren,
                        "Can only call functions and classes.",
                    )
                if (arguments.size !=
                    function.arity
                ) {
                    throw LoxRuntimeError(
                        expr.paren,
                        "Expected ${function.arity} arguments but got ${arguments.size}.",
                    )
                }
                return function.call(this, arguments)
            }
            is Expressions.Grouping -> return evaluate(expr.expression, environment)
            is Expressions.Literal -> return expr.value
            is Expressions.Logical -> {
                val left = evaluate(expr.left, environment)
                when (expr.operator.type) {
                    OR -> if (expr.left.isTruthy()) return left
                    else -> if (!expr.left.isTruthy()) return left
                }
                return evaluate(expr.right, environment)
            }

            is Expressions.Ternary -> {
                val left = evaluate(expr.left, environment)
                return if (left.isTruthy()) evaluate(expr.medium, environment) else evaluate(expr.right, environment)
            }
            is Expressions.Unary -> {
                val right = evaluate(expr.right, environment)
                return when (expr.operator.type) {
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

            is Expressions.Variable -> return environment[expr.name]

            else -> TODO()
        }
    }

    fun execute(statement: Statements, environment: Environment) {
        when (statement) {
            is Statements.Block -> {
                val env = Environment(environment)
                statement.statements.forEach {
                    execute(it, env)
                }
            }
            is Statements.Expression -> evaluate(statement.expression, environment)
            is Statements.Function -> {
                val function = LoxFunction(statement, environment)
                environment.define(statement.name.lexeme, function)
            }
            is Statements.If -> {
                if (evaluate(statement.condition, environment).isTruthy()) {
                    execute(statement.thenBranch, environment)
                } else if (statement.elseBranch != null) {
                    execute(statement.elseBranch, environment)
                }
            }
            is Statements.Return -> {
                throw Return(statement.value?.let { evaluate(it, environment)},statement.keyword)
            }
            is Statements.Var -> {
                val value = if (statement.initializer != null) evaluate(statement.initializer, environment) else null
                environment.define(statement.name.lexeme, value)
            }
            is Statements.While -> {
                while (evaluate(statement.condition, environment).isTruthy()) {
                    execute(statement.body, environment)
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
