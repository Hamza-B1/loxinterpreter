class Scanner(private val source: String) {

    private var tokens: ArrayList<Token> = ArrayList()
    private var start: Int = 0
    private var current: Int = 0
    private var line: Int = 1
    var errorList: ArrayList<String> = ArrayList()
    private val isAtEnd: Boolean
        get() = current >= source.length
    fun scanTokens(): List<Token> {
        while (!isAtEnd) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }
    private fun scanToken() {
        var c = advance() // now the current pointer is at the next character
        when (c) {
            '('-> addToken(TokenType.LEFT_PAREN)
            ')'-> addToken(TokenType.RIGHT_PAREN)
            '{'-> addToken(TokenType.LEFT_BRACE)
            '}'-> addToken(TokenType.RIGHT_BRACE)
            ','-> addToken(TokenType.COMMA)
            '.'-> addToken(TokenType.DOT)
            '-'-> addToken(TokenType.MINUS)
            '+'-> addToken(TokenType.PLUS)
            ';'-> addToken(TokenType.SEMICOLON)
            '*'-> addToken(TokenType.STAR)

            '!'-> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '='-> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<'-> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>'-> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)

            '/'-> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd) advance()
                } else addToken(TokenType.SLASH)
            }
            ' '-> {}
            '\r'-> {}
            '\t'-> {}
            '\n'-> line++

            '"'-> string()

            else -> {
                errorList.add("Unexpected character $c at line $line")
            }
        }
    }
    private fun string() {
        while (peek() != '"' && !isAtEnd) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd) {
            errorList.add("unterminated string at line $line")
            return
        }

        // we have reached the closing "
        advance()
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }
    private fun match(expected: Char): Boolean {
        if (isAtEnd) return false
        if (source[current] != expected) return false
        current++
        return true
    }
    private fun advance() = source[current++]
    private fun addToken(type: TokenType, literal: Any? = null) {
        val text: String = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }
    private fun peek(): Char = if (isAtEnd) '\n' else source[current]
}