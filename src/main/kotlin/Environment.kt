class Environment(private var values: HashMap<String, Any>) {
    fun define(name: String, value: Any) {
        values[name] = value
    }

    fun get() {

    }
}