package com.hirrao.klox

fun main(args: Array<String>) {
    when(args.size){
        in 2..Int.MAX_VALUE -> println("Usage: jlox [script]")
        1 -> Lox.runFile(args[0])
        0-> Lox.runPrompt()
    }
}