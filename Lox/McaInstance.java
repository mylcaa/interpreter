package lox;

import java.util.HashMap;
import java.util.Map;

class McaInstance {
    final private McaClass klas;
    private final Map<String, Object> fields = new HashMap<>();

    public McaInstance(McaClass klas) {
        this.klas = klas;
    }

    Object get(Token name){
        if(fields.containsKey(name._lexeme))
            return fields.get(name._lexeme);

        McaFunction method = klas.findMethod(name._lexeme);
        if(method != null)
            return method.bind(this);

        throw new RuntimeError(name, "Undefined property '" + name._lexeme + "'.");
    }

    void set(Token name, Object value){
        fields.put(name._lexeme, value);
    }

    @Override
    public String toString(){
        return klas.name + " instance";
    }

    
}