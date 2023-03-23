class Scanner(private val source: String) {

    private var tokens: ArrayList<Token> = ArrayList()
    // start and current used for two-pointer traversal to parse tokens of length > 2
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
        when (val c = advance()) { // now the current pointer is at the next character
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

            // comments and multiline comments
            '/'-> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd) advance()
                }
                else if (match('*')) { // consume the asterisk
                    while (peek() != '*' && !isAtEnd) {
                        if (peek() == '\n') line++
                        advance()
                    }
                    if (peekNext() == '/')
                    {
                        val s: String = source.substring(start + 2, current)
                        advance() // consume the trailing asterisk and slash
                        advance()
                    }
                } else addToken(TokenType.SLASH)
            }

            ' '-> {}
            '\r'-> {}
            '\t'-> {}
            '\n'-> line++

            '"'-> string()

            else -> {
                if (isDigit(c)) {
                    number()
                }

                else if (isAlpha(c)) {
                    identifier()
                }
                else errorList.add("Unexpected character '$c' at line $line")
            }
        }
    }

    private fun isAlpha(c: Char): Boolean = c in 'a' .. 'z' || c == '_' || c in 'A' .. 'Z'
    private fun isAlphaNumeric(c: Char) : Boolean = isAlpha(c) || isDigit(c)
    private fun number() {
        while (isDigit(source[current])) advance()

        if (peek() == '.' && isDigit(peekNext())) {
            advance() // consume decimal point
            while(isDigit(source[current])) advance()
        }
        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }
    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        val substring = source.substring(start, current)
        addToken(keywords[substring] ?: TokenType.IDENTIFIER)
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
    private fun peekNext(): Char = if (current + 1 > source.length) '\u0000' else source[current + 1]
    private fun peek(): Char = if (isAtEnd) '\u0000' else source[current]
    private fun isDigit(c: Char): Boolean = c in '0'..'9' // standard lib impl allows weird stuff like full width numbers

    private val keywords: HashMap<String, TokenType>
        get() = hashMapOf(
            "and" to TokenType.AND,
            "class" to TokenType.CLASS,
            "else" to TokenType.ELSE,
            "false" to TokenType.FALSE,
            "for" to TokenType.FOR,
            "fun" to TokenType.FUN,
            "if" to TokenType.IF,
            "nil" to TokenType.NIL,
            "or" to TokenType.OR,
            "print" to TokenType.PRINT,
            "return" to TokenType.RETURN,
            "super" to TokenType.SUPER,
            "this" to TokenType.THIS,
            "true" to TokenType.TRUE,
            "var" to TokenType.VAR,
            "while" to TokenType.WHILE
        )
}