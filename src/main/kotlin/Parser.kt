class Parser(private val tokens: List<Token>) {
    private var current: Int = 0
    private var isAtEnd = peek().type == TokenType.EOF
    var errorList: ArrayList<String> = ArrayList()

    /*
    CONTEXT FREE GRAMMAR FOR PARSER
    program        → statement* EOF ;
    statement      → exprStmt | printStmt ;
    exprStmt       → expression ";" ;
    printStmt      → "print" expression ";" ;
    expression     → equality ;
    equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    term           → factor ( ( "-" | "+" ) factor )* ;
    factor         → unary ( ( "/" | "*" ) unary )* ;
    unary          → ( "!" | "-" ) unary | primary ;
    primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;

    with ternary?? TBC
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

    // Expression parsers that map to each grammar rule
    fun parse(): ArrayList<Stmt> {
        var stmts = ArrayList<Stmt>()
        while (!isAtEnd) {
            stmts.add(statement())
        }
        return stmts
    }

    private fun statement(): Stmt {
        if (match(TokenType.PRINT)) return printStatement()
        return expressionStatement()
    }

    private fun printStatement(): Stmt {
        var exp = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value")
        return Stmt.Print(exp)
    }

    private fun expressionStatement(): Stmt {
        val exp = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression")
        return Stmt.Expression(exp)
    }
    private fun expression(): Expr {
        return equality()
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
        return primary()
    }

    private fun primary(): Expr {
        if (match(TokenType.FALSE)) return Expr.Literal(false)
        if (match(TokenType.TRUE)) return Expr.Literal(true)
        if (match(TokenType.NIL)) return Expr.Literal(null)

        if (match(TokenType.NUMBER, TokenType.STRING)) return Expr.Literal(previous().literal)

        if (match(TokenType.LEFT_PAREN)) {
            val exp = expression()
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.")
            return Expr.Grouping(exp)
        }

        throw error(peek(), "Expected expression")
    }

    // Error handling
    class ParseError: RuntimeException()
    // consume tokens until we get to the next boundary to continue parsing after error
    private fun synchronise() {
        advance()
        while(!isAtEnd) {
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
            errorList.add("Parse error at line " + token.line.toString() + "at end " + message)
        else {
            errorList.add("Parse Error at line " + token.line.toString() +" at token: '" + token.lexeme + "';" + message)
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
        if (!isAtEnd) current++
        return previous()
    }

    private fun check(type: TokenType): Boolean {
        // doesn't consume the token
        if (isAtEnd) return false
        return peek().type == type
    }
    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token = tokens[current - 1]

}