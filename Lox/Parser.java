package lox;

import java.util.ArrayList;
import java.util.Arrays;
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

        if(match(WHILE))
            return WhileStatement();
        
        if(match(FOR))
            return ForStatement();

        if(match(IF))
            return ifStatement();

        if(match(PRINT))
            return printStatement();

        if(match(LEFT_BRACE))
            return new Stmt.Block(block());

        return expressionStatement();
    }

    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !IsAtEnd()){
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block!");
        return statements;
    }

    private Stmt WhileStatement(){
        consume(LEFT_PAREN, "Keyword while must be followed by '('!");
        Expr condition = expression();
        consume(RIGHT_PAREN, "While condition must be followed by ')'!");

        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt ForStatement(){
        consume(LEFT_PAREN, "Keyword for must be followed by '('!");
        
        Stmt initializer;
        if(match(SEMICOLON)){
            initializer = null;
        }else if(match(VAR)){
            initializer = varDeclaration();
        }else{
            initializer = expressionStatement();
        }

        Expr condition = null;
        if(!check(SEMICOLON)){
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition!");

        Expr increment = null;
        if(!check(SEMICOLON)){
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clause!");

        Stmt body = statement();

        if(increment != null){
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        } 

        if(condition == null)
            condition = new Expr.Literal(true);

        body = new Stmt.While(condition, body);

        if(initializer != null){
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }   

        return body;
    }

    private Stmt ifStatement(){
        consume(LEFT_PAREN, "Expect '(' after keyword 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        
        Stmt thenBranch = statement();
        Stmt elseBranch = null;

        if(match(ELSE))
            elseBranch = statement();

        return new Stmt.If(condition, thenBranch, elseBranch);
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
        return assignment();
    }

    private Expr assignment(){
        //parse the left side since "=" doesn't fit into any rule bellow
        Expr expr = or(); //will return only the identifier and now current = 1

        if(match(EQUAL)){
            Token equals = previous();
            Expr value = assignment();

            if(expr instanceof Expr.Variable){
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target!");
        }
        return expr;
    }

    private Expr or(){
        Expr expr = and();

        while(match(OR)){
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and(){
        Expr expr = equality();

        while(match(AND)){
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
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


