package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable
{

    public final static String constructorName = "init";

    final String name;
    Map<String, LoxFunction> methods = new HashMap<>();

    public LoxClass(String name, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    public LoxFunction findMethod(String name){
        return methods.get(name);
    }

    @Override
    public String toString(){
        return name;
    }
    
    @Override
    public int arity(){
        if (methods.containsKey(constructorName))
            return methods.get(constructorName).arity();
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initMethod = findMethod(constructorName);
        if (initMethod != null){
            initMethod.bind(instance)
                      .call(interpreter, args);
        }
        return instance;
    }

}
