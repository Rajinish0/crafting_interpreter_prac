package lox;

import java.util.List;

public class LoxFunction implements LoxCallable{

    private final Stmt.Function declaration;
    private Environment localEnv = null;

    public LoxFunction(Stmt.Function declaration, Environment env){
        this.declaration = declaration;
        this.localEnv = env;
    }

    @Override
    public int arity(){
        return this.declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args){
        Environment env = new Environment(localEnv);
        for (int i=0; i < declaration.params.size(); i++)
            env.define(declaration.params.get(i).lexeme, 
                       args.get(i));
        try{
            interpreter.executeBlock(declaration.body, env);
            return null;
        } catch (Return retExcep){
            return retExcep.value;
        }
    }

    @Override
    public String toString(){
        return "<fn " + this.declaration.name.lexeme + ">";
    }
}
