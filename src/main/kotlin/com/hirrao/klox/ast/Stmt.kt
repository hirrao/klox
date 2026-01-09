package com.hirrao.klox.ast

import com.hirrao.klox.token.Token

sealed class Stmt {
    class Block(statements: List<Stmt>) : Stmt()
    class Class(name: Token, superclass: Expr.Variable, methods: List<Function>) : Stmt()
    class Expression(expression: Expr) : Stmt()
    class Function(name: Token, params: List<Token>, body: List<Stmt>) : Stmt()
    class If(condition: Expr, thenBranch: Stmt, elseBranch: Stmt) : Stmt()
    class Print(expression: Expr) : Stmt()
    class Return(keyword: Token, value: Expr) : Stmt()
    class Var(name: Token, initializer: Expr) : Stmt()
    class While(condition: Expr, body: Stmt) : Stmt()
}