class Environment(private var enclosing: Environment?) {
    constructor(): this(null)

    private var values: HashMap<String, Any?> = HashMap()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme] // search the current scope
        }

        enclosing?.let {get(name)}

        throw Interpreter.RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    // assign is only for assigning to existing variables
    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = values
        }

        enclosing?.let {
            assign(name, value)
            return
        }

        throw Interpreter.RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
}