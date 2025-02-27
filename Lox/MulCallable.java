package mul;

import java.util.List;

interface MulCallable{
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
