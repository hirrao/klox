package com.hirrao.klox.syntax

import com.hirrao.klox.token.Token

sealed class Expressions {
    object None : Expressions()
    class AnonymousFunction(val params: List<Token>, val body: List<Statements>) : Expressions()
    class Assign(val name: Token, val value: Expressions) : Expressions()
    class Binary(val left: Expressions, val operator: Token, val right: Expressions) : Expressions()
    class Call(val callee: Expressions, val paren: Token, val arguments: List<Expressions>) : Expressions()
    class Get(val obj: Expressions, val name: Token) : Expressions()
    class Grouping(val expression: Expressions) : Expressions()
    class Literal(val value: Any?) : Expressions()
    class Logical(val left: Expressions, val operator: Token, val right: Expressions) : Expressions()
    class Set(val obj: Expressions, val name: Token, val value: Expressions) : Expressions()
    class Super(val keyword: Token, val method: Token) : Expressions()
    class Ternary(val left: Expressions, val operator: Token, val medium: Expressions, val right: Expressions) :
        Expressions()

    class This(val keyword: Token) : Expressions()
    class Unary(val operator: Token, val right: Expressions) : Expressions()
    class Variable(val name: Token) : Expressions()
}
