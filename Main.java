enum TokenType {
    EOF,
    MAIN,
    IDENTIFIER,
    COMMA,
    SEMICOLON,
    // Statements
    WRITE,
    WRITELN,
    READLN,
    RETURN,
    IF,
    THEN,
    ELSE,
    FOR,
    // Modifiers
    INT,
    CHAR,
    BOOLEAN,
    FINAL,
    // Literals
    INTEGER,
    HEX_INTEGER,
    STRING,
    TRUE,
    FALSE,
    // Operators
    ASSIGN,
    LEFT_PAREN,
    RIGHT_PAREN,
    LEFT_BRACKET,
    RIGHT_BRACKET,
    LEFT_BRACES,
    RIGHT_BRACES,
    PERCENT,
    PLUS,
    MINUS,
    ASTERISK,
    BACKSLASH,
    OR,
    AND,
    NOT,
    EQUAL,
    DIFFERENT,
    GREATER,
    SMALLER,
    GREATER_OR_EQUAL,
    SMALLER_OR_EQUAL,
}

class Token {
    TokenType type;
    String value;
    Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }
    Token(TokenType type, char c) {
        this(type, "" + c);
    }
}

