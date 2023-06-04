class Parser(private val tokens: List<Token>) {
    private var current: Int = 0
    var errorList: ArrayList<String> = ArrayList()

    fun parse(): ArrayList<Stmt?>? {
        val stmts = ArrayList<Stmt?>()
        try {
        while (!isAtEnd()) {
            stmts.add(declaration())
            }
        }
        catch (e: ParseError) {
            return null
        }

        return stmts
    }

    private fun declaration(): Stmt? {
        try {
            if (match(TokenType.VAR)) return varDeclaration()
            return statement()
        }
        catch (e: ParseError) {
            synchronise()
            return null
        }
    }

    private fun varDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        var initialiser: Expr? = null
        if (match(TokenType.EQUAL)) {
            initialiser = expression()
        }

        consume(TokenType.SEMICOLON, "Expect ; after variable declaration.")
        return Stmt.Var(name, initialiser)
    }

    private fun statement(): Stmt {
        if (match(TokenType.IF)) return ifStatement()
        if (match(TokenType.PRINT)) return printStatement()
        if (match(TokenType.WHILE)) return whileStatement()
        if (match(TokenType.LEFT_BRACE)) return Stmt.Block(block())
        if (match(TokenType.FOR)) return forStatement()
        return expressionStatement()
    }

    private fun forStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after for keyword.")
        val init: Stmt? = if (match(TokenType.SEMICOLON))
            null
        else if (match(TokenType.VAR))
            varDeclaration()
        else
            expressionStatement()

        var condition: Expr? = if (!check(TokenType.SEMICOLON))
            expression()
        else null

        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")

        val increment: Expr? = if (!check(TokenType.RIGHT_PAREN))
            expression()
        else null

        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.")

        var body = statement()

        if (increment != null) {
            body = Stmt.Block(listOf(body, Stmt.Expression(increment)))
        }

        if (condition == null) condition = Expr.Literal(true)
        body = Stmt.While(condition, body)

        if (init != null)
            body = Stmt.Block(listOf(init, body))

        return body

    }

    private fun whileStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after while keyword.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after while condition.")
        val body = statement()
        return Stmt.While(condition, body)
    }

    private fun block(): ArrayList<Stmt> {
        val statements = ArrayList<Stmt>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            declaration()?.let { statements.add(it) }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun ifStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expected '(' after if keyword.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expected ')' after if condition.")

        val thenBranch = statement()
        var elseBranch: Stmt? = null
        if (match(TokenType.ELSE)) // avoids dangling else problem by immediately parsing else
            elseBranch = statement()
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun printStatement(): Stmt {
        val exp = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(exp)
    }

    private fun expressionStatement(): Stmt {
        val exp = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(exp)
    }
    private fun expression(): Expr {
        return assignment()
    }

    private fun assignment(): Expr {
        val exp = or()

        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (exp is Expr.Variable) {
                val name = exp.name
                return Expr.Assign(name, value)
            }

            error(equals, "Invalid assignment target.")
        }
        return exp

    }

    private fun or(): Expr {
        var exp = and()

        while (match(TokenType.OR)) {
            val operator = previous()
            val right = and()
            exp = Expr.Logical(exp, operator, right)
        }
        return exp
    }

    private fun and(): Expr {
        var exp = equality()

        while (match(TokenType.AND)) {
            val operator = previous()
            val right = equality()
            exp = Expr.Logical(exp, operator, right)
        }
        return exp
    }

    private fun equality(): Expr {
        var exp = comparison()
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            exp = Expr.Binary(exp, operator, right)
        }
        return exp
    }

    private fun comparison(): Expr {
        var exp = term()
        while (match(TokenType.GREATER, TokenType.EQUAL_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous() // since the token pointer has been advanced already by match
            val right = term()
            exp = Expr.Binary(exp, operator, right)
        }
        return exp
    }

    private fun term(): Expr {
        var exp = factor()
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = previous()
            val right = factor()
            exp = Expr.Binary(exp, operator, right)
        }
        return exp
    }

    private fun factor(): Expr {
        var exp = unary()
        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous()
            val right = unary()
            exp = Expr.Binary(exp, operator, right)
        }
        return exp
    }

    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)

        }
        return call()
    }

    private fun call(): Expr {
        var expr = primary()
        consume(TokenType.LEFT_PAREN, "Expected '(' after callee.")

        while (true) {
            if (match(TokenType.LEFT_PAREN))
                expr = finishCall(expr)
            else {
                break
            }
        }
        return expr
    }

    private fun finishCall(callee: Expr): Expr {
        val args = ArrayList<Expr>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (args.size >= 255)
                    error(peek(), "Can't have more than 255 arguments.")
                args.add(expression())
            } while (match(TokenType.COMMA))
        }

        val paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")

        return Expr.Call(callee, paren, args)

    }

    private fun primary(): Expr {
        if (match(TokenType.FALSE)) return Expr.Literal(false)
        if (match(TokenType.TRUE)) return Expr.Literal(true)
        if (match(TokenType.NIL)) return Expr.Literal(null)

        if (match(TokenType.NUMBER, TokenType.STRING)) return Expr.Literal(previous().literal)

        if (match(TokenType.IDENTIFIER)) return Expr.Variable(previous())
        if (match(TokenType.LEFT_PAREN)) {
            val exp = expression()
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.")
            return Expr.Grouping(exp)
        }

        throw error(peek(), "Expected expression.")
    }

    // Error handling
    class ParseError: RuntimeException()
    // consume tokens until we get to the next boundary to continue parsing after error
    private fun synchronise() {
        advance()
        while(!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return

            when (peek().type) {
                TokenType.CLASS -> return
                TokenType.FUN -> return
                TokenType.VAR -> return
                TokenType.FOR -> return
                TokenType.IF -> return
                TokenType.WHILE -> return
                TokenType.PRINT -> return
                TokenType.RETURN -> return
                else ->advance()
            }
        }
    }

    private fun error(token: Token, message: String): ParseError {
        if (token.type == TokenType.EOF)
            errorList.add("Parse error at line ${token.line} at end: $message")
        else {
            errorList.add("Parse error at line ${token.line} at token: '${token.lexeme}': $message")
        }
        return ParseError()
    }

    // Helper methods
    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }
    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }
    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun check(type: TokenType): Boolean {
        // doesn't consume the token
        if (isAtEnd()) return false
        return peek().type == type
    }
    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token = tokens[current - 1]

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF

}