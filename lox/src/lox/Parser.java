package lox;

import static lox.TokenType.*;

import java.beans.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/* 
 * THIS BUILDS THE AST,
 * I think it should be able to execute statements as the come here
 * but i reckon the AST is a cleaner choice
 * 
 * actually i need the AST because the loops would be a terrible headache without them
*/
public class Parser {

    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current =0;

    public Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    //program : statements* EOF, parse is basically
    //program in the grammar
    List<Stmt> parse(){
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd()){
            stmts.add( declaration() );
        }
        return stmts;
        // try{
        //     return expression();
        // }
        // catch (ParseError error){
        //     return null;
        // }
    }

    private Boolean match(TokenType... types){
        if (current >= tokens.size())
            return false;
        
        for (TokenType type : types){
            if (check(type)){
                advance();
                return true;
            }
        }

        return false;
    }

    private Boolean isAtEnd(){
        return current >= tokens.size();
    }

    private Token advance(){
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token previous(){
        return tokens.get(current-1);
    }

    private Token peek(){
        // System.out.println("FAILING HERE");
        if (isAtEnd()) return previous();
        return tokens.get(current);
    }

    private Boolean check(TokenType type){
        if (isAtEnd()) return false;
        return peek().type == type;
    }


    private Stmt declaration(){
        try {
            if (match(VAR)) return varStatement();

            return statement();
        } catch (ParseError error){
            sync();
            return null;
        }
    }
    Stmt statement(){
        if (match(PRINT))
            return printStatement();
        else if (match(LEFT_BRACE))
            return blockStatement();
        else if (match(IF))
            return ifStatement();
        else if (match(WHILE))
            return whileStatement();
        else if (match(FN))
            return functionStatement();
        else if (match(RETURN))
            return returnStatement();
        else if (match(FOR))
            return forStatement();
        else
            return expressionStatement();
    }

    private Stmt printStatement(){
        Expr expr = expression();
        consume(SEMI_COLON, "Expected ; after print statement");
        return new Stmt.Print(expr);
    }

    private Stmt functionStatement(){
        Token name = consume(IDENTIFIER, "Expected identifier after fn");
        consume(LEFT_PAREN, "Expected '(' after function identifier");
        List<Token> params = new ArrayList<>();
        if (!check(RIGHT_PAREN)){
            do {
                if (params.size() >= 255)
                    throw error(peek(), "Max number params is: 255");
                params.add(consume(IDENTIFIER, "Expected identifier for param"));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expected ')' after params");
        consume(LEFT_BRACE, "Expected '{' for function body");
        List<Stmt> stmts = block();

        return new Stmt.Function(name, params, stmts);
    }

    private Stmt returnStatement(){
        Token returnKW = previous();
        Expr val = null;
        if (!check(SEMI_COLON))
            val = expression();
        consume(SEMI_COLON, "Expected ';' after return");

        return new Stmt.Return(returnKW, val);
    }

    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd())
            statements.add(declaration());

        consume(RIGHT_BRACE, "Expected '}' after block");
        return statements;
    }

    private Stmt ifStatement(){
        consume(LEFT_PAREN, "Expected '(' after if");
        Expr expr = expression();
        consume(RIGHT_PAREN, "Expected ')' after if condition");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE))
            elseBranch = statement();
        return new Stmt.If(expr, thenBranch, elseBranch);
    }

    private Stmt whileStatement(){
        consume(LEFT_PAREN, "Expected ( after while");
        Expr expr = expression();
        consume(RIGHT_PAREN, "Expected ) after while condition");

        Stmt statement = statement();

        return new Stmt.While(expr, statement);
    }

    private Stmt forStatement(){
        consume(LEFT_PAREN, "Expected '(' after for");
        Stmt initExpr;

        if (match(SEMI_COLON))
            initExpr = null;
        else if (match(VAR))
            initExpr = varStatement();
        else 
            initExpr = expressionStatement();

        Expr condition = null;
        if (!check(SEMI_COLON))
            condition = expression();
        
        consume(SEMI_COLON, "Expected ; after for condition");

        Expr incExpr = null;
        if (!check(RIGHT_PAREN))
            incExpr = expression();

        consume(RIGHT_PAREN, "Expected ')' after for condition");

        Stmt body = statement();

        if (incExpr != null)
            body = new Stmt.Block(
                                Arrays.asList(
                                    body,
                                    new Stmt.Expression(incExpr)
                                )
                            );

        if (condition == null) condition = new Expr.Literal(true);

        body = new Stmt.While(condition, body);

        if (initExpr != null)
            body = new Stmt.Block(
                Arrays.asList(
                    initExpr,
                    body
                )
            );

        return body;
    }

    // private Stmt functionStatement(){
    //     Expr expr = expression();
    //     Token name = null;
    //     Token paren = consume(LEFT_PAREN, "'(' expected after function name");
    //     if (expr instanceof Expr.Variable)
    //         name = ((Expr.Variable)expr).name;
    //     else
    //         throw error(paren, "Function name is not a variable");
    //     List<Expr> args = arguments();
    //     consume(RIGHT_PAREN, "')' expected after arguments");
    //     Stmt functionBlock = blockStatement();

    //     return Stmt.Function(name, args, functionBlock);
    // }

    private Stmt blockStatement(){
        /* 
         * I was thinking of moving consume(SEMI_COLON) to the end of the statement() above
         * but this is a good reason to not do that. Plus the curated messages for each case are better then a more 
         * generic one.
        */
        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd())
            statements.add(declaration());

        consume(RIGHT_BRACE, "Expected '}' after block");
        
        return new Stmt.Block(statements);
    }

    private Stmt varStatement(){
        Token varName = consume(IDENTIFIER, "Expected identifier");
        
        Expr expr = null;
        if (match(EQUAL))
            expr = expression();

        consume(SEMI_COLON, "Expected ; after declaration");
        return new Stmt.Var(varName, expr);
    }

    private Stmt expressionStatement(){
        Expr expr = expression();
        consume(SEMI_COLON, "Expected ; after statement");
        return new Stmt.Expression(expr);
    }
    
    private Expr expression(){
        return assignment();
    }

    private Expr assignment(){
        Expr lvalue = or();
        if (match(EQUAL)){
            Token equals = previous();
            Expr rvalue = assignment();

            if (lvalue instanceof Expr.Variable){
                return new Expr.Assignment(((Expr.Variable)(lvalue)).name, 
                                            rvalue);
            }
            else 
                throw error(equals, "Assignment target is not a variable");
        }

        return lvalue;
    }

    private Expr or(){
        Expr expr = and();
        while (match(OR)){
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and(){
        Expr expr = equality();

        while (match(AND)){
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality(){
        Expr expr = comparision();
        while (match(EXCLAM_EQUAL, EQUAL_EQUAL))
        {
            Token operator = previous();
            Expr right = comparision();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparision(){
        Expr expr = term();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL))
        {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term(){
        Expr expr = factor();
        while (match(MINUS, PLUS))
        {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor(){
        Expr expr = unary();
        while (match(SLASH, STAR))
        {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(EXCLAM, MINUS))
            return new Expr.Unary(previous(), unary());
        return call();
    }

    private Expr call(){
        Expr expr = primary();
        while (match(LEFT_PAREN)){
            List<Expr> args = arguments();
            Token paren = consume(RIGHT_PAREN, "Expected ')' at the end of function call");
            expr = new Expr.Call(expr, paren, args);
        }
        return expr;
    }

    private List<Expr> arguments(){
        List<Expr> args = new ArrayList<>();
        if (!check(RIGHT_PAREN)){
            do {
                if (args.size() >= 255)
                    error(peek(), "Functions can't have more than 255 arguments.");
                args.add(expression());
            } while (match(COMMA)); 
        }

        return args;
    }

    private Expr primary(){
        // if (match(NUMBER, STRING, TRUE, FALSE, 
                //   NIL))
            // return new Expr.Literal(previous().lexeme);
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);


        if (match(NUMBER, STRING))
            return new Expr.Literal(previous().literal);
        
        if (match(IDENTIFIER)){
            Token id = previous();
            if (match(PLUS_PLUS, MINUS_MINUS))
                return new Expr.PostOp(id, previous());
            return new Expr.Variable(id);
        }

        if (match(PLUS_PLUS, MINUS)){
            Token op = previous();
            Token id = consume(IDENTIFIER, "Expected a identifier after '"+op.lexeme+"'");
            return new Expr.PreOp(id, op);
        }
            

        if (match(LEFT_PAREN)){
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expected expression");
    }

    private Token consume(TokenType type, String message){
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message){
        Lox.error(token, message);
        return new ParseError();
    }

    private void sync(){
        advance();

        while (!isAtEnd()){
            if (previous().type == SEMI_COLON) return;

            switch (peek().type){
                case CLASS:
                case FN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
        
    }

}
