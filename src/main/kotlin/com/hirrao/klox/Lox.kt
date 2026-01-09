package com.hirrao.klox

import com.hirrao.klox.interpreter.Interpreter
import com.hirrao.klox.interpreter.LoxRuntimeError
import com.hirrao.klox.parser.Parser
import com.hirrao.klox.scanner.Scanner
import com.hirrao.klox.token.Token
import com.hirrao.klox.token.TokenType

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

object Lox {
    var hadError = false
    var hadRuntimeError = false

    fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))
        if (hadError) exitProcess(65)
        if (hadRuntimeError) exitProcess(70)
    }

    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)
        while (true) {
            print("> ")
            val line = reader.readLine() ?: break
            run(line)
            hadError = false
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        val expression = parser.parse()
        if (hadError) return
        Interpreter.interpret(expression)
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, message: String) {
        if (token.type === TokenType.EOF) {
            report(token.line, " at end", message)
        } else {
            report(token.line, " at '${token.lexeme}'", message)
        }
    }

    private fun report(line: Int, where: String, message: String) {
        println("[line $line] Error $where: $message")
        hadError = true
    }

    fun runtimeError(error: LoxRuntimeError) {
        System.err.println("${error.message}\n [line ${error.token.line}]")
        hadRuntimeError = true
    }
}
