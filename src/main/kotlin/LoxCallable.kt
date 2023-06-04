interface LoxCallable {
    fun call(interpreter: Interpreter, args: ArrayList<Any?>): Any?
    fun arity(): Int
}