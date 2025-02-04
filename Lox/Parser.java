package lox;

import java.utils.List;
import lox.Token;

class Parser{
    final private List<TokenType> _tokens;
    final private int current = 0;

    Parser(List<TokenType> tokens) :
    _tokens(tokens){}

    private Expr expression(){
        return equality();
    }

    private Expr equality(){
        Expr expression = comparsion();

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
            return new Expr.Binary(operation, right);
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
            return new Expr.Literal(previous().literal);
        
        if(match(LEFT_PAREN)){
            Expr exp = expression();
            //NOTE: needs to be written
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
 
    }


    private bool match(TokenType... types){
        for(TokenType type: types){
            if(check(type)){
                advance();
                return true;
            }
        }
        return false;
    }

    private bool check(TokenType type){
        if(IsAtEnd())
            return false;
        
        return peek().type == type;

    }

    private bool IsAtEnd(){
        return peek().type == EOF;
    }

    private Token peek(){
        return _tokens.get(current);
    }

    private Token advance(){
        if(!IsAtEnd)
            current++;
        
        return previous();
    }

    private Token previous(){
        return tokens.get(current - 1);
    }

}


