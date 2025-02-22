package lox;

import java.util.List;

interface McaCallable{
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
