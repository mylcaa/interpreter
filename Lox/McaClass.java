package lox;

import java.util.List;
import java.util.Map;

class McaClass implements McaCallable {
    final String name;
    final Map<String, McaFunction> methods;

    public McaClass(String name, Map<String, McaFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    public McaFunction findMethod(String name){
        if(methods.containsKey(name))
            return methods.get(name);
        
        return null;
    }

    @Override
    public int arity(){
        McaFunction initializer = findMethod("init");
        if(initializer == null)
            return 0;

        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments){
        McaInstance instance = new McaInstance(this);

        McaFunction initializer = findMethod("init");
        if(initializer != null){
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
        
    }

    @Override
    public String toString(){
        return name;
    }

    
}