package com.hirrao.klox.ast

import com.hirrao.klox.token.Token
import com.hirrao.klox.token.TokenType.MINUS
import com.hirrao.klox.token.TokenType.STAR

@Deprecated("Finished")
object AstPrinter {
    fun printAst(expr: Expressions): String = when (expr) {
        is Expressions.Binary -> parenthesize(expr.operator.lexeme, expr.left, expr.right)
        is Expressions.Grouping -> parenthesize("Group", expr.expression)
        is Expressions.Literal -> expr.value?.toString() ?: "nil"
        is Expressions.Unary -> parenthesize(expr.operator.lexeme, expr.right)
        is Expressions.Ternary -> parenthesize(expr.operator.lexeme, expr.left, expr.medium, expr.right)
        else -> TODO()
    }

    private fun parenthesize(name: String, vararg exprs: Expressions): String =
        "($name ${exprs.map(::printAst).reduce { acc, expr -> "$acc $expr" }})"
}

fun main() {
    val expression = Expressions.Binary(
        Expressions.Unary(Token(MINUS, "-", null, 1), Expressions.Literal(123)),
        Token(STAR, "*", null, 1),
        Expressions.Grouping(Expressions.Literal(45.67)),
    )
    println(AstPrinter.printAst(expression))
}
