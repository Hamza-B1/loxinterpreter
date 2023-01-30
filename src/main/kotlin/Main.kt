import java.io.BufferedReader
import java.io.File
import kotlin.system.exitProcess

// basic functionality for interpreter
fun main(args: Array<String>) {
    // error handling
    var hadError = false
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
            hadError = true
        }
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


    if (args.size > 1) {
        println("Usage: loxinterpreter [script]")
    } else if (args.size == 1) {
        runFile(args[0])
    }
    else runPrompt()
    if (hadError) {
        println(scannerErrors.joinToString("\n"))
        exitProcess(65)
    }
}