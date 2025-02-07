package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static lox.TokenType.*;

class Scanner{
    private final String _input;
    private final List<Token> _tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("or", OR);
        keywords.put("if", IF);
        keywords.put("else", ELSE);
        keywords.put("switch", SWITCH);
        keywords.put("case", CASE);
        keywords.put("for", FOR);
        keywords.put("while", WHILE);
        keywords.put("true", TRUE);
        keywords.put("return", RETURN);
        keywords.put("nil", NIL);
        keywords.put("var", VAR);
        keywords.put("class", CLASS);
        keywords.put("print", PRINT);
        keywords.put("eof", EOF);
    }

    Scanner(String input){
        _input = input;
    }

    List<Token> scanTokens(){
        while(!isAtEnd()){
            start = current;
            scanToken();
        }

        _tokens.add(new Token(EOF, "", null, line));
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
                    while(peek() != '\n' && !isAtEnd()) 
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

    private boolean isAlpha(char c){
        return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_');

    }

    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }

    private void identifier(){

        while(isAlphaNumeric(peek()))
            advance();

        String text = _input.substring(start, current);
        TokenType type = keywords.get(text);
        if(type == null)
            type = IDENTIFIER; 

        addToken(type);
    }

    private void string(){

        String value;

        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n')
                line++;
        
            advance();
        }

        if(isAtEnd()){
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
        if(isAtEnd())
            return false;
        
        if(_input.charAt(current) != expected)
            return false;

        current++;
        return true;
    }

    private char peek(){
        if(isAtEnd()) 
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

    private boolean isAtEnd(){
        return current >= _input.length();
    }

    private void addToken(TokenType type, Object literal){
        String text = _input.substring(start, current);
        _tokens.add(new Token(type, text, literal, line));
    }
}


