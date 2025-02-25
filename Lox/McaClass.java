package lox;

import java.util.Map;
import java.util.List;

class McaClass implements McaCallable {
    final String name;

    public McaClass(String name) {
        this.name = name;
    }

    @Override
    public int arity(){
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments){
        McaInstance instance = new McaInstance(this);
        return instance;
        
    }

    @Override
    public String toString(){
        return name;
    }

    
}