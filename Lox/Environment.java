package lox;

import java.util.HashMap;
import java.util.Map;

class Environment{
    final Environment enclosing; 
    private final Map<String, Object> values = new HashMap<>();

    Environment() {
        this.enclosing = null;
    }

    Environment(Environment environment) {
        this.enclosing = environment;
    }

    Object get(Token name){
        if(values.containsKey(name._lexeme)){
            return values.get(name._lexeme);
        }

        if(enclosing != null)
            return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name._lexeme + "'.");
    }

    void define(String name, Object value){
        values.put(name, value);
    }

    void assign(Token name, Object value){
        if(values.containsKey(name._lexeme)){
            values.put(name._lexeme, value);
            return;
        }

        if(enclosing != null){
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name._lexeme + "'.");
    }


}