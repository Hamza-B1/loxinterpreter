class Environment(var enclosing: Environment?) {

    var values: HashMap<String, Any?> = HashMap()
        private set

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme] // search the current scope
        }

        if (enclosing != null)
            return enclosing!!.get(name)

        throw Interpreter.RuntimeError(name, "Undefined variable '${name.lexeme}'")
    }

    // assign is only for assigning to existing variables
    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        if (enclosing != null) {
            enclosing!!.assign(name, value)
            return
        }

        throw Interpreter.RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
}