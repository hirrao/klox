package com.hirrao.klox.ast

import com.hirrao.klox.token.Token

sealed class Expr {
    class None : Expr()

    class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()

    class Call(val callee: Expr, val paran: Token, val argument: List<Expr>) : Expr()

    class Get(val obj: Expr, val name: Token) : Expr()

    class Grouping(val expression: Expr) : Expr()

    class Literal(val value: Any?) : Expr()

    class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr()

    class Set(val obj: Expr, val name: Token, val value: Expr) : Expr()

    class Super(val keyword: Token, val method: Token) : Expr()

    class This(val keyword: Token) : Expr()

    class Unary(val operator: Token, val right: Expr) : Expr()

    class Variable(val name: Token) : Expr()
}
