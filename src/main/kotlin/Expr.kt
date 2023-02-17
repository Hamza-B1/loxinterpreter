abstract class Expr {
    interface Visitor<R> {

    }
    class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R? {
            return null
        }
    }

    abstract fun <R> accept(visitor: Visitor<R>): R?


}
