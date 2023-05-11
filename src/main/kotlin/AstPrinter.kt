//class AstPrinter : Expr.Visitor<String> {
//    fun print(exp: Expr): String {
//        return exp.accept(this)
//    }
//    override fun visitBinaryExpr(exp: Expr.Binary): String {
//        return parenthesise(exp.operator.lexeme, exp.left, exp.right)
//    }
//    override fun visitGroupingExpr(exp: Expr.Grouping): String {
//        return parenthesise("grouping", exp.expression)
//    }
//    override fun visitLiteralExpr(exp: Expr.Literal): String {
//        if (exp.value == null) return "nil"
//        return exp.value.toString()
//    }
//    override fun visitUnaryExpr(exp: Expr.Unary): String {
//        return parenthesise(exp.operator.lexeme, exp.right)
//    }
//
//    private fun parenthesise(name: String, vararg exp: Expr): String {
//        val builder: StringBuilder = StringBuilder()
//        builder.append("(").append(name)
//        for (expr in exp) {
//            builder.append(" ")
//            builder.append(expr.accept(this))
//        }
//        builder.append(")")
//        return builder.toString()
//    }
//}