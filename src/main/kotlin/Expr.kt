abstract class Expr {
    abstract fun <R> accept(visitor: Visitor<R>): R
    interface Visitor<R> {
        fun visitBinaryExpr(exp: Binary): R
        fun visitGroupingExpr(exp: Grouping): R
        fun visitLiteralExpr(exp: Literal): R
        fun visitUnaryExpr(exp: Unary): R
    }

    class Literal(val value: Any?) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLiteralExpr(this)
        }
    }

    class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBinaryExpr(this)
        }
    }

    class Unary(val operator: Token, val right: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitUnaryExpr(this)
        }
    }

    class Grouping(val expression: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitGroupingExpr(this)
        }
    }


}
