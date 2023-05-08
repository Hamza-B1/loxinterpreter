import java.io.BufferedReader
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    var hadScanError = false
    var hadParseError = false
    var hadRuntimeError = false

    var scannerErrors: ArrayList<String> = ArrayList()
    var parserErrors: ArrayList<String> = ArrayList()

    fun run(code: String) {
        val scanner = Scanner(code)
        val tokens: List<Token> = scanner.scanTokens()

        if (scanner.errorList.isNotEmpty()) {
            scannerErrors = scanner.errorList
            hadScanError = true
        }

        val parser = Parser(tokens)
        val exp = parser.parse()
        if (parser.errorList.isNotEmpty()) {
            parserErrors = parser.errorList
            hadParseError = true
            return
        }
        val interpreter = Interpreter()
        if (exp != null) {
            interpreter.interpret(exp)
        }

        hadRuntimeError = interpreter.hadError
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
    } else runPrompt()
    if (hadParseError || hadScanError) {
        println(scannerErrors.joinToString(" "))
        println(parserErrors.joinToString(" "))
        exitProcess(65)
    }
    if (hadRuntimeError) {
        exitProcess(70)
    }
}