package lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> variables = new HashMap<>();
   
    final Environment parentEnv;

    public void define(String name, Object value){
        variables.put(name, value);
    }

    public Environment(){
        parentEnv = null;
    }

    public Environment(Environment parentEnv){
        this.parentEnv = parentEnv;
    }

    public Environment deepCopy(){
        Environment pEnv = null;
        if (parentEnv != null)
            pEnv = parentEnv.deepCopy();
        Environment newEnv = new Environment(pEnv);
        for (Map.Entry<String, Object> entry : variables.entrySet()){
            newEnv.variables.put(entry.getKey(), entry.getValue());
        }
        return newEnv;
    }

    public Object get(Token name){
        if (variables.containsKey(name.lexeme))
            return variables.get(name.lexeme);
        
        if (parentEnv != null) 
            return parentEnv.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    public Object getAt(Integer dist, String name){
        return ancestor(dist).variables.get(name);
    }

    private Environment ancestor(Integer dist){
        Environment env = this;
        for (int i =0; i < dist; ++i)
            env = env.parentEnv;
        return env;

    }

    void assignAt(Integer dist, Token name, Object value){
        ancestor(dist).variables.put(name.lexeme, value);
    }

    void assign(Token name, Object value){
        if (variables.containsKey(name.lexeme)){
            variables.put(name.lexeme, value);
            return;
        }

        if (parentEnv != null){
            parentEnv.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
