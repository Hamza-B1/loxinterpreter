abstract class Stmt {
    interface Visitor<R> {
        fun visitBlockStatement(stmt: Block): R
        fun visitExpressionStatement(stmt: Expression): R
        fun visitPrintStatement(stmt: Print): R
        fun visitVarStatement(stmt: Var): R
        fun visitIfStatement(stmt: If): R
        fun visitEmptyStatement(stmt: Empty): R
    }

    class Empty : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitEmptyStatement(this)
        }
    }
    class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?): Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitIfStatement(this)
        }
    }
    class Block(val statements: List<Stmt>) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBlockStatement(this)
        }
    }
    class Var(val name: Token, val initialiser: Expr?) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVarStatement(this)
        }
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