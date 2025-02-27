package mul;

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

    Object getAt(int distance, String name){
        return ancestor(distance).values.get(name);
    }

    Environment ancestor(int distance){
        Environment environment = this;
        for(int i = 0; i < distance; ++i){
            environment = environment.enclosing;
        }

        return environment;
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

    void assignAt(int distance, Token name, Object value){
        ancestor(distance).values.put(name._lexeme, value);
    }


}