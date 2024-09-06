package lox;

import lox.Expr.Assignment;
import lox.Expr.Call;
import lox.Expr.Get;
import lox.Expr.Logical;
import lox.Expr.PostOp;
import lox.Expr.PreOp;
import lox.Expr.Set;
import lox.Expr.Super;
import lox.Expr.This;

class RPNPrinter implements Expr.Visitor<String> {
    String print(Expr expr){
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr){
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr){
        return expr.operator.lexeme.toString() + expr.right.accept(this);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr){
        return expr.expression.accept(this);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr){
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr){
        if (expr.name == null) return "nil";
        return expr.name.toString();
    }

    private String parenthesize(String name, Expr... exprs){
        StringBuilder builder = new StringBuilder();

        for (Expr expr : exprs){
            builder.append(" ");
            builder.append(expr.accept(this));
        }

        builder.append(name);

        return builder.toString();
    }

    @Override
    public String visitAssignmentExpr(Assignment expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitAssignmentExpr'");
    }

    @Override
    public String visitLogicalExpr(Logical expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitLogicalExpr'");
    }

    @Override
    public String visitPostOpExpr(PostOp expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitPostOpExpr'");
    }

    @Override
    public String visitPreOpExpr(PreOp expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitPreOpExpr'");
    }

    @Override
    public String visitCallExpr(Call expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
    }

    @Override
    public String visitGetExpr(Get expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitGetExpr'");
    }

    @Override
    public String visitSetExpr(Set expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSetExpr'");
    }

    @Override
    public String visitThisExpr(This expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitThisExpr'");
    }

    @Override
    public String visitSuperExpr(Super expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSuperExpr'");
    }

}