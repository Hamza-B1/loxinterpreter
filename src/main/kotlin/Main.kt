import java.io.BufferedReader
import java.io.File
import kotlin.system.exitProcess

// basic functionality for interpreter
fun main(args: Array<String>) {
    var hadScanError = false
    var hadParseError = false
    var scannerErrors: ArrayList<String> = ArrayList()
    var parserErrors: ArrayList<String> = ArrayList()

    fun run(code: String) {
        val scanner = Scanner(code)
        val tokens: List<Token> = scanner.scanTokens()
        for (token in tokens) {
            println(token.toString())
        }

        if (scanner.errorList.isNotEmpty()) {
            scannerErrors = scanner.errorList
            hadScanError = true
        }

        val parser = Parser(tokens)
        parser.parse()
        if (parser.errorList.isNotEmpty()) {
            parserErrors = parser.errorList
            hadParseError = true
        }
        // do not continue if the parser threw an error

    }

    fun runPrompt() {
        while (true) {
            println("> ")
            val line: String = readln()
            if (line == "") break
            run(line)
        }
    }

    fun runFile(path: String) {
        val reader: BufferedReader = File(path).bufferedReader()
        val code = reader.use { it.readText() }
        run(code)
    }

    // true entry point
    if (args.size > 1) {
        println("Usage: loxinterpreter [script]")
    } else if (args.size == 1) {
        runFile(args[0])
    }
    else runPrompt()
    if (hadParseError || hadScanError) {
        println(scannerErrors.joinToString(" "))
        println(parserErrors.joinToString(" "))
        exitProcess(65)
    }

//    val e: Expr = Expr.Binary(
//            Expr.Unary(
//                    Token(TokenType.MINUS, "-", null, 1),
//                    Expr.Literal(123)
//            ),
//            Token(TokenType.STAR, "*", null, 1),
//            Expr.Grouping(
//                    Expr.Literal(45.67)
//            )
//    )
//
//    val p = AstPrinter()
//    println(p.print(e))

}