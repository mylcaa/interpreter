package lox;

import java.util.ArrayList;
import java.util.List;
import static lox.TokenType.*;

class Parser{
    private static class ParseError extends RuntimeException{}

    final private List<Token> _tokens; 
    private int current = 0;

    Parser(List<Token> tokens){
        this._tokens = tokens;
    }

    List<Stmt> parse(){
            List<Stmt> statements = new ArrayList<>();
            while(!IsAtEnd()){
                statements.add(declaration());
            }

            return statements;
    }

    private Stmt declaration(){
        try{
            if(match(VAR)){
                return varDeclaration();
            }
            return statement();

        }catch(ParseError error){
            synchronize();
            return null;
        }
    }


    private Stmt varDeclaration(){
        Token name = consume(IDENTIFIER, "Expect variable name!");
        
        Expr initializer = null;
        if(match(EQUAL)){
            initializer = expression();
        }

        consume(SEMICOLON, "Expect semicolon after variable declaration!");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement(){
        if(match(PRINT))
            return printStatement();
        
        return expressionStatement();
    }

    private Stmt printStatement(){
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement(){
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Expression(value);
    }

    private Expr expression(){
        return equality();
    }

    private Expr equality(){
        Expr expression = comparison();

        while(match(BANG_EQUAL, EQUAL_EQUAL)){
            Token operation = previous();
            Expr right = comparison();
            expression = new Expr.Binary(expression, operation, right);
        }

        return expression; 
    }

    private Expr comparison(){
        Expr expression = term();

        while(match(GREATER, GREATER_EQUAL, LESSER, LESSER_EQUAL)){
            Token operation = previous();
            Expr right = term();
            expression = new Expr.Binary(expression, operation, right);
        }

        return expression; 
    }

    private Expr term(){
        Expr expression = factor();

        while(match(MINUS, PLUS)){
            Token operation = previous();
            Expr right = factor();
            expression = new Expr.Binary(expression, operation, right);
        }

        return expression; 
    }

    private Expr factor(){
        Expr expression = unary();

        while(match(STAR, SLASH)){
            Token operation = previous();
            Expr right = unary();
            expression = new Expr.Binary(expression, operation, right);
        }

        return expression; 
    }

    private Expr unary(){

        if(match(BANG, MINUS)){
            Token operation = previous();
            Expr right = unary();
            return new Expr.Unary(operation, right);
        }

        return primary(); 
    }

    private Expr primary(){

        if(match(FALSE)) 
            return new Expr.Literal(false);

        if(match(TRUE)) 
            return new Expr.Literal(true);

        if(match(NIL)) 
            return new Expr.Literal(null);

        if(match(NUMBER, STRING))
            return new Expr.Literal(previous()._literal);

        if(match(IDENTIFIER)) 
            return new Expr.Variable(previous());
        
        if(match(LEFT_PAREN)){
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private Token consume(TokenType type, String message){
        if(check(type))
            return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message){
        Lox.error(token._line, message);
        return new ParseError();
    }

    private void synchronize(){
        advance();

        while(!IsAtEnd()){
            if(previous()._type == SEMICOLON)
                return;
            
            switch(peek()._type){
                case CLASS:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    private boolean match(TokenType... types){
        for(TokenType type: types){
            if(check(type)){
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type){
        if(IsAtEnd())
            return false;
        
        return peek()._type == type;

    }

    private Token peek(){
        return _tokens.get(current);
    }

    private Token advance(){
        if(!IsAtEnd())
            current++;
        
        return previous();
    }

    private boolean IsAtEnd(){
        return peek()._type == EOF;
    }


    private Token previous(){
        return _tokens.get(current - 1);
    }

}


