class Interpreter(var hadError: Boolean = false, var globals: Environment = Environment(null)): Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    private val LOGGING = false
    private var env = globals
    init {
        globals.define("clock", object: LoxCallable {
            override fun arity(): Int = 0

            override fun call(interpreter: Interpreter, args: ArrayList<Any?>): Any {
                return System.currentTimeMillis().toDouble() / 1000.0
            }

            override fun toString(): String = "<native fn>"

        })
    }

    fun interpret(stmts: ArrayList<Stmt?>?) {
        try {
            if (stmts != null) {
                for (stmt in stmts)
                    stmt?.let { execute(it) }
            }
        }

        catch (error: RuntimeError) {
            println("${error.msg}: [line ${error.token.line}]")
            hadError = true
        }
    }

    override fun visitReturnStatement(stmt: Stmt.Return) {
        var value: Any? = null
        if (stmt.value != null) {
            value = evaluate(stmt.value)
        }

        throw Return(value)
    }
    class Return(public val value: Any?): RuntimeException(null, null, false, false)
    override fun visitFunctionStatement(stmt: Stmt.Function) {
        val function = LoxFunction(stmt)
        env.define(stmt.name.lexeme, function)
        if(LOGGING)
            logEnvironment(env)
    }

    override fun visitBreakStatement(stmt: Stmt.Break) {
        return
    }
    override fun visitCallExpr(exp: Expr.Call): Any? {
        val callee = evaluate(exp.callee)

        val args = ArrayList<Any?>()
        for (argument in exp.args) {
            args.add(evaluate(argument))
        }

        if (callee !is LoxCallable)
            throw RuntimeError(exp.paren, "Can only call functions and classes.")
        if (args.size != callee.arity())
            throw RuntimeError(exp.paren, "Expected ${callee.arity()} arguments but got ${args.size}")
        return callee.call(this, args)
    }

    override fun visitWhileStatement(stmt: Stmt.While) {
        while (isTruthy(stmt.condition?.let { evaluate(it) }))
            execute(stmt.body)
    }

    override fun visitIfStatement(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitBlockStatement(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(env))
    }

    fun executeBlock(statements: List<Stmt>, env: Environment) {
        val previous = this.env

        try {
            this.env = env
            statements.forEach {execute(it)} // for each applies the function in place
        }
        finally {
            if (LOGGING)
                logEnvironment(this.env)
            this.env = previous
        }
    }

    override fun visitVarStatement(stmt: Stmt.Var) {
        var value : Any? = null
        if (stmt.initialiser != null)
            value = evaluate(stmt.initialiser)
        env.define(stmt.name.lexeme, value)
    }
    override fun visitExpressionStatement(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitPrintStatement(stmt: Stmt.Print) {
        val obj = evaluate(stmt.expression)
        println(stringify(obj))
    }

    override fun visitAssignExpr(exp: Expr.Assign) : Any? {
        val value = evaluate(exp.value)
        env.assign(exp.name, value)
        return value
    }

    override fun visitLogicalExpr(exp: Expr.Logical): Any? {
        val left = evaluate(exp.left)
        if (exp.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
            }
        return evaluate(exp.right)
    }

    override fun visitVariableExpr(exp: Expr.Variable): Any? {
        return env.get(exp.name)
    }
    override fun visitLiteralExpr(exp: Expr.Literal) : Any? {
        return exp.value
    }

    override fun visitGroupingExpr(exp: Expr.Grouping): Any? {
        return evaluate(exp.expression)
    }

    override fun visitUnaryExpr(exp: Expr.Unary): Any? {
        val right: Any? = evaluate(exp.right)

        return when (exp.operator.type) {
            TokenType.MINUS -> -(right as Double)
            TokenType.BANG -> !isTruthy(right)
            else -> null
        }
    }

    override fun visitBinaryExpr(exp: Expr.Binary): Any? {
        val left: Any? = evaluate(exp.left)
        val right: Any? = evaluate(exp.right)

        when (exp.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperand(exp.operator, right)
                return (left as Double) - (right as Double)
            }
            TokenType.SLASH -> {
                checkNumberOperands(exp.operator, left, right)
                return (left as Double) / (right as Double)
            }
            TokenType.STAR -> {
                checkNumberOperands(exp.operator, left, right)
                return (left as Double) * (right as Double)
            }
            TokenType.PLUS -> {
                if (left is Double && right is Double)
                    return (left + right)
                if (left is String && right is String)
                    return (left + right)
                throw RuntimeError(exp.operator, "Operands must be 2 numbers or 2 strings")
            }
            TokenType.GREATER -> {
                checkNumberOperands(exp.operator, left, right)
                return (left as Double) > (right as Double)
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(exp.operator, left, right)
                return (left as Double) >= (right as Double)
            }
            TokenType.LESS -> {
                checkNumberOperands(exp.operator, left, right)
                return (left as Double) < (right as Double)
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(exp.operator, left, right)
                return (left as Double) <= (right as Double)
            }
            TokenType.BANG -> {
                checkNumberOperands(exp.operator, left, right)
                !isEqual(left, right)
            }
            TokenType.BANG_EQUAL -> {
                checkNumberOperands(exp.operator, left, right)
                !isEqual(left, right)
            }
            else -> null
        }
        return null
    }

    private fun isTruthy(obj: Any?) : Boolean {
        if (obj == null) return false
        if (obj is Boolean) return obj
        return true
    }
    private fun evaluate(exp: Expr) : Any? {
        return exp.accept(this)
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }
    private fun isEqual(a: Any?, b: Any?) : Boolean {
        if (a == null && b == null) return true
        if (a == null) return false
        return a.equals(b)
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers for operator")
    }
    class RuntimeError(val token: Token, val msg: String) : RuntimeException(msg)

    private fun stringify(obj: Any?): String {
        if (obj == null) return "nil"

        if (obj is Double) {
            var text = obj.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }
        return obj.toString()
    }

    private fun logEnvironment(env: Environment) {
        val stackTrace = Thread.currentThread().stackTrace
        val calleeName = stackTrace[1].methodName
        val callerName = stackTrace[2].methodName

        // walk the linked list of environments
        println("$calleeName called by $callerName")
        var currentEnv: Environment? = env
        var count = 0

        println("printing variables:")
        while (currentEnv?.enclosing != null) {

            println("Content of scope $count from inner")
            for ((name, value) in env.values)
                println("Name: $name, Value: $value")

            currentEnv = currentEnv.enclosing
            count++

        }
    }
}