package lox;


class Token {
    /* 
    Token type is GREATER for instance
    then lexeme is >
    and literal is NULL

    but for instance if it is string
    the lexeme is actually "the whole string"
    but the literal will be -the whole string-
    */
    final TokenType type;
    final String lexeme; 
    final Object literal; 
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line){
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString(){
        return type + " " + lexeme + " " + literal;
    }
};