class Parser(private val tokens: List<Token>) {
    private var current: Int = 0
    private var isAtEnd = peek().type == TokenType.EOF
    private var previous = tokens[current - 1]

    /*
    CONTEXT FREE GRAMMAR FOR PARSER
    expression     → equality ;
    equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    term           → factor ( ( "-" | "+" ) factor )* ;
    factor         → unary ( ( "/" | "*" ) unary )* ;
    unary          → ( "!" | "-" ) unary | primary ;
    primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;

    with ternary
    // parse ? expr : kinda like grouping expression
    //
    expression     → equality ;
    equality       → ternary ( ( "!=" | "==" ) ternary )* ;
    ternary        → comparison ("?") comparison (":") comparison ;
    comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    term           → factor ( ( "-" | "+" ) factor )* ;
    factor         → unary ( ( "/" | "*" ) unary )* ;
    unary          → ( "!" | "-" ) unary | primary ;
    primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
    */
    private fun expression(): Expr {
        return equality()
    }

    private fun equality(): Expr {
        var exp = comparison()
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = previous
            val right = comparison()
            exp = Expr.Binary(exp, operator, right)
        }
        return exp
    }

    private fun comparison(): Expr {
        var exp = term()
        while (match(TokenType.GREATER, TokenType.EQUAL_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous // since the token pointer has been advanced already by match
            val right = term()
            exp = Expr.Binary(exp, operator, right)

        }
        return exp
    }

    private fun term(): Expr {
        var exp = factor()
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = previous
            val right = factor()
            exp = Expr.Binary(exp, operator, right)
        }
        return exp
    }

    private fun factor(): Expr {
        var exp = unary()
        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous
            val right = unary()
            exp = Expr.Binary(exp, operator, right)
        }
        return exp
    }

    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous
            val right = unary()
            return Expr.Unary(operator, right)

        }
        return primary()
    }

    private fun primary(): Expr {
        if (match(TokenType.FALSE)) return Expr.Literal(false)
        if (match(TokenType.TRUE)) return Expr.Literal(true)
        if (match(TokenType.NIL)) return Expr.Literal(null)

        if (match(TokenType.NUMBER, TokenType.STRING)) return Expr.Literal(previous.literal)

        if (match(TokenType.LEFT_PAREN)) {
            val exp = expression()
            // consume
            return Expr.Grouping(exp)
        }
        throw Exception()
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
        if (!isAtEnd) current++
        return previous
    }

    private fun check(type: TokenType): Boolean {
        // doesn't consume the token
        if (isAtEnd) return false
        return peek().type == type
    }
    private fun peek(): Token {
        return tokens[current]
    }

}