abstract class Stmt {
    interface Visitor<R> {
        fun visitBlockStatement(stmt: Block): R
        fun visitExpressionStatement(stmt: Expression): R
        fun visitPrintStatement(stmt: Print): R
        fun visitVarStatement(stmt: Var): R
        fun visitIfStatement(stmt: If): R
        fun visitWhileStatement(stmt: While): R
        fun visitBreakStatement(stmt: Break): R
        fun visitFunctionStatement(stmt: Function): R
    }

    class Function(val name: Token, val params: ArrayList<Token>, val body: ArrayList<Stmt>): Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitFunctionStatement(this)
        }
    }

    class Break: Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBreakStatement(this)
        }
    }

    class While(val condition: Expr?, val body: Stmt) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitWhileStatement(this)
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

    class Expression(val expression: Expr) : Stmt() {
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