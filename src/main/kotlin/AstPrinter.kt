class AstPrinter : Expr.Visitor<String> {
    public fun print(exp: Expr): String {
        return exp.accept(this)
    }
    override fun visitLiteralExpr(exp: Expr.Literal): String {
        TODO("Not yet implemented")
    }
    override fun visitBinaryExpr(exp: Expr.Binary): String {
        return parenthesise(exp.operator.lexeme)
    }
    override fun visitUnaryExpr(exp: Expr.Unary): String {
        TODO("Not yet implemented")
    }
    override fun visitGroupingExpr(exp: Expr.Grouping): String {
        TODO("Not yet implemented")
    }
    private fun parenthesise(name: String, vararg exp: Expr): String {
        return String()
    }
}