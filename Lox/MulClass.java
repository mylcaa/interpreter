package mul;

import java.util.List;
import java.util.Map;

class MulClass implements MulCallable {
    final String name;
    final MulClass superclass;
    final Map<String, MulFunction> methods;

    public MulClass(String name, MulClass superclass, Map<String, MulFunction> methods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    public MulFunction findMethod(String name){
        if(methods.containsKey(name))
            return methods.get(name);

        if(superclass != null)
            return superclass.findMethod(name);
        
        return null;
    }

    @Override
    public int arity(){
        MulFunction initializer = findMethod("init");
        if(initializer == null)
            return 0;

        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments){
        MulInstance instance = new MulInstance(this);

        MulFunction initializer = findMethod("init");
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