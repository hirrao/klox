package com.hirrao.klox.syntax

import com.hirrao.klox.token.Token

sealed class Statements {
    object None : Statements()
    class Block(val statements: List<Statements>) : Statements()
    class Class(val name: Token, val superclass: Expressions.Variable, val methods: List<Function>) :
        Statements()
    class Expression(val expression: Expressions) : Statements()
    class Function(val name: Token, val params: List<Token>, val body: List<Statements>) : Statements()
    class If(val condition: Expressions, val thenBranch: Statements, val elseBranch: Statements?) : Statements()
    class Return(val keyword: Token, val value: Expressions?) : Statements()
    class Var(val name: Token, val initializer: Expressions?) : Statements()
    class While(val condition: Expressions, val body: Statements) : Statements()
}
