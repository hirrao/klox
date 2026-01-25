package com.hirrao.klox.parser

import com.hirrao.klox.Lox
import com.hirrao.klox.ast.Expressions
import com.hirrao.klox.ast.Statements
import com.hirrao.klox.token.Token
import com.hirrao.klox.token.TokenType
import com.hirrao.klox.token.TokenType.*

class Parser(val tokens: List<Token>) {
    private class ParseError : RuntimeException()

    private var current = 0

    fun parse(): List<Statements> {
        val statements: MutableList<Statements> = ArrayList()
        while (!isAtEnd()) {
            statements.add(declaration())
        }
        return statements
    }

    // 解析定义部分
    private fun declaration(): Statements {
        try {
            if (match(VAR)) return varDeclaration()
            /*
            if (match(CLASS)) return classDeclaration()
            if (match(FUN)) return funDeclaration()
             */
            return statement()
        } catch (_: ParseError) {
            synchronize()
            return Statements.None
        }
    }

    private fun varDeclaration(): Statements {
        val name = consume(IDENTIFIER, "Expect variable name.")
        val initializer = if (match(EQUAL)) {
            expression()
        } else {
            null
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return Statements.Var(name, initializer)
    }

    // 解析语句部分
    private fun statement(): Statements {
        if (match(IF)) return ifStatement()
        if (match(PRINT)) return printStatement()
        if (match(WHILE)) return whileStatement()
        if (match(FOR)) return forStatement()
        if (match(LEFT_BRACE)) return Statements.Block(block())
        /*
        if (match(RETURN)) return returnStatement()
         */
        return expressionStatement()
    }

    private fun ifStatement(): Statements {
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after if condition.")
        val thenBranch = statement()
        val elseBranch = if (match(ELSE)) statement() else null
        return Statements.If(condition, thenBranch, elseBranch)
    }

    private fun printStatement(): Statements {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Statements.Print(value)
    }

    private fun whileStatement(): Statements {
        consume(LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()
        return Statements.While(condition, body)
    }

    private fun forStatement(): Statements {
        consume(LEFT_PAREN, "Expect '(' after 'for'.")
        val initializer = if (match(SEMICOLON)) {
            null
        } else if (match(VAR)) {
            varDeclaration()
        } else {
            expressionStatement()
        }
        val condition = if (!check(SEMICOLON)) expression() else Expressions.Literal(true)
        consume(SEMICOLON, "Expect ';' after loop condition.")
        val increment = if (!check(RIGHT_PAREN)) expression() else null
        consume(RIGHT_PAREN, "Expect ')' after for clauses.")
        val body = Statements.While(
            condition,
            if (increment != null) {
                Statements.Block(listOf(statement(), Statements.Expression(increment)))
            } else {
                statement()
            },
        )
        val statement = if (initializer != null) {
            Statements.Block(listOf(initializer, body))
        } else {
            body
        }
        return statement
    }

    private fun block(): List<Statements> {
        val statements: MutableList<Statements> = ArrayList()
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }
        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun expressionStatement(): Statements {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Statements.Expression(expr)
    }

    // 表达式部分
    private fun expression() = comma()

    private fun comma(): Expressions {
        var expr = assignment()
        while (match(COMMA)) {
            val operator = previous()
            val right = assignment()
            expr = Expressions.Binary(expr, operator, right)
        }
        return expr
    }

    private fun assignment(): Expressions {
        val expr = ternary()
        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()
            if (expr is Expressions.Variable) {
                val name = expr.name
                return Expressions.Assign(name, value)
            }
            error(equals, "Invalid assignment target.")
        }
        return expr
    }

    private fun ternary(): Expressions {
        var expr = or()
        while (match(QUESTION)) {
            val operator = previous()
            val medium = expression()
            consume(COLON, "Expect ':' after expression.")
            val right = ternary()
            expr = Expressions.Ternary(expr, operator, medium, right)
        }
        return expr
    }

    private fun or(): Expressions {
        val expr = and()
        while (match(OR)) {
            val operator = previous()
            val right = and()
            return Expressions.Logical(expr, operator, right)
        }
        return expr
    }

    private fun and(): Expressions {
        val expr = equality()
        while (match(AND)) {
            val operator = previous()
            val right = equality()
            return Expressions.Logical(expr, operator, right)
        }
        return expr
    }

    private fun equality(): Expressions {
        var expr = comparison()
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expressions.Binary(expr, operator, right)
        }
        return expr
    }

    private fun comparison(): Expressions {
        var expr = term()
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expressions.Binary(expr, operator, right)
        }
        return expr
    }

    private fun term(): Expressions {
        var expr = factor()
        while (match(MINUS, PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Expressions.Binary(expr, operator, right)
        }
        return expr
    }

    private fun factor(): Expressions {
        var expr = unary()
        while (match(SLASH, STAR)) {
            val operator = previous()
            val right = unary()
            expr = Expressions.Binary(expr, operator, right)
        }
        return expr
    }

    private fun unary(): Expressions {
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Expressions.Unary(operator, right)
        }
        return primary()
    }

    private fun primary(): Expressions {
        if (match(FALSE)) return Expressions.Literal(false)
        if (match(TRUE)) return Expressions.Literal(true)
        if (match(NIL)) return Expressions.Literal(null)
        if (match(NUMBER, STRING)) {
            return Expressions.Literal(previous().literal)
        }

        if (match(IDENTIFIER)) return Expressions.Variable(previous())
        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Expressions.Grouping(expr)
        }
        throw error(peek(), "Expect expression.")
    }

    // 工具函数
    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd() = peek().type == EOF

    private fun peek() = tokens[current]

    private fun previous() = tokens[current - 1]

    private fun error(token: Token, message: String): ParseError {
        Lox.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return
            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> Unit
            }
            advance()
        }
    }
}
