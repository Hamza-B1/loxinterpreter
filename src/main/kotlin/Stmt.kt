abstract class Stmt {
    interface Visitor<R> {
        fun visitExpressionStatement(stmt: Expression): R
        fun visitPrintStatement(stmt: Print): R
    }

    class Expression(var expression: Expr) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitExpressionStatement(this)
        }
    }

    class Print(val expression: Expr) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitPrintStatement(this)
        }
    }

    abstract fun <R> accept(visitor: Visitor<R>): R




}