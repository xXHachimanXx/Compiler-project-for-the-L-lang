import java.io.PushbackReader;
import java.util.HashMap;

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

class Lexer {
    public int line;
    public PushbackReader reader;
    public HashMap<String, Token> keywords;

    void initKeywords() {
        keywords = new HashMap<>();
        keywords.put("main", new Token(TokenType.MAIN, "main"));
        keywords.put("write", new Token(TokenType.WRITE, "write"));
        keywords.put("writeln", new Token(TokenType.WRITELN, "writeln"));
        keywords.put("readln", new Token(TokenType.READLN, "readln"));
        keywords.put("return", new Token(TokenType.RETURN, "return"));
        keywords.put("if", new Token(TokenType.IF, "if"));
        keywords.put("then", new Token(TokenType.THEN, "then"));
        keywords.put("else", new Token(TokenType.ELSE, "else"));
        keywords.put("for", new Token(TokenType.FOR, "for"));
        keywords.put("int", new Token(TokenType.INT, "int"));
        keywords.put("char", new Token(TokenType.CHAR, "char"));
        keywords.put("boolean", new Token(TokenType.BOOLEAN, "boolean"));
        keywords.put("final", new Token(TokenType.FINAL, "final"));
        keywords.put("TRUE", new Token(TokenType.TRUE, "TRUE"));
        keywords.put("FALSE", new Token(TokenType.FALSE, "FALSE"));
        keywords.put("or", new Token(TokenType.OR, "or"));
        keywords.put("and", new Token(TokenType.AND, "and"));
        keywords.put("not", new Token(TokenType.NOT, "not"));
    }

    Lexer(PushbackReader reader) {
        this.line = 1;
        this.reader = reader;
        initKeywords();
    }

    boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F');
    }

    Token readInteger(char firstDigit) throws IOException {
        String str = "" + firstDigit;

        char c = (char) reader.read();
        char c1 = (char) reader.read();
        char c2 = (char) reader.read();
        if (
            firstDigit == '0'
            && c2 == 'h'
            && isHex(c)
            && isHex(c1)
        ) {
            str += c;
            str += c1;
            str += c2;
            return new Token(TokenType.HEX_INTEGER, str);
        }

        reader.unread(c2);
        reader.unread(c1);

        while (c >= '0' && c <= '9') {
            str += c;
            c = (char) reader.read();
        }

        reader.unread(c);

        int value = Integer.parseInt(str);
        if (str.length() > 5 || value < -32768 || value > 32767) {
            System.out.printf("%d\nlexema nao identificado [%s].\n", line, str);
            System.exit(0);
            return new Token(TokenType.EOF, "");
        } else {
            return new Token(TokenType.INTEGER, str);
        }
    }

}

