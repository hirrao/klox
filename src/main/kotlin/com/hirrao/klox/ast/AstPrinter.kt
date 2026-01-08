package com.hirrao.klox.ast

import com.hirrao.klox.token.Token
import com.hirrao.klox.token.TokenType.MINUS
import com.hirrao.klox.token.TokenType.STAR

object AstPrinter {
    fun printAst(expr: Expr): String = when (expr) {
        is Expr.None -> "none"
        is Expr.Binary -> parenthesize(expr.operator.lexeme, expr.left, expr.right)
        is Expr.Call -> TODO()
        is Expr.Get -> TODO()
        is Expr.Grouping -> parenthesize("Group", expr.expression)
        is Expr.Literal -> expr.value?.toString() ?: "nil"
        is Expr.Logical -> TODO()
        is Expr.Set -> TODO()
        is Expr.Super -> TODO()
        is Expr.This -> TODO()
        is Expr.Unary -> parenthesize(expr.operator.lexeme, expr.right)
        is Expr.Variable -> TODO()
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String =
        "($name ${exprs.map(::printAst).reduce { acc, expr -> "$acc $expr" }})"
}

fun main() {
    val expression =
        Expr.Binary(
            Expr.Unary(Token(MINUS, "-", null, 1), Expr.Literal(123)),
            Token(STAR, "*", null, 1),
            Expr.Grouping(Expr.Literal(45.67)),
        )
    println(AstPrinter.printAst(expression))
}
