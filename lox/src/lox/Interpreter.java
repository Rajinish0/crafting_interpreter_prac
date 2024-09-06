package lox;

import static lox.TokenType.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import lox.Expr.Assignment;
import lox.Expr.Binary;
import lox.Expr.Grouping;
import lox.Expr.Literal;
import lox.Expr.Unary;
import lox.Expr.Variable;


/*
 * THIS IS AN INTERPRETER FOR THE AST
 * 
 * It is important to note that statement() is being called after 
 * each conditional thing like if or while
 * 
 * this stops declarations from being made there so they cant sneak into the 
 * global scope; however, statement itself can be a block 
 * 
 * and if '{' is detected then it triggers the formation of a new block 
 * and hence a new local scope
 */
public class Interpreter implements Expr.Visitor<Object>,
                                    Stmt.Visitor<Void>
    {

    // private static HashMap<Object, Object> variables = new HashMap<>();
    final Environment globals = new Environment();
    private Environment env = globals;
    private final HashMap<Expr, Integer> locals = new HashMap<>();

    public Interpreter(){
        // std library functions 
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {return 0;}

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments){
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn>";}
        });

        globals.define("input", new LoxCallable() {
            @Override
            public int arity() {return 0;}

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments)
            {
                try (Scanner scanner = new Scanner(System.in)) {
                    return scanner.nextLine();
                }
            }

            @Override
            public String toString() {return "<native fn>";}
        });
    }

    void interpret(List<Stmt> statements) { 
        try {
            for (Stmt stmt : statements)
                stmt.accept(this);
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private String stringify(Object value){
        if (value == null) return "nil";

        if (value instanceof Double){
            String numText = value.toString();
            // it is an integer
            if (numText.endsWith(".0")){
                numText = numText.substring(0, numText.length() - 2);
            } 

            return numText;
        }

        return value.toString();

    }

    private Object evaluate(Expr expr){
        return expr.accept(this);
    }

    public void resolve(Expr expr, int depth){
        locals.put(expr, depth);
        return;
    }

    private void execute(Stmt statement){
        statement.accept(this);
    }

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return ((double)left + (double)right);
                else if (left instanceof String && right instanceof String)
                    return ((String)left + (String)right);
                throw new RuntimeError(expr.operator, "Operands must be number or string");
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return ((double)left - (double)right);
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return ((double)left * (double)right);
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return ((double)left / (double)right);
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return ((double)left > (double)right);
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return ((double)left >= (double)right);
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return ((double)left < (double)right);
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return ((double)left <= (double)right);
            case EXCLAM_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            default:
                break;
        }
        return null;
    }

    private boolean isEqual(Object left, Object right){
        if (left == null && right == null) return true;
        if (left == null) return false;
        return left.equals(right);
    }

    private void checkNumberOperand(Token operator, Object operand){
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right){
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be of type number");
    }

    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.value;
    }

    private boolean isTruthy(Object object){
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.operator.type){
            case EXCLAM: 
                return (!isTruthy(right));
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return(-(double)right);
            default:
                return null;
        }
    }

    @Override
    public Object visitVariableExpr(Variable expr){
        // return variables.get(expr.name.literal);
        // return env.get(expr.name);
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr){
        Integer dist = locals.get(expr);
        if (dist != null)
            return env.getAt(dist, name.lexeme);
        else
            return globals.get(name);
    }

    @Override
    public Object visitPreOpExpr(Expr.PreOp expr){
        // Object val = env.get(expr.identifier);
        Object val = lookUpVariable(expr.identifier, expr);
        checkNumberOperand(expr.operator, val);

        double newVal = (expr.operator.type == PLUS_PLUS) ? ((double)(val) + 1)
                        : ((double)val - 1);

        // env.define(expr.identifier.lexeme, newVal);
        Integer dist = locals.get(expr);
        if (dist != null)
            env.assignAt(dist, expr.identifier, newVal);
        else
            globals.assign(expr.identifier, newVal);
        // env.assignAt(null, null, val);
        return newVal;
    }

    @Override
    public Object visitPostOpExpr(Expr.PostOp expr){
        // Object val = env.get(expr.identifier);
        Object val = lookUpVariable(expr.identifier, expr);
        checkNumberOperand(expr.operator, val);

        double newVal = (expr.operator.type == PLUS_PLUS) ? ((double)(val) + 1)
                        : ((double)val - 1);
        
        Integer dist = locals.get(expr);
        if (dist != null)
            env.assignAt(dist, expr.identifier, newVal);
        else
            globals.assign(expr.identifier, newVal);
        // env.assign(expr.identifier, newVal);
        return val;
    }

    /* 
     * instead of returning true or false im retuning the objects
     * this is to make it similar to javascript and python
     * like (null or Object) will return Object
    */
    @Override
    public Object visitLogicalExpr(Expr.Logical expr){
        Object left = evaluate(expr.left);
        switch (expr.operator.type){
            case AND:
                if (!isTruthy(left)) return left;
                break;
            case OR:
                if (isTruthy(left)) return left;
                break;
            default:
                break;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitCallExpr(Expr.Call expr){
        Object callee = evaluate(expr.callee);

        if (!(callee instanceof LoxCallable))
            throw new RuntimeError(expr.paren, "Can only call functions and classes");

        List<Object> args = new ArrayList<>();
        for (Expr arg : expr.arguments)
            args.add(evaluate(arg));
        
        LoxCallable function = (LoxCallable)callee;
        if (args.size() != function.arity())
            throw new RuntimeError(expr.paren, "Expected '"+ function.arity() + 
                                   "' arugments, instead got '" + args.size() + "'.");

        return function.call(this, args);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr){
        Object obj = evaluate(expr.object);
        if (!(obj instanceof LoxInstance))
            throw new RuntimeError(expr.name, "Cannot access property '" + expr.name.lexeme + "' of a non instance object");
        return ((LoxInstance) obj).get(expr.name);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr){
        Object obj = evaluate(expr.object);
        // System.out.println(obj);
        if (!(obj instanceof LoxInstance))
            throw new RuntimeError(expr.name, "Only instances can have fields");
        Object rval = evaluate(expr.value);
        // does not raise error if there is no requested property, instead it just adds that.
        ((LoxInstance) obj).set(expr.name, rval);
        return rval;
    }

    @Override
    public Object visitThisExpr(Expr.This expr){
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr){
        int dist = locals.get(expr);
        LoxClass superclass = (LoxClass)env.getAt(dist, "super");
        LoxInstance obj = (LoxInstance)env.getAt(dist - 1, "this");

        LoxFunction func = superclass.findMethod(expr.method.lexeme);
        if (func == null){
            throw new RuntimeError(expr.method, "Cannot find property '" + expr.method.lexeme + "' of class " + superclass + ".");
        }
        return func.bind(obj);
    }

    @Override
    public Void visitPrintStmt(Stmt.Print printStmt){
        Object e = evaluate(printStmt.expression);
        System.out.println(stringify(e));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var varStmt){
        Object e = null;
        if (varStmt.expression != null)
            e = evaluate(varStmt.expression);
        // Object e = evaluate(varStmt.expression);
        env.define(varStmt.identifier.lexeme, e);
        // variables.put(varStmt.identifier.literal, e);
        return null;
    }

    @Override
    public Object visitAssignmentExpr(Expr.Assignment expr) {
        Object e = evaluate(expr.expression);
        Integer dist = locals.get(expr);
        if (dist != null)
            env.assignAt(dist, expr.identifier, e);
        else
            globals.assign(expr.identifier, e);
        return e;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block blockStmt){
        Environment newEnv = new Environment(env);
        executeBlock(blockStmt.statements, newEnv);
        // for (Stmt stmt : blockStmt.statements)
        //     stmt.accept(this);
        // env = env.parentEnv;
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If ifStmt){
        Object cond = evaluate(ifStmt.condition);
        if (isTruthy(cond))
            execute(ifStmt.thenBranch);
        else if (ifStmt.elseBranch != null)
            execute(ifStmt.elseBranch);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While whileStmt){
        while (isTruthy(evaluate(whileStmt.condition)))
            execute(whileStmt.body);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function funcStmt){
        LoxFunction func = new LoxFunction(funcStmt, env, false);
        env.define(funcStmt.name.lexeme, func);
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class clsStmt){
        env.define(clsStmt.name.lexeme, null);
        Map<String, LoxFunction> methods = new HashMap<>();

        
        LoxClass superclass = null;
        if (clsStmt.superclass != null){
            Object supercls = evaluate(clsStmt.superclass);
            if (!(supercls instanceof LoxClass))
                throw new RuntimeError(clsStmt.superclass.name,
                                        "Can only inherit from a class");
            
            superclass = (LoxClass)supercls;
            env = new Environment(env);
            env.define("super", superclass);
        }
        for (Stmt.Function methodDef : clsStmt.methods){
            methods.put(
                methodDef.name.lexeme, 
                new LoxFunction(methodDef, env, 
                methodDef.name.lexeme.equals(LoxClass.constructorName))
            );
        }

        LoxClass cls = new LoxClass(clsStmt.name.lexeme, superclass, methods);

        if (superclass != null)
            env = env.parentEnv;
        //assign in global (or actually more generally parent) environment
        env.assign(clsStmt.name, cls);
        // Environment newEnv = new Environment(this.env);
        // List<LoxFunction> methods = new ArrayList<>();
        // for (Stmt.Function method : clsStmt.methods){
        //     methods.add(
        //         new LoxFunction(method, newEnv)
        //     );
        // }

        // LoxClass cls = new LoxClass(clsStmt, newEnv, methods);
        // env.define(clsStmt.name.lexeme, cls);

        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return retStmt){
        Object val = null;

        if (retStmt.expression != null)
            val = evaluate(retStmt.expression);

        throw new Return(val);
    }

    public void executeBlock(List<Stmt> stmts, Environment newEnv){
        Environment prevEnv = this.env;
        try {
            this.env = newEnv;
            for (Stmt stmt : stmts)
                execute(stmt);
                // stmt.accept(this);
        } finally {
            this.env = prevEnv;
        }
    }

}
