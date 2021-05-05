import java.io.IOException;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.io.InputStreamReader;

//TODO Verificar o tipo das atribuições [Semantic]

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
    BOOLEAN_CONST,
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

enum SymbolClass{
    CONST,
    VAR
}

enum Type{
    INT,
    CHAR,
    STR,
    BOOL,
    VET
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
        keywords.put("if", new Token(TokenType.IF, "if"));
        keywords.put("then", new Token(TokenType.THEN, "then"));
        keywords.put("else", new Token(TokenType.ELSE, "else"));
        keywords.put("for", new Token(TokenType.FOR, "for"));
        keywords.put("int", new Token(TokenType.INT, "int"));
        keywords.put("char", new Token(TokenType.CHAR, "char"));
        keywords.put("boolean", new Token(TokenType.BOOLEAN, "boolean"));
        keywords.put("final", new Token(TokenType.FINAL, "final"));
        keywords.put("TRUE", new Token(TokenType.BOOLEAN_CONST, "TRUE"));
        keywords.put("FALSE", new Token(TokenType.BOOLEAN_CONST, "FALSE"));
        keywords.put("or", new Token(TokenType.OR, "or"));
        keywords.put("and", new Token(TokenType.AND, "and"));
        keywords.put("not", new Token(TokenType.NOT, "not"));
    }

    Lexer(PushbackReader reader) {
        this.line = 1;
        this.reader = reader;
        initKeywords();
    }

    boolean isValidCharForStr(char c) {
        return (
                isalnum(c) || c == ' ' || c == '\t' || c == '_' || c == '.' || c == ','
                        || c == ';' || c == ':' || c == '(' || c == ')' || c == '{' || c == '}'
                        || c == '[' || c == ']' || c == '=' || c == '<' || c == '>' || c == '%'
                        || c == '+' || c == '-' || c == '*' || c == '/' || c == '\'' || c == '"'
        );
    }

    boolean isValidChar(char c) {
        return (
                isValidCharForStr(c) || c == '\n' || c == '\r' || c == (char) -1
        );
    }

    void assertValidChar(char c) {
        if (!isValidChar(c)) {
            System.out.printf("%d\ncaractere invalido.\n", line);
            System.exit(0);
        }
    }

    char read() throws IOException {
        char c = (char) reader.read();
        assertValidChar(c);
        return c;
    }

    boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F');
    }

    Token readInteger(char firstDigit) throws IOException {
        String str = "" + firstDigit;

        char c = read();
        if (firstDigit != '0') {
            while (c >= '0' && c <= '9') {
                str += c;
                c = read();
            }
        } else {
            if (c >= '0' && c <= '9') {
                str += c;
                c = read();
                if (c == (char) -1) {
                    System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
                    System.exit(0);
                }
                if (c >= '0' && c <= '9') {
                    str += c;
                    c = read();
                    if (c == (char) -1) {
                        System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
                        System.exit(0);
                    }
                    if (c == 'h') {
                        str += c;
                        return new Token(TokenType.HEX_INTEGER, str);
                    } else if (c >= '0' && c <= '9') {
                        str += c;
                        c = read();
                        while (c >= '0' && c <= '9') {
                            str += c;
                            c = read();
                        }
                    } else {
                        System.out.printf("%d\nlexema nao identificado [%s%c].\n", line, str, c);
                        System.exit(0);
                    }
                }
                else if (c >= 'A' && c <= 'F') {
                    str += c;
                    c = read();
                    if (c == (char) -1) {
                        System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
                        System.exit(0);
                    }
                    if (c != 'h') {
                        System.out.printf("%d\nlexema nao identificado [%s%c].\n", line, str, c);
                        System.exit(0);
                    }
                    str += c;
                    return new Token(TokenType.HEX_INTEGER, str);
                }
            }
            else if (c >= 'A' && c <= 'F') {
                str += c;
                c = read();
                if (c == (char) -1) {
                    System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
                    System.exit(0);
                }
                if (!isHex(c)) {
                    reader.unread(c);
                    System.out.printf("%d\nlexema nao identificado [%s].\n", line, str);
                    System.exit(0);
                }
                str += c;
                c = read();
                if (c == (char) -1) {
                    System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
                    System.exit(0);
                }
                if (c != 'h') {
                    System.out.printf("%d\nlexema nao identificado [%s%c].\n", line, str, c);
                    System.exit(0);
                }
                str += c;
                return new Token(TokenType.HEX_INTEGER, str);
            }
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

        char c = read();
        while (isalnum(c) || c == '_') {
            assertValidChar(c);
            str += c;
            c = read();
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
        char c = read();
        while (isSpace(c)) {
            if (c == '\n') line++;
            c = read();
        }
        return c;
    }

    Token readString() throws IOException {
        String str = "";
        char c = read();
        while (c != '"') {
            if (c == (char) -1) {
                System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
                System.exit(0);
            }
            isValidCharForStr(c);
            str += c;
            c = read();
        }
        return new Token(TokenType.STRING, str);
    }

    Token readChar() throws IOException {
        char c = read();
        if (c == (char) -1) {
            System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
            System.exit(0);
        }
        assertValidChar(c);

        char apostrophe = read();
        if (apostrophe == (char) -1) {
            System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
            System.exit(0);
        }
        assertValidChar(apostrophe);

        if (apostrophe != '\'') {
            reader.unread(apostrophe);
            System.out.printf("%d\nlexema nao identificado ['%c].\n", line, c);
            System.exit(0);
        }
        return new Token(TokenType.CHAR, c);
    }

    void skipSingleLineComment() throws IOException {
        char c;
        do { c = read(); }
        while (c != '\n' && c != (char) -1);
        if (c == '\n') line++;
    }

    void skipMultiLineComment() throws IOException {
        char c;
        do {
            do {
                c = read();
                if (c == '\n') line++;
            } while (c != '*' && c != (char) -1);

            if (c == (char) -1) {
                System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
                System.exit(0);
            }

            do { c = read(); }
            while (c == '*');

            if (c == '\n') line++;
            else if (c == (char) -1) {
                System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
                System.exit(0);
            }
        } while (c != '/');
    }

    void skipComments() throws IOException {
        char c = read();
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
                c = read();
            }
            assertValidChar(c);
        }
        return c;
    }

    Token next() throws IOException {
        char c = read();
        c = skipSpacesAndComments(c);
        if (c == (char) -1) return new Token(TokenType.EOF, (char) -1);
        if (c == ',') return new Token(TokenType.COMMA, ',');
        if (c == ';') return new Token(TokenType.SEMICOLON, ';');
        if (c == '+') return new Token(TokenType.PLUS, '+');
        if (c == '-') return new Token(TokenType.MINUS, '-');
        if (c == '*') return new Token(TokenType.ASTERISK, '*');
        if (c == '/') return new Token(TokenType.BACKSLASH, '/');
        if (c == '(') return new Token(TokenType.LEFT_PAREN, '(');
        if (c == ')') return new Token(TokenType.RIGHT_PAREN, ')');
        if (c == '[') return new Token(TokenType.LEFT_BRACKET, '[');
        if (c == ']') return new Token(TokenType.RIGHT_BRACKET, ']');
        if (c == '{') return new Token(TokenType.LEFT_BRACES, '{');
        if (c == '}') return new Token(TokenType.RIGHT_BRACES, '}');
        if (c == '%') return new Token(TokenType.PERCENT, '%');
        if (c == '=') return new Token(TokenType.EQUAL, '=');
        if (c == '"') return readString();
        if (c == '\'') return readChar();
        if (c >= '0' && c <= '9') return readInteger(c);
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_')
            return readIdentifier(c);
        if (c == '<')
        {
            char c2 = read();
            if (c2 == '>') return new Token(TokenType.DIFFERENT, "<>");
            else if (c2 == '=') return new Token(TokenType.SMALLER_OR_EQUAL, "<=");
            else {
                reader.unread(c2);
                return new Token(TokenType.SMALLER, '<');
            }
        }
        if (c == '>')
        {
            char c2 = read();
            if (c2 == '=') return new Token(TokenType.GREATER_OR_EQUAL, ">=");
            else {
                reader.unread(c2);
                return new Token(TokenType.GREATER, ">");
            }
        }
        if (c == ':')
        {
            char c2 = read();
            if (c2 == (char) -1) {
                System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
                System.exit(0);
            }
            else if (c2 != '=') {
                System.out.printf("%d\nlexema nao identificado [:%c].\n", line, c2);
                System.exit(0);
            } else return new Token(TokenType.ASSIGN, ":=");
        }

        System.out.printf("%d\nlexema nao identificado [%c].\n", line, c);
        System.exit(0);
        return new Token(TokenType.EOF, "");
    }
}

abstract class Node {
}

abstract class StatementNode extends Node {
}

abstract class ExpressionNode extends Node {
}

class BooleanExpressionNode extends ExpressionNode {
    boolean value;
    BooleanExpressionNode(boolean value) {
        this.value = value;
    }
}

class IntExpressionNode extends ExpressionNode {
    int value;
    IntExpressionNode(int value) {
        this.value = value;
    }
}

class CharExpressionNode extends ExpressionNode {
    char value;
    CharExpressionNode(char value) {
        this.value = value;
    }
}

class StringExpressionNode extends ExpressionNode {
    String value;
    StringExpressionNode(String value) {
        this.value = value;
    }
}

class ParenthesizedExpressionNode extends ExpressionNode {
    ExpressionNode expression;
    ParenthesizedExpressionNode(ExpressionNode expression) {
        this.expression = expression;
    }
}

class UnaryExpressionNode extends ExpressionNode {
    String operator;
    ExpressionNode expression;
    UnaryExpressionNode(String operator, ExpressionNode expression) {
        this.operator = operator;
        this.expression = expression;
    }
}

class BinaryExpressionNode extends ExpressionNode {
    ExpressionNode leftExpression;
    String operator;
    ExpressionNode rightExpression;
    BinaryExpressionNode(
            ExpressionNode leftExpression, String operator, ExpressionNode rightExpression
    ) {
        this.leftExpression = leftExpression;
        this.operator = operator;
        this.rightExpression = rightExpression;
    }
}

class IdentifierExpressionNode extends ExpressionNode {
    String identifier;
    IdentifierExpressionNode(String identifier) {
        this.identifier = identifier;
    }
}

class ArraySubscriptExpressionNode extends ExpressionNode {
    String identifier;
    ExpressionNode subscriptExpr;
    ArraySubscriptExpressionNode(String identifier, ExpressionNode subscriptExpr) {
        this.identifier = identifier;
        this.subscriptExpr = subscriptExpr;
    }
}

class VarDeclStatementNode extends StatementNode {
    String identifier;
    ExpressionNode size;
    ExpressionNode value;
    VarDeclStatementNode(
            String identifier,
            ExpressionNode size,
            ExpressionNode value
    ) {
        this.identifier = identifier;
        this.size = size;
        this.value = value;
    }
}

class VarDeclsStatementNode extends StatementNode {
    ArrayList<VarDeclStatementNode> varsDecl = new ArrayList<>();
}

class ConstDeclStatementNode extends StatementNode {
    String identifier;
    ExpressionNode value;
    ConstDeclStatementNode(
            String identifier,
            ExpressionNode value
    ) {
        this.identifier = identifier;
        this.value = value;
    }
}

class ConstDeclsStatementNode extends StatementNode {
    ArrayList<ConstDeclStatementNode> constDecls = new ArrayList<>();
}

class CompoundStatementNode extends StatementNode {
    ArrayList<StatementNode> stmts = new ArrayList<>();
}

class ExpressionStatementNode extends StatementNode {
    ExpressionNode expression;
    ExpressionStatementNode(ExpressionNode expression) {
    }
}

class WriteStatementNode extends StatementNode {
    ArrayList<ExpressionNode> args = new ArrayList<>();
}

class WritelnStatementNode extends StatementNode {
    ArrayList<ExpressionNode> args = new ArrayList<>();
}

abstract class ReadlnStatementNode extends StatementNode {
}

class ReadlnVarStatementNode extends ReadlnStatementNode {
    String identifier;
    ReadlnVarStatementNode(String identifier) {
        this.identifier = identifier;
    }
}

class ReadlnArrayStatementNode extends ReadlnStatementNode {
    String identifier;
    ExpressionNode expression;
    ReadlnArrayStatementNode(String identifier, ExpressionNode expression) {
        this.identifier = identifier;
        this.expression = expression;
    }
}

class IfStatementNode extends StatementNode {
    ExpressionNode expression;
    StatementNode ifStatement;
    StatementNode elseStatement;
    IfStatementNode(
            ExpressionNode expression,
            StatementNode ifStatement,
            StatementNode elseStatement
    ) {
        this.expression = expression;
        this.ifStatement = ifStatement;
        this.elseStatement = elseStatement;
    }
}

abstract class AssignStatementNode extends StatementNode {
    String identifier;
    ExpressionNode value;
    AssignStatementNode(String identifier, ExpressionNode value) {
        this.identifier = identifier;
        this.value = value;
    }
}

class IdentifierAssignStatementNode extends AssignStatementNode {
    IdentifierAssignStatementNode(String identifier, ExpressionNode value) {
        super(identifier, value);
    }
}

class ArraySubscriptAssignStatementNode extends AssignStatementNode {
    ExpressionNode index;
    ArraySubscriptAssignStatementNode(
            String identifier, ExpressionNode index, ExpressionNode value
    ) {
        super(identifier, value);
        this.index = index;
    }
}

class ForStatementNode extends StatementNode {
    CompoundStatementNode init;
    ExpressionNode condition;
    CompoundStatementNode inc;
    StatementNode stmt;
    ForStatementNode(
            CompoundStatementNode init,
            ExpressionNode condition,
            CompoundStatementNode inc,
            StatementNode stmt
    ) {
        this.init = init;
        this.condition = condition;
        this.inc = inc;
        this.stmt = stmt;
    }
}

class ProgramNode extends Node {
    ArrayList<VarDeclsStatementNode> varDecls = new ArrayList<>();
    ArrayList<ConstDeclsStatementNode> constDecls = new ArrayList<>();
    ArrayList<StatementNode> stmts = new ArrayList<>();
}

class ParserUtils {
    public static int getTypeSize(TokenType type, int size){
        int size2 = 0;
        // INTEGER,
        //    HEX_INTEGER,
        //    STRING,
        //    BOOLEAN_CONST,
        switch(type){
            case INT: size2 = 2; break;
            case INTEGER: size2 = 2; break;
            case HEX_INTEGER: size2 = 2; break;
            case BOOLEAN: size2 = 1; break;
            case BOOLEAN_CONST: size2 = 1; break;
            case CHAR: size2 = 1; break;
            case IDENTIFIER: break;
            default :
                System.out.println("ERROR: Type not supported");
        }

        if(size != 0)
            size2 *= size;

        return size2;
    }

    public static TokenType getType(ExpressionNode node){
        TokenType tokenType = null;
        if(node instanceof ArraySubscriptExpressionNode){
            tokenType = getBinary(((ArraySubscriptExpressionNode) node).subscriptExpr);
            if(tokenType == null)
                return getLiteral(((ArraySubscriptExpressionNode) node).subscriptExpr);
        }else if(node instanceof BinaryExpressionNode){
            return getBinary(node);
        }else if(node instanceof IntExpressionNode){
            tokenType = getLiteral(node);
        }

        return tokenType;
    }

    private static TokenType getLiteral(ExpressionNode node){
        if(node instanceof IntExpressionNode){
            return TokenType.INTEGER;
        } else if(node instanceof BooleanExpressionNode){
            return TokenType.BOOLEAN_CONST;
        }else if(node instanceof CharExpressionNode){
            return TokenType.CHAR;
        }

        return null;
    }

    private static TokenType getBinary(ExpressionNode node){
        if(node instanceof BinaryExpressionNode){
            return getLiteral(((BinaryExpressionNode) node).rightExpression);
        }

        return null;
    }
}

class Parser {
    Lexer lexer;
    Token currentToken;
    Semantic semantic;


    Parser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        this.currentToken = lexer.next();
        this.semantic = new Semantic(lexer);
    }

    void tokenNotExpected() {
        if (currentToken.type == TokenType.EOF)
            System.out.printf("%d\nfim de arquivo nao esperado.\n", lexer.line);
        else
            System.out.printf("%d\ntoken nao esperado [%s].\n",
                    lexer.line, currentToken.value);
        System.exit(0);
    }

    void eat() throws IOException {
        // System.out.printf("Ate type = %s, value = '%s'\n",
        //     currentToken.type.name(), currentToken.value);
        currentToken = lexer.next();
    }

    void eat(TokenType type) throws IOException {
        if (currentToken.type == type) eat();
        else tokenNotExpected();
    }

    ExpressionNode parseConstExpression() throws IOException {
        ExpressionNode node = null;
        switch (currentToken.type) {
            case BOOLEAN_CONST:
                node = new BooleanExpressionNode(currentToken.value.equals("TRUE"));
                eat();
                break;

            case CHAR:
                node = new CharExpressionNode(currentToken.value.charAt(0));
                eat();
                break;

            case INTEGER:
                node = new IntExpressionNode(Integer.parseInt(currentToken.value));
                eat();
                break;

            case HEX_INTEGER:
                String hexStr = currentToken.value.substring(1, 3);
                node = new IntExpressionNode(Integer.parseInt(hexStr, 16));
                eat();
                break;

            case STRING:
                node = new StringExpressionNode(currentToken.value);
                eat();
                break;
        }
        return node;
    }

    ExpressionNode parsePrimaryExpression() throws IOException {
        ExpressionNode node = parseConstExpression();
        if (node != null) return node;
        switch (currentToken.type) {
            case LEFT_PAREN:
                eat();
                node = new ParenthesizedExpressionNode(parseExpression());
                eat(TokenType.RIGHT_PAREN);
                break;

            case IDENTIFIER:
                ExpressionNode subscriptExpr = null;
                String identifier = currentToken.value;
                eat();

                if (currentToken.type == TokenType.LEFT_BRACKET) {
                    eat(TokenType.LEFT_BRACKET);
                    subscriptExpr = parseExpression();
                    eat(TokenType.RIGHT_BRACKET);
                }

                if (subscriptExpr == null) {
                    node = new IdentifierExpressionNode(identifier);
                } else {
                    node = new ArraySubscriptExpressionNode(identifier, subscriptExpr);
                }
                break;

            default: tokenNotExpected(); break;
        }
        return node;
    }

    ExpressionNode parseUnaryExpression() throws IOException {
        ExpressionNode node = null;
        String operator = currentToken.value;
        switch (currentToken.type) {
            case NOT:
                eat();
                node = new UnaryExpressionNode(operator, parseUnaryExpression());
                break;
        }
        return node == null ? parsePrimaryExpression() : node;
    }

    ExpressionNode parseMultiplicativeExpression() throws IOException {
        ExpressionNode node = parseUnaryExpression();
        while (
                currentToken.type == TokenType.ASTERISK
                        || currentToken.type == TokenType.BACKSLASH
                        || currentToken.type == TokenType.PERCENT
                        || currentToken.type == TokenType.AND
        ) {
            String operator = currentToken.value;
            eat();
            node = new BinaryExpressionNode(node, operator, parseUnaryExpression());
        }
        return node;
    }

    ExpressionNode parseAdditiveExpression() throws IOException {
        ExpressionNode node = null;
        if (currentToken.type == TokenType.PLUS || currentToken.type == TokenType.MINUS) {
            String operator = currentToken.value;
            eat();
            node = new UnaryExpressionNode(
                    operator, parseMultiplicativeExpression()
            );
        } else {
            node = parseMultiplicativeExpression();
        }
        while (
                currentToken.type == TokenType.PLUS
                        || currentToken.type == TokenType.MINUS
                        || currentToken.type == TokenType.OR
        ) {
            String operator = currentToken.value;
            eat();
            node = new BinaryExpressionNode(
                    node, operator, parseMultiplicativeExpression()
            );
        }
        return node;
    }

    ExpressionNode parseRelationalExpression() throws IOException {
        ExpressionNode node = parseAdditiveExpression();
        while (
                currentToken.type == TokenType.DIFFERENT
                        || currentToken.type == TokenType.EQUAL
                        || currentToken.type == TokenType.SMALLER
                        || currentToken.type == TokenType.GREATER
                        || currentToken.type == TokenType.SMALLER_OR_EQUAL
                        || currentToken.type == TokenType.GREATER_OR_EQUAL
        ) {
            String operator = currentToken.value;
            eat();
            node = new BinaryExpressionNode(node, operator, parseAdditiveExpression());
        }
        return node;
    }

    ExpressionNode parseExpression() throws IOException {
        ExpressionNode node = parseRelationalExpression();
        // System.out.println(node.getClass().getName());
        return node;
    }

    VarDeclStatementNode parseVarDecl(TokenType tokenType) throws IOException {
        String identifier = currentToken.value;
        ExpressionNode size = null;
        ExpressionNode value = null;
        eat(TokenType.IDENTIFIER);
        int vetSize = 0;
        int varValue = 0;

        if (currentToken.type == TokenType.LEFT_BRACKET) {
            eat();
            size = parseConstExpression();
            if (size == null) tokenNotExpected();

            IntExpressionNode intNode = (IntExpressionNode)size;
            vetSize = intNode.value;

            //Semantic Action
            this.semantic.verifyVetSize(identifier, ParserUtils.getTypeSize(tokenType, vetSize));

            eat(TokenType.RIGHT_BRACKET);
        }

        else if (currentToken.type == TokenType.ASSIGN) {
            eat();
            TokenType currentTokenType = currentToken.type;

            value = parseConstExpression();
            if (value == null) tokenNotExpected();

            //Semantic Action
            this.semantic.verifyDeclaredVariable(identifier);
            this.semantic.verifyTypeCompatibility(identifier, currentTokenType);

            return new VarDeclStatementNode(identifier, size, value);
        }

        //Semantic Action
        this.semantic.addSymbol(new Symbol(identifier, SymbolClass.VAR, tokenType, vetSize));
        return new VarDeclStatementNode(identifier, size, value);
    }

    VarDeclsStatementNode parseVarDecls(TokenType tokenType) throws IOException {
        VarDeclsStatementNode node = new VarDeclsStatementNode();
        node.varsDecl.add(parseVarDecl(tokenType));
        while (currentToken.type == TokenType.COMMA) {
            eat();
            tokenType = currentToken.type;
            node.varsDecl.add(parseVarDecl(tokenType));
        }
        // System.out.println(node.getClass().getName());
        return node;
    }

    ConstDeclStatementNode parseConstDecl() throws IOException {
        String identifier = currentToken.value;
        ExpressionNode value = null;
        eat(TokenType.IDENTIFIER);
        eat(TokenType.EQUAL);

        TokenType tokenType = currentToken.type;

        value = parseConstExpression();

        if (value == null) tokenNotExpected();
        this.semantic.addSymbol(new Symbol(identifier, SymbolClass.CONST, tokenType, 0)); //Semantic Action

        return new ConstDeclStatementNode(identifier, value);
    }

    ConstDeclsStatementNode parseConstDecls() throws IOException {
        ConstDeclsStatementNode node = new ConstDeclsStatementNode();
        node.constDecls.add(parseConstDecl());
        while (currentToken.type == TokenType.COMMA) {
            eat();
            node.constDecls.add(parseConstDecl());
        }
        return node;
    }

    WriteStatementNode parseWriteStatement() throws IOException {
        WriteStatementNode node = new WriteStatementNode();
        eat(TokenType.LEFT_PAREN);
        node.args.add(parseExpression());
        while (currentToken.type == TokenType.COMMA) {
            eat(TokenType.COMMA);
            node.args.add(parseExpression());
        }
        eat(TokenType.RIGHT_PAREN);
        return node;
    }

    WritelnStatementNode parseWritelnStatement() throws IOException {
        WritelnStatementNode node = new WritelnStatementNode();
        eat(TokenType.LEFT_PAREN);
        node.args.add(parseExpression());
        while (currentToken.type == TokenType.COMMA) {
            eat(TokenType.COMMA);
            node.args.add(parseExpression());
        }
        eat(TokenType.RIGHT_PAREN);
        return node;
    }

    ReadlnStatementNode parseReadlnStatement() throws IOException {
        ReadlnStatementNode node;
        eat(TokenType.LEFT_PAREN);
        String identifier = currentToken.value;
        eat(TokenType.IDENTIFIER);
        if (currentToken.type == TokenType.LEFT_BRACKET) {
            eat();
            ExpressionNode expression = parseExpression();
            node = new ReadlnArrayStatementNode(identifier, expression);
            eat(TokenType.RIGHT_BRACKET);
        } else {
            node = new ReadlnVarStatementNode(identifier);
        }
        eat(TokenType.RIGHT_PAREN);
        return node;
    }

    IfStatementNode parseIfStatement() throws IOException {
        eat(TokenType.LEFT_PAREN);
        ExpressionNode expression = parseExpression();
        eat(TokenType.RIGHT_PAREN);
        eat(TokenType.THEN);
        StatementNode ifStatement = parseStatementOrStatements();
        StatementNode elseStatement = null;
        if (currentToken.type == TokenType.ELSE) {
            eat();
            elseStatement = parseStatementOrStatements();
        }
        IfStatementNode node = new IfStatementNode(expression, ifStatement, elseStatement);
        return node;
    }

    AssignStatementNode parseAssignStatement() throws IOException {
        AssignStatementNode node;
        ExpressionNode subscriptExpr = null;
        String identifier = currentToken.value;

        //Semantic Action
        this.semantic.verifyDeclaredVariable(identifier);

        eat();

        if (currentToken.type == TokenType.LEFT_BRACKET) {
            eat();
            subscriptExpr = parseExpression();
            eat(TokenType.RIGHT_BRACKET);
        }

        eat(TokenType.ASSIGN);

        if (subscriptExpr == null) {
            node = new IdentifierAssignStatementNode(identifier, parseExpression());
        } else {
            node = new ArraySubscriptAssignStatementNode(
                    identifier, subscriptExpr, parseExpression()
            );
        }

        //Semantic Action
        this.semantic.verifyTypeCompatibility(identifier, ParserUtils.getType(node.value));
        this.semantic.verifyClassCompatibility(identifier);

        return node;
    }

    CompoundStatementNode parseCommaSeparatedStatements() throws IOException {
        CompoundStatementNode node = new CompoundStatementNode();
        node.stmts.add(parseStatement());
        while (currentToken.type == TokenType.COMMA) {
            eat();
            node.stmts.add(parseStatement());
        }
        return node;
    }

    ForStatementNode parseForStatement() throws IOException {
        eat(TokenType.LEFT_PAREN);
        CompoundStatementNode init = null, inc = null;
        ExpressionNode condition = null;

        if (currentToken.type != TokenType.SEMICOLON)
            init = parseCommaSeparatedStatements();
        eat(TokenType.SEMICOLON);

        condition = parseExpression();
        eat(TokenType.SEMICOLON);

        if (currentToken.type != TokenType.RIGHT_PAREN)
            inc = parseCommaSeparatedStatements();

        eat(TokenType.RIGHT_PAREN);
        ForStatementNode node = new ForStatementNode(
                init, condition, inc, parseStatementOrStatements()
        );
        return node;
    }

    StatementNode parseStatement() throws IOException {
        StatementNode node = null;
        switch (currentToken.type) {
            case WRITE:
                eat();
                node = parseWriteStatement();
                break;

            case WRITELN:
                eat();
                node = parseWritelnStatement();
                break;

            case READLN:
                eat();
                node = parseReadlnStatement();
                break;

            case IDENTIFIER:
                node = parseAssignStatement();
                break;

            case IF:
                eat();
                node = parseIfStatement();
                break;

            case FOR:
                eat();
                node = parseForStatement();
                break;

            default: tokenNotExpected(); break;
        }
        // System.out.println(node.getClass().getName());
        return node;
    }

    StatementNode parseTerminatedStatement() throws IOException {
        StatementNode node = null;
        switch (currentToken.type) {
            case WRITE:
                eat();
                node = parseWriteStatement();
                eat(TokenType.SEMICOLON);
                break;

            case WRITELN:
                eat();
                node = parseWritelnStatement();
                eat(TokenType.SEMICOLON);
                break;

            case READLN:
                eat();
                node = parseReadlnStatement();
                eat(TokenType.SEMICOLON);
                break;

            case IDENTIFIER:
                node = parseAssignStatement();
                eat(TokenType.SEMICOLON);
                break;

            case IF:
                eat();
                node = parseIfStatement();
                break;

            case FOR:
                eat();
                node = parseForStatement();
                break;

            default: tokenNotExpected(); break;
        }
        // System.out.println(node.getClass().getName());
        return node;
    }

    StatementNode parseStatementOrStatements() throws IOException {
        StatementNode node = null;
        if (currentToken.type == TokenType.LEFT_BRACES) {
            eat();
            CompoundStatementNode mynode = new CompoundStatementNode();
            node = mynode;
            while (currentToken.type != TokenType.RIGHT_BRACES)
                mynode.stmts.add(parseTerminatedStatement());
            eat(TokenType.RIGHT_BRACES);
        } else {
            node = parseTerminatedStatement();
        }
        return node;
    }

    ProgramNode parseProgram() throws IOException {
        ProgramNode node = new ProgramNode();
        while (
                currentToken.type == TokenType.INT
                        || currentToken.type == TokenType.CHAR
                        || currentToken.type == TokenType.BOOLEAN
                        || currentToken.type == TokenType.FINAL
        ) {
            System.out.println("TOKEN TYPE: " + currentToken.type + " TOKEN VALUE: " + currentToken.value);
            TokenType currentTokenType = currentToken.type;
            if (currentTokenType == TokenType.FINAL) {
                eat();
                node.constDecls.add(parseConstDecls());
            } else {
                eat();
                node.varDecls.add(parseVarDecls(currentTokenType));
            }
            eat(TokenType.SEMICOLON);
        }
        eat(TokenType.MAIN);
        eat(TokenType.LEFT_BRACES);
        while (currentToken.type != TokenType.RIGHT_BRACES)
            node.stmts.add(parseTerminatedStatement());
        eat(TokenType.RIGHT_BRACES);
        eat(TokenType.EOF);

        System.out.println(this.semantic.symTable);

        return node;
    }
}

/* Semantico Area */

class Semantic{
    public SymTable symTable;
    public Lexer lexer;
    public int address = 16384;

    public Semantic(Lexer lexer){
        this.symTable = new SymTable();
        this.lexer = lexer;
    }

    public void addSymbol(Symbol s) throws IOException{
        Symbol symbol = symTable.getSymbol(s.getName());

        if(symbol == null){
            int size = ParserUtils.getTypeSize(s.type, s.size);
            s.address = address;
            address+= size;

            symTable.addSymbol(s);
        }
        else{
            SemanticErros.declaredVariable(s.getName(), lexer.line);
        }
    }

    private Symbol getDeclaredSymbol(String symbolName){
        Symbol symbol = symTable.getSymbol(symbolName);

        if(symbol == null){
            SemanticErros.undeclaredVariable(symbol.getName(), lexer.line);
        }

        return symbol;
    }

    public void verifyDeclaredVariable(String symbolName){
        if(!containsSymbol(symbolName))
            SemanticErros.undeclaredVariable(symbolName, lexer.line);
    }

    public void verifyVetSize(String symbolName, int size){
        if(size > 8000){
            SemanticErros.vetOverflow(symbolName, size, lexer.line);
        }
    }

    public void verifyTypeCompatibility(String symbolName, TokenType tokenType){
        Symbol symbol = getDeclaredSymbol(symbolName);

        verifyTypeCompatibility(symbol.type, tokenType);
    }

    public void verifyClassCompatibility(String symbolName){
        Symbol symbol = getDeclaredSymbol(symbolName);

        if(symbol.symbolClass == SymbolClass.CONST){
            SemanticErros.changeConst(symbolName, lexer.line);
        }
    }

    private void verifyTypeCompatibility(TokenType tokenTypeLeft, TokenType tokenTypeRight){
        if(tokenTypeLeft == TokenType.INT && (tokenTypeRight != TokenType.INTEGER && tokenTypeRight != TokenType.HEX_INTEGER)){
            SemanticErros.incompatibleType(tokenTypeLeft, tokenTypeRight, lexer.line);
        }else if(tokenTypeLeft == TokenType.CHAR && (tokenTypeRight != TokenType.CHAR && tokenTypeRight != TokenType.STRING)){
            SemanticErros.incompatibleType(tokenTypeLeft, tokenTypeRight, lexer.line);
        }else if(tokenTypeLeft == TokenType.BOOLEAN && tokenTypeRight != TokenType.BOOLEAN_CONST){
            SemanticErros.incompatibleType(tokenTypeLeft, tokenTypeRight, lexer.line);
        }
    }


    private boolean containsSymbol(String symbolName){
        Symbol symbol = symTable.getSymbol(symbolName);

        return symbol != null;
    }
}

class SemanticErros{
    public static void declaredVariable(String variableName, int line){
        System.out.println(String.format("[%d] VARIÁVEL '%s' JÁ DECLARADA", line, variableName));
        breakProgram();
    }

    public static void undeclaredVariable(String variableName, int line){
        System.out.println(String.format("[%d] VARIÁVEL '%s' NÃO DECLARADA", line, variableName));
        breakProgram();
    }

    public static void vetOverflow(String name, int size, int line){
        System.out.println(String.format("[%d] TAMANHO DE VETOR NÃO SUPORTADO -> Nome do identificador: '%s' Tamanho em bytes: '%d'", line, name, size));
        breakProgram();
    }

    public static void incompatibleType(TokenType tokenTypeLeft, TokenType tokenTypeRight, int line){
        System.out.println(String.format("[%d] TIPOS INCOMPATIVEIS -> %s com %s", line, tokenTypeLeft.toString(), tokenTypeRight.toString()));
        breakProgram();
    }

    public static void changeConst(String symbolName, int line){
        System.out.println(String.format("[%d] APENAS VARIÁVEIS PODEM SER ATRIBUIDAS -> Nome identificador %s", line, symbolName));
        breakProgram();
    }

    private static void breakProgram(){
        System.out.println("COMPILAÇÃO TERMINADA DEVIDO A ERRO SEMÂNTICO!");
        System.exit(0);
    }
}

class Symbol{
    public String name;
    public SymbolClass symbolClass;
    public TokenType type;
    public int size;
    public int address;

    public Symbol(String name, SymbolClass symbolClass, TokenType type, int size){
        this.name = name;
        this.symbolClass = symbolClass;
        this.type = type;
        this.size = size;
    }

    public String getName(){
        return this.name;
    }
}

class SymTable{
    private HashMap<String, Symbol> symTable;

    public SymTable(){
        this.symTable = new HashMap<>();
    }

    public void addSymbol(Symbol s){
        symTable.put(s.getName(), s);

    }

    public Symbol getSymbol(String name){
        return symTable.get(name);
    }

    @Override
    public String toString(){
        String str = "SYMBOL TABLE\n";
        for (Entry<String, Symbol> entry : symTable.entrySet()) {
            Symbol s = entry.getValue();
            str+= String.format("NAME: %s SYMBOL CLASS: %s TYPE: %s SIZE: %d ADDRESS: %d \n",
                    s.getName(), s.symbolClass, s.type, s.size, s.address);
        }

        return str;
    }
}

// class CodeGenerator {
//     int varCounter = 0;
//     int stackSize = 0;
//     List<String> dataSection = new ArrayList<>();
//     List<String> codeSection = new ArrayList<>();

//     private void addData(String line, int size) {
//         dataSection.add(line);
//         stackSize += size;
//     }

//     private void addCode(String line) {
//         codeSection.add(line);
//     }

//     public void generate(WriteStatementNode node) {
//         String str = "";
//         for (ExpressionNode arg : node.args) {
//             Class<?> argClass = arg.getClass();
//             if (argClass.equals(StringExpressionNode.class)) {
//                 StringExpressionNode aux = (StringExpressionNode) arg;
//                 str += aux.value;
//             } else if (argClass.equals(IntExpressionNode.class)) {
//                 IntExpressionNode aux = (IntExpressionNode) arg;
//                 str += aux.value;
//             }
//         }
//         addData(
//             String.format("var%d db \"%s\", '$'", varCounter, str),
//             str.getBytes().length + 1
//         );
//         addCode(String.format("print var%d", varCounter));
//         varCounter++;
//     }

//     public void generate(WritelnStatementNode node) {
//         WriteStatementNode auxNode = new WriteStatementNode();
//         auxNode.args = new ArrayList<>(node.args);
//         auxNode.args.add(new StringExpressionNode("\n"));
//     }

//     public void generate(StatementNode node) {
//         Class<?> nodeClass = node.getClass();
//         if (nodeClass.equals(WriteStatementNode.class))
//             generate((WriteStatementNode) node);
//     }

//     public void generate(ArrayList<StatementNode> nodes) {
//         for (StatementNode node : nodes) generate(node);
//     }

//     public String generate(ProgramNode node) {
//         generate(node.stmts);
//         return String.format("""
// .model small
// .stack %d

// print macro msg
//     lea dx, msg
//     mov ah, 09h
//     int 21h
// endm

// .data
// %s

// .code
//     MOV AX, @DATA
//     MOV DS, AX

//     %s

//     MOV AH, 4CH ; Exit
//     INT 21H
// end
// """, stackSize, String.join("\n", dataSection), String.join("\n", codeSection));
//     }
// }

public class Main {
    public static void main(String[] args) throws IOException {
        // try (Scanner scan = new Scanner(System.in)) {
        //     try {
        //         while (true) {
        //             System.err.println(scan.nextLine());
        //         }
        //     } catch (Exception e) {
        //     }
        // }
        // System.exit(1);
        System.out.println("START");
        PushbackReader input = new PushbackReader(new InputStreamReader(System.in), 3);
        Lexer lexer = new Lexer(input);
        // Token tok = lexer.next();
        // while (tok.type != TokenType.EOF) {
        //     // System.out.printf("id = %s, lexem = '%s'\n", tok.type.name(), tok.value);
        //     tok = lexer.next();
        // }
        ProgramNode node = new Parser(lexer).parseProgram();
        // System.out.printf(new CodeGenerator().generate(node));
        System.out.printf("%d linhas compiladas.\n", lexer.line);
    }
}