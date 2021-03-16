import java.io.IOException;
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

    boolean justUnderlines(String str) {
        for (char c : str.toCharArray())
            if (c != '_') return false;
        return true;
    }

    boolean isalnum(char c) {
        return (
            (c >= '0' && c <= '9')
            || (c >= 'a' && c <= 'z')
            || (c >= 'A' && c <= 'Z')
        );
    }

    Token readIdentifier(char firstDigit) throws IOException {
        String str = "";
        str += firstDigit;

        char c = (char) reader.read();
        while (isalnum(c) || c == '_') {
            assertValidChar(c);
            str += c;
            c = (char) reader.read();
        }
        reader.unread(c);

        if (justUnderlines(str)) {
            System.out.printf("%d\nlexema nao identificado [%s].\n", line, str);
            System.exit(0);
        }

        if (keywords.containsKey(str)) return keywords.get(str);
        else return new Token(TokenType.IDENTIFIER, str);
    }

    boolean isSpace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    char skipSpaces() throws IOException {
        char c = (char) reader.read();
        while (isSpace(c)) {
            if (c == '\n') line++;
            c = (char) reader.read();
        }
        return c;
    }

    boolean isValidChar(char c) {
        return (
            isalnum(c) || c == ' ' || c == '\t' || c == '_' || c == '.' || c == ','
            || c == ';' || c == ':' || c == '(' || c == ')' || c == '{' || c == '}'
            || c == '[' || c == ']' || c == '=' || c == '<' || c == '>' || c == '%'
            || c == '+' || c == '-' || c == '*' || c == '/' || c == '\n' || c == '\r'
            || c == '\'' || c == '"' || c == (char) -1
        );
    }

    void assertValidChar(char c) {
        if (!isValidChar(c)) {
            System.out.printf("%d\ncaractere invalido.\n", line);
            System.exit(0);
        }
    }

    Token readString() throws IOException {
        String str = "";
        char c = (char) reader.read();
        while (c != '"') {
            if (c == -1) {
                System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
                System.exit(0);
            }
            assertValidChar(c);
            str += c;
            c = (char) reader.read();
        }
        return new Token(TokenType.STRING, str);
    }

    Token readChar() throws IOException {
        char c = (char) reader.read();
        if (c == -1) {
            System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
            System.exit(0);
        }
        assertValidChar(c);

        char apostrophe = (char) reader.read();
        if (apostrophe == -1) {
            System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
            System.exit(0);
        }
        assertValidChar(apostrophe);

        if (apostrophe != '\'') {
            System.out.printf("%d\nlexema nao identificado ['%c%c].\n", line, c, apostrophe);
            System.exit(0);
        }
        return new Token(TokenType.CHAR, c);
    }

    void skipSingleLineComment() throws IOException {
        char c;
        do { c = (char) reader.read(); }
        while (c != '\n' && c != -1);
        if (c == '\n') line++;
    }

    void skipMultiLineComment() throws IOException {
        char c;
        do {
            do {
                c = (char) reader.read();
                if (c == '\n') line++;
            } while (c != '*' && c != -1);

            if (c == -1) {
                System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
                System.exit(0);
            }

            do { c = (char) reader.read(); }
            while (c == '*');

            if (c == '\n') line++;
            else if (c == -1) {
                System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
                System.exit(0);
            }
        } while (c != '/');
    }

    void skipComments() throws IOException {
        char c = (char) reader.read();
        if (c == '/') skipSingleLineComment();
        else if (c == '*') skipMultiLineComment();
        else reader.unread(c);
    }

    char skipSpacesAndComments(char c) throws IOException {
        while (isSpace(c) || c == '/') {
            if (isSpace(c)) {
                if (c == '\n') line++;
                c = skipSpaces();
            }
            if (c == '/') {
                skipComments();
                c = (char) reader.read();
            }
            assertValidChar(c);
        }
        return c;
    }

}

