package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import lox.Expr.Get;

public class Resolver implements
    Expr.Visitor<Void>, Stmt.Visitor<Void>
{
    private final Interpreter interpreter;
    private final Stack<HashMap<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    Resolver(Interpreter interpreter){
        this.interpreter = interpreter;
    }

    private static enum FunctionType {
        NONE,
        FUNCTION,
        INITIALIZER,
        METHOD
    };

    private static enum ClassType {
        NONE,
        CLASS
    };

    @Override
    public Void visitBlockStmt(Stmt.Block stmt){
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt){
        declare(stmt.identifier);
        if (stmt.expression != null)
            resolve(stmt.expression);
        define(stmt.identifier);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr){
        if (!scopes.isEmpty() && 
            scopes.peek().get(expr.name.lexeme) == Boolean.FALSE)
            Lox.error(expr.name, "Can't read local variable in its own initalizer");
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitAssignmentExpr(Expr.Assignment expr){
        resolve(expr.expression);
        resolveLocal(expr, expr.identifier);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt){
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt){
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt){
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt){
        if (currentFunction == FunctionType.NONE){
            Lox.error(stmt.keyword, "return outside of a function");
        }
        if (stmt.expression != null){
            if (currentFunction == FunctionType.INITIALIZER)
                Lox.error(stmt.keyword, "Cannot return a value from initalizer");
            resolve(stmt.expression);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt){
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt){
        declare(stmt.name);
        define(stmt.name);
        
        ClassType encClassType = currentClass;
        currentClass = ClassType.CLASS;
        beginScope();
            scopes.peek().put("this", true);
            for (Stmt.Function method : stmt.methods){
                FunctionType decl = FunctionType.METHOD;
                if (method.name.lexeme.equals(LoxClass.constructorName))
                    decl = FunctionType.INITIALIZER;
                resolveFunction(method, decl);
            }
        endScope();
        currentClass = encClassType;
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr){
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr){
        resolve(expr.callee);
        for (Expr arg : expr.arguments)
            resolve(arg);
        return null;
    }

    @Override
    public Void visitGetExpr(Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr){
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr){
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr){
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr){
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr){
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitPreOpExpr(Expr.PreOp expr){
        resolveLocal(expr, expr.identifier);
        return null;
    }

    @Override
    public Void visitPostOpExpr(Expr.PostOp expr){
        resolveLocal(expr, expr.identifier);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr){
        if (currentClass != ClassType.CLASS)
            Lox.error(expr.keyword, "'this' keyword outside of a class");
        resolveLocal(expr, expr.keyword);
        return null;
    }

    private void resolveFunction(Stmt.Function stmt, FunctionType type)
    {
        FunctionType enclosingType = currentFunction;
        currentFunction = type;
        beginScope();
        for (Token param : stmt.params){
            declare(param);
            define(param);
        }
        resolve(stmt.body);
        endScope();
        currentFunction = enclosingType;
    }

    private void resolveLocal(Expr expr, Token name){
        for (int i =scopes.size()-1; i >= 0; i--){
            if (scopes.get(i).containsKey(name.lexeme)){
                interpreter.resolve(expr, scopes.size() - i - 1);
                return;
            }
        }
    }

    void resolve(List<Stmt> statements){
        for (Stmt statement : statements)
            resolve(statement);
    }

    private void declare(Token name){
        if (scopes.isEmpty()) return;
        if (scopes.peek().containsKey(name.lexeme))
            Lox.error(name, "redeclaration of variable");
        // Map<String, Boolean> scope = scopes.peek();
        scopes.peek().put(name.lexeme, false);
    }

    private void define(Token name){
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }

    private void resolve(Stmt stmt){
        stmt.accept(this);
    }

    void resolve(Expr expr){
        expr.accept(this);
    }

    private void beginScope(){
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope(){
        scopes.pop();
    }

}