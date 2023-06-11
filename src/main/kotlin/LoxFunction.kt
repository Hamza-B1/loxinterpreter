class LoxFunction(private val declaration: Stmt.Function) : LoxCallable {

    override fun call(interpreter: Interpreter, args: ArrayList<Any?>) {
        val env = interpreter.globals
        for (i in 0 until declaration.params.size) {
            env.define(declaration.params[i].lexeme, args[i])
        }

        interpreter.executeBlock(declaration.body, env)
    }

    override fun arity(): Int = declaration.params.size

    override fun toString(): String = "<fn ${declaration.name.lexeme}>"
}