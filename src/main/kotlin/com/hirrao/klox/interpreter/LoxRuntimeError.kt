package com.hirrao.klox.interpreter

import com.hirrao.klox.token.Token

data class LoxRuntimeError(val token: Token, override val message: String) : RuntimeException(message)
