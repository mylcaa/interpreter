package lox;

class RunTimeError extends RunTimeeXException{
    final Token token;

    RunTimeError(Token token, String message){
        //calling constructor of parent class
        super(message);
        this.token = token;
    }
}