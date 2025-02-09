package lox;

import java.util.HashMap;
import java.util.Map;

class Environment{
    private final Map<String, Object> values = new HashMap<>();

    Object get(Token name){
        if(values.containsKey(name._lexeme)){
            return values.get(name._lexeme);
        }

        throw new RuntimeError(name, "Undefined variable '" + name._lexeme + "'.");
    }

    void define(String name, Object value){
        values.put(name, value);
    }


}