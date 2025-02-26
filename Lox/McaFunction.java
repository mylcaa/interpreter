package lox;

import java.util.List;

class McaFunction implements McaCallable{
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInit;

    McaFunction(Stmt.Function declaration, Environment closure, boolean isInit) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInit = isInit;
    }

    public McaFunction bind(McaInstance instance){
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new McaFunction(declaration, environment, isInit);
    }

    //binds parameters of function call with it's names in function declaration
    //executes function block
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments){
        Environment environment = new Environment(closure);
        for(int i=0; i < declaration.params.size(); i++){
            environment.define(declaration.params.get(i)._lexeme, arguments.get(i));
        }
        
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            if(isInit) return closure.getAt(0, "this");
            return returnValue.value;
        }

        if(isInit) return closure.getAt(0, "this");
        return null;
    }

    @Override
    public int arity(){
        return declaration.params.size();
    }
    
    @Override
    public String toString(){
        return "<fn " + declaration.name._lexeme + ">";
    }

    
}