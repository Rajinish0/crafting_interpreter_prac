package lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoxInstance {
    private final LoxClass cls;
    private final Map<String, Object> fields = new HashMap<>();

    public LoxInstance(LoxClass cls){
        this.cls = cls;
    }

    public Object get(Token name){
        if (fields.containsKey(name.lexeme))
            return fields.get(name.lexeme);



        LoxFunction method = cls.findMethod(name.lexeme);
        // System.out.println(method);
        if (method != null) return method.bind(this);


        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "' of class " + cls + ".");
    }

    public void set(Token name, Object val){
        fields.put(name.lexeme, val);
    }

    @Override
    public String toString(){
        return cls.name + " instance";
    }
}
