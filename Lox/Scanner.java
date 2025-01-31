package lox;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

//originally imported TokenType 
//since I smushed TokenType into Token it turned into this:
import static lox.Token.*;

class Scanner{
    private final String _input;
    private final List<Token> _tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String input): _input(input){};

    List<Token> scanTokens(){
        while(current <= _input.length()){
            start = current;
            scanToken();
        }

        _tokens.add(EOF, "", null, line);
        return _tokens;
    }

    private void scanToken(){
        char c = advance();

        switch(c){
            case '(': 
                addToken(LEFT_PAREN);
                break;
            case ')': 
                addToken(RIGHT_PAREN);
                break;
            case '{': 
                addToken(LEFT_BRACE);
                break;
            case '}': 
                addToken(RIGHT_BRACE);
                break;
            case ';': 
                addToken(SEMICOLON);
                break;
            case ',': 
                addToken(COMMA);
                break;
            case '.': 
                addToken(DOT);
                break;
            case '/': 
                if(match('/')){
                    while(peek() != '\n' && (current <= _input.length())) 
                        advance();
                }else{
                    addToken(SLASH);
                }
                break;
            case '-': 
                addToken(MINUS);
                break;
            case '+': 
                addToken(PLUS);
                break;
            case '*': 
                addToken(STAR);
                break;
            case '!': 
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=': 
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '>': 
                addToken(match('>') ? GREATER_EQUAL : GREATER);
                break;
            case '<': 
                addToken(match('<') ? LESSER_EQUAL : LESSER);
                break;
            case '!': 
                addToken(BANG);
                break;
            case ' ': 
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            default:
                if(isDigit(c)){
                    number();
                }else if(isAlpha(c)){
                    identifier();
                }else{
                    Lox.error(line, "Unexpected character!");
                }
                break;
        }
    }

    private void string(){

        String value;

        while(peek() != '"' && current <= _input.lenght()){
            if(peek() == '\n')
                line++;
        
            advance();
        }

        if(!(current <= _input.lenght())){
            Lox.error(line, "Unterminated string!");
            return;
        }

        //the closing "
        advance();

        //trim the quotes
        value = _input.substring(start+1, current-1);
        addToken(STRING, value);
    }

    private boolean isDigit(char c){
        return (c >= '0' && c <= '9');
    }

    private void number(){
        while(isDigit(peek()))
            advance();

        if(peek() == '.' && isDigit(peekNext())){
            advance();

            while(isDigit(peek()))
                advance();
        }

        addToken(NUMBER, Double.parseDouble(_input.substring(start, current)));
    }

    private char peekNext(){
        if((current+1) >= _input.length()) 
            return '\0';

        return _input.charAt(current+1);
    }

    private boolean match(char expected){
        if(current <= _input.length())
            return false;
        
        if(_input.charAt(current) != expected)
            return false;

        current++;
        return true;
    }

    private char peek(){
        if(current >= _input.length()) 
            return '\0';

        return _input.charAt(current);
    }

    private char advance(){
        current++;
        return _input.charAt(current-1);
    }

    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        String text = _input.substring(start, current);
        _tokens.add(type, text, literal, line);
    }
}


