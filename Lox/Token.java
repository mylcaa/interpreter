package lox;

enum TokenType{
//single character tokens
SEMICOLON, COMMA, DOT,
SLASH, STAR, MINUS, PLUS,
LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,

//two character tokens (or potential two char tok)
EQUAL, GREATER, LESSER, BANG,
BANG_EQUAL, EQUAL_EQUAL, GREATER_EQUAL, LESSER_EQUAL,

//literals
STRING, NUMBER, IDENTIFIER,

//keywords
FUNCT, THIS, SUPER,
IF, ELSE, WHILE, SWITCH, CASE, FOR,
AND, OR,
TRUE, FALSE, RETURN, NIL, VAR, CLASS, PRINT,

EOF
}

class Token{
                          //example:
    final TokenType _type; //SEMICOLON 
    final String _lexeme;  //;
    final Object _literal; //nothing
    final int _line;       //location info

    //constructor
    Token(TokenType type, String lexeme, Object literal, int line){
        this._type = type;
        this._lexeme = lexeme;
        this._literal = literal;
        this._line = line;
    }

    public String toString(){
        return _type + " " + _lexeme + " " + _literal;
    }
}