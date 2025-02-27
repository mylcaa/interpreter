package mul;

class RuntimeError extends RuntimeException{
    final Token token;

    RuntimeError(Token token, String message){
        //calling constructor of parent class
        super(message);
        this.token = token;
    }
}