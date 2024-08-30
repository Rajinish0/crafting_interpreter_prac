package lox;

import static lox.TokenType.*;
import lox.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class Scanner{
    private int start, current, line;
    private String source;
    private char curChar;
    private List<Token> tokens = new ArrayList<>();
    private static final HashMap<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("fn", FN);
        keywords.put("for", FOR);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    public Scanner(String source){
        this.source = source;
        start = current = 0;
        line = 1;
    }

    List<Token> scanTokens(){
        while (!isFinished()){
            start = current;
            scanToken();
        }

        return tokens;
    }

    Boolean isFinished(){
        return current >= source.length();
    }

    /* These consume */
    Boolean match(char c){
        if (isFinished() || peek() != c) 
            return false; 
        current++;
        return true;
    }

    void advance(){
        curChar = source.charAt(current++);
    }

    /* These don't */
    char peek(){
        if (isFinished()) return '\0';
        return source.charAt(current);
    }

    char peekNext(){
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }


    void scanToken(){
        advance();
        switch (curChar){
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': 
                if (match('-'))
                    addToken(MINUS_MINUS);
                else
                    addToken(MINUS);
                break;
            case '+':  
                if (match('+'))
                    addToken(PLUS_PLUS);
                else
                    addToken(PLUS);
                break;
            case ';': addToken(SEMI_COLON); break;
            case '*': addToken(STAR); break;
            case '/':
                if (match('/'))
                    while (peek() != '\n' && !isFinished()) advance();
                else 
                    addToken(SLASH);
                break;
            case '!':
                addToken(match('=') ? EXCLAM_EQUAL : EXCLAM);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '"':
                string();
                break;
            case ' ': case '\r': case '\t': 
                break;
            case '\n':
                line++; break;
            default:
                if (isDigit(curChar))
                    number();
                else if (isAlpha(curChar))
                    alphaNumeric();
                else 
                    Lox.error(line, "Unexpected character: " + curChar);
                break;
        }
    }

    void alphaNumeric(){
        while (isAlphaNumeric(peek())) advance();

        String str = source.substring(start, current);

        if (keywords.containsKey(str)){
            addToken(keywords.get(str));
            return;
        }

        addToken(IDENTIFIER, 
                 str);
    }

    void number(){
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext()))
            advance(); //consume the dot
        
        while (isDigit(peek()))
            advance();
        
        addToken(NUMBER, 
                Double.parseDouble(
                    source.substring(
                        start,
                        current
                    )
                ));
    }

    void string(){
        while (!isFinished() && peek() != '"') {
            if (peek() == '\n') line++; // allow multi line strings
            advance();
            };

        if (isFinished()){
            Lox.error(line, "Unterminated string");
            return;
        }

        advance();

        String literal = source.substring(start + 1, current - 1);
        addToken(STRING, literal);
    }

    Boolean isDigit(char c){
        return (c >= '0' && c <= '9');
    }

    Boolean isAlpha(char c){
        return ( (c >= 'a' && c <= 'z') || 
                 (c >= 'A' && c <= 'Z') || 
                 (c == '_') );
    }

    Boolean isAlphaNumeric(char c){
        return ( isAlpha(c) || isDigit(c) );
    }

    void addToken(TokenType t){
        String lex = source.substring(start, current);
        tokens.add(new Token(t, lex, null, line));
    }

    void addToken(TokenType t, Object literal){
        String lex = source.substring(start, current);
        tokens.add(new Token(t, lex, literal, line));
    }

};