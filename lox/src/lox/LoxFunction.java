package lox;

import java.util.List;

public class LoxFunction implements LoxCallable{

    private final Stmt.Function declaration;
    private Environment localEnv = null;
    private final Boolean isInitializer;

    public LoxFunction(Stmt.Function declaration, Environment env, Boolean isInitializer){
        this.declaration = declaration;
        this.localEnv = env;
        this.isInitializer = isInitializer;
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
        } catch (Return retExcep){
            /*
             * return; is allowed from initializer
             * but return smth; is not!
             */
            if (isInitializer) return localEnv.getAt(0, "this");
            return retExcep.value;
        }
        if (isInitializer)
            return localEnv.getAt(0, "this");
        return null;
    }

    LoxFunction bind(LoxInstance instance){
        Environment env = new Environment(this.localEnv);
        env.define("this", instance);
        return new LoxFunction(declaration, env, isInitializer);
    }

    @Override
    public String toString(){
        return "<fn " + this.declaration.name.lexeme + ">";
    }
}
