package mul;

import java.util.HashMap;
import java.util.Map;

class MulInstance {
    final private MulClass klas;
    private final Map<String, Object> fields = new HashMap<>();

    public MulInstance(MulClass klas) {
        this.klas = klas;
    }

    Object get(Token name){
        if(fields.containsKey(name._lexeme))
            return fields.get(name._lexeme);

        MulFunction method = klas.findMethod(name._lexeme);
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