import java.io.IOException;
import java.io.PushbackReader;
import java.util.ArrayList;
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

    Token next() throws IOException {
        char c = (char) reader.read();
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
            char c2 = (char) reader.read();
            if (c2 == '>') return new Token(TokenType.DIFFERENT, "<>");
            else if (c2 == '=') return new Token(TokenType.SMALLER_OR_EQUAL, "<=");
            else {
                reader.unread(c2);
                return new Token(TokenType.SMALLER, '<');
            }
        }
        if (c == '>')
        {
            char c2 = (char) reader.read();
            if (c2 == '=') return new Token(TokenType.GREATER_OR_EQUAL, ">=");
            else {
                reader.unread(c2);
                return new Token(TokenType.GREATER, ">");
            }
        }
        if (c == ':')
        {
            char c2 = (char) reader.read();
            if (c2 == -1) {
                System.out.printf("%d\nfim de arquivo nao esperado.\n", line);
                System.exit(0);
            }
            else if (c2 != '=') {
                System.out.printf("%d\nlexema nao identificado [:%c].\n", line, c2);
                System.exit(0);
            } else return new Token(TokenType.ASSIGN, ":=");
        }

        System.out.printf("%d\ncaractere invalido.\n", line);
        System.exit(0);
        return new Token(TokenType.EOF, "");
    }
}

enum ASTNodeType {
    PROGRAM,
    VAR_DECL_STATEMENT,
    VARS_DECL_STATEMENT,
    COMPOUND_STATEMENT,
    EXPRESSION_STATEMENT,
    WRITE_STATEMENT,
    WRITELN_STATEMENT,
    READLN_VAR_STATEMENT,
    READLN_ARRAY_STATEMENT,
    RETURN_STATEMENT,
    IF_STATEMENT,
    FOR_STATEMENT,
    INT_EXPRESSION,
    CHAR_EXPRESSION,
    STRING_EXPRESSION,
    BOOLEAN_EXPRESSION,
    IDENTIFIER_EXPRESSION,
    PARENTHESIZED_EXPRESSION,
    ASSIGNMENT_BINARY_EXPRESSION,
    ARITHMETIC_UNARY_EXPRESSION,
    ARITHMETIC_BINARY_EXPRESSION,
    BOOLEAN_UNARY_EXPRESSION,
    BOOLEAN_BINARY_EXPRESSION,
    IDENTIFIER_ASSIGN_EXPRESSION,
    ARRAY_SUBSCRIPT_EXPRESSION,
    ARRAY_SUBSCRIPT_ASSIGN_EXPRESSION,
}

abstract class Node {
    ASTNodeType type;
    Node(ASTNodeType type) {
        this.type = type;
    }
}

abstract class StatementNode extends Node {
    StatementNode(ASTNodeType type) {
        super(type);
    }
}

abstract class ExpressionNode extends Node {
    ExpressionNode(ASTNodeType type) {
        super(type);
    }
}

class BooleanExpressionNode extends ExpressionNode {
    boolean value;
    BooleanExpressionNode(boolean value) {
        super(ASTNodeType.BOOLEAN_EXPRESSION);
        this.value = value;
    }
}

class IntExpressionNode extends ExpressionNode {
    int value;
    IntExpressionNode(int value) {
        super(ASTNodeType.INT_EXPRESSION);
        this.value = value;
    }
}

class CharExpressionNode extends ExpressionNode {
    char value;
    CharExpressionNode(char value) {
        super(ASTNodeType.CHAR_EXPRESSION);
        this.value = value;
    }
}

class StringExpressionNode extends ExpressionNode {
    String value;
    StringExpressionNode(String value) {
        super(ASTNodeType.STRING_EXPRESSION);
        this.value = value;
    }
}

class ParenthesizedExpressionNode extends ExpressionNode {
    ExpressionNode expression;
    ParenthesizedExpressionNode(ExpressionNode expression) {
        super(ASTNodeType.PARENTHESIZED_EXPRESSION);
        this.expression = expression;
    }
}

class AssignmentBinaryExpressionNode extends ExpressionNode {
    ExpressionNode leftExpression;
    String operator;
    ExpressionNode rightExpression;
    AssignmentBinaryExpressionNode(
        ExpressionNode leftExpression, String operator, ExpressionNode rightExpression
    ) {
        super(ASTNodeType.ASSIGNMENT_BINARY_EXPRESSION);
        this.leftExpression = leftExpression;
        this.operator = operator;
        this.rightExpression = rightExpression;
    }
}

class ArithmeticUnaryExpressionNode extends ExpressionNode {
    String operator;
    ExpressionNode expression;
    ArithmeticUnaryExpressionNode(String operator, ExpressionNode expression) {
        super(ASTNodeType.ARITHMETIC_UNARY_EXPRESSION);
        this.operator = operator;
        this.expression = expression;
    }
}

class ArithmeticBinaryExpressionNode extends ExpressionNode {
    ExpressionNode leftExpression;
    String operator;
    ExpressionNode rightExpression;
    ArithmeticBinaryExpressionNode(
        ExpressionNode leftExpression, String operator, ExpressionNode rightExpression
    ) {
        super(ASTNodeType.ARITHMETIC_BINARY_EXPRESSION);
        this.leftExpression = leftExpression;
        this.operator = operator;
        this.rightExpression = rightExpression;
    }
}

class BooleanUnaryExpressionNode extends ExpressionNode {
    String operator;
    ExpressionNode expression;
    BooleanUnaryExpressionNode(String operator, ExpressionNode expression) {
        super(ASTNodeType.BOOLEAN_UNARY_EXPRESSION);
        this.operator = operator;
        this.expression = expression;
    }
}

class BooleanBinaryExpressionNode extends ExpressionNode {
    ExpressionNode leftExpression;
    String operator;
    ExpressionNode rightExpression;
    BooleanBinaryExpressionNode(
        ExpressionNode leftExpression, String operator, ExpressionNode rightExpression
    ) {
        super(ASTNodeType.BOOLEAN_BINARY_EXPRESSION);
        this.leftExpression = leftExpression;
        this.operator = operator;
        this.rightExpression = rightExpression;
    }
}

class IdentifierExpressionNode extends ExpressionNode {
    String identifier;
    IdentifierExpressionNode(String identifier) {
        super(ASTNodeType.IDENTIFIER_EXPRESSION);
        this.identifier = identifier;
    }
}

class IdentifierAssignExpressionNode extends ExpressionNode {
    IdentifierExpressionNode identifier;
    ExpressionNode value;
    IdentifierAssignExpressionNode(
        IdentifierExpressionNode identifier, ExpressionNode value
    ) {
        super(ASTNodeType.IDENTIFIER_ASSIGN_EXPRESSION);
        this.identifier = identifier;
        this.value = value;
    }
}

class ArraySubscriptExpressionNode extends ExpressionNode {
    String identifier;
    ExpressionNode subscriptExpr;
    ArraySubscriptExpressionNode(String identifier, ExpressionNode subscriptExpr) {
        super(ASTNodeType.ARRAY_SUBSCRIPT_EXPRESSION);
        this.identifier = identifier;
        this.subscriptExpr = subscriptExpr;
    }
}

class ArraySubscriptAssignExpressionNode extends ExpressionNode {
    ArraySubscriptExpressionNode subscript;
    ExpressionNode value;
    ArraySubscriptAssignExpressionNode(
        ArraySubscriptExpressionNode subscript, ExpressionNode value
    ) {
        super(ASTNodeType.ARRAY_SUBSCRIPT_ASSIGN_EXPRESSION);
        this.subscript = subscript;
        this.value = value;
    }
}

class VarDeclStatementNode extends StatementNode {
    String identifier;
    int size;
    ExpressionNode expression;
    VarDeclStatementNode(
        String identifier,
        int size,
        ExpressionNode expression
    ) {
        super(ASTNodeType.VAR_DECL_STATEMENT);
        this.identifier = identifier;
        this.size = size;
        this.expression = expression;
    }
}

class VarsDeclStatementNode extends StatementNode {
    ArrayList<VarDeclStatementNode> varsDecl = new ArrayList<>();
    VarsDeclStatementNode() {
        super(ASTNodeType.VARS_DECL_STATEMENT);
    }
}

class CompoundStatementNode extends StatementNode {
    ArrayList<StatementNode> stmts = new ArrayList<>();
    CompoundStatementNode() {
        super(ASTNodeType.COMPOUND_STATEMENT);
    }
}

class ExpressionStatementNode extends StatementNode {
    ExpressionNode expression;
    ExpressionStatementNode(ExpressionNode expression) {
        super(ASTNodeType.EXPRESSION_STATEMENT);
    }
}

class WriteStatementNode extends StatementNode {
    ArrayList<ExpressionNode> args = new ArrayList<>();
    WriteStatementNode() {
        super(ASTNodeType.WRITE_STATEMENT);
    }
}

class WritelnStatementNode extends StatementNode {
    ArrayList<ExpressionNode> args = new ArrayList<>();
    WritelnStatementNode() {
        super(ASTNodeType.WRITELN_STATEMENT);
    }
}

abstract class ReadlnStatementNode extends StatementNode {
    ReadlnStatementNode(ASTNodeType type) {
        super(type);
    }
}

class ReadlnVarStatementNode extends ReadlnStatementNode {
    String identifier;
    ReadlnVarStatementNode(String identifier) {
        super(ASTNodeType.READLN_VAR_STATEMENT);
        this.identifier = identifier;
    }
}

class ReadlnArrayStatementNode extends ReadlnStatementNode {
    String identifier;
    ExpressionNode expression;
    ReadlnArrayStatementNode(String identifier, ExpressionNode expression) {
        super(ASTNodeType.READLN_VAR_STATEMENT);
        this.identifier = identifier;
        this.expression = expression;
    }
}

class ReturnStatementNode extends StatementNode {
    ExpressionNode expression;
    ReturnStatementNode(ExpressionNode expression) {
        super(ASTNodeType.RETURN_STATEMENT);
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
        super(ASTNodeType.IF_STATEMENT);
        this.expression = expression;
        this.ifStatement = ifStatement;
        this.elseStatement = elseStatement;
    }
}

class ForStatementNode extends StatementNode {
    ExpressionNode init;
    ExpressionNode condition;
    ExpressionNode inc;
    StatementNode stmt;
    ForStatementNode(
        ExpressionNode init,
        ExpressionNode condition,
        ExpressionNode inc,
        StatementNode stmt
    ) {
        super(ASTNodeType.FOR_STATEMENT);
        this.init = init;
        this.condition = condition;
        this.inc = inc;
        this.stmt = stmt;
    }
}

class ProgramNode extends Node {
    VarsDeclStatementNode varsDecl;
    StatementNode stmt;
    ProgramNode() {
        super(ASTNodeType.PROGRAM);
    }
}

class Parser {
    Lexer lexer;
    Token currentToken;

    Parser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        this.currentToken = lexer.next();
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
        System.out.printf("Ate type = %s, value = '%s'\n",
            currentToken.type.name(), currentToken.value);
        currentToken = lexer.next();
    }

    void eat(TokenType type) throws IOException {
        if (currentToken.type == type) eat();
        else tokenNotExpected();
    }

    ExpressionNode parsePrimaryExpression() throws IOException {
        ExpressionNode node = null;
        switch (currentToken.type) {
            case TRUE:
            case FALSE:
                node = new BooleanExpressionNode(currentToken.type == TokenType.TRUE);
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
            case PLUS:
            case MINUS:
                eat();
                node = new ArithmeticUnaryExpressionNode(operator, parseExpression());
                break;

            case NOT:
                eat();
                node = new BooleanUnaryExpressionNode(operator, parseExpression());
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
        ) {
            String operator = currentToken.value;
            eat();
            node = new ArithmeticBinaryExpressionNode(node, operator, parseUnaryExpression());
        }
        return node;
    }

    ExpressionNode parseAdditiveExpression() throws IOException {
        ExpressionNode node = parseMultiplicativeExpression();
        while (
            currentToken.type == TokenType.PLUS
            || currentToken.type == TokenType.MINUS
        ) {
            String operator = currentToken.value;
            eat();
            node = new ArithmeticBinaryExpressionNode(node, operator, parseMultiplicativeExpression());
        }
        return node;
    }

    ExpressionNode parseRelationalExpression() throws IOException {
        ExpressionNode node = parseAdditiveExpression();
        while (
            currentToken.type == TokenType.SMALLER
            || currentToken.type == TokenType.GREATER
            || currentToken.type == TokenType.SMALLER_OR_EQUAL
            || currentToken.type == TokenType.GREATER_OR_EQUAL
        ) {
            String operator = currentToken.value;
            eat();
            node = new BooleanBinaryExpressionNode(node, operator, parseAdditiveExpression());
        }
        return node;
    }

    ExpressionNode parseEqualityExpression() throws IOException {
        ExpressionNode node = parseRelationalExpression();
        while (
            currentToken.type == TokenType.EQUAL
            || currentToken.type == TokenType.DIFFERENT
        ) {
            String operator = currentToken.value;
            eat();
            node = new BooleanBinaryExpressionNode(node, operator, parseRelationalExpression());
        }
        return node;
    }

    ExpressionNode parseAndExpression() throws IOException {
        ExpressionNode node = parseEqualityExpression();
        while (currentToken.type == TokenType.AND) {
            String operator = currentToken.value;
            eat();
            node = new BooleanBinaryExpressionNode(node, operator, parseEqualityExpression());
        }
        return node;
    }

    ExpressionNode parseOrExpression() throws IOException {
        ExpressionNode node = parseAndExpression();
        while (currentToken.type == TokenType.OR) {
            String operator = currentToken.value;
            eat();
            node = new BooleanBinaryExpressionNode(node, operator, parseAndExpression());
        }
        return node;
    }

    ExpressionNode parseAssignmentExpression() throws IOException {
        ExpressionNode node = parseOrExpression();
        switch (currentToken.type) {
            case ASSIGN:
                eat();
                ExpressionNode value = parseOrExpression();
                if (node.getClass().equals(IdentifierExpressionNode.class)) {
                    IdentifierExpressionNode identifier = (IdentifierExpressionNode) node;
                    node = new IdentifierAssignExpressionNode(identifier, value);
                } else {
                    ArraySubscriptExpressionNode subscript = (ArraySubscriptExpressionNode) node;
                    node = new ArraySubscriptAssignExpressionNode(subscript, value);
                }
                break;
        }
        return node;
    }

    ExpressionNode parseExpression() throws IOException {
        ExpressionNode node = parseAssignmentExpression();
        System.out.println(node.getClass().getName());
        return node;
    }

    // ExpressionNode parseExpression() throws IOException {
    //     ExpressionNode node = null;
    //     switch (currentToken.type) {
    //         case TRUE:
    //         case FALSE:
    //             node = new BooleanExpressionNode(currentToken.type == TokenType.TRUE);
    //             eat();
    //             break;

    //         case CHAR:
    //             node = new CharExpressionNode(currentToken.value.charAt(0));
    //             eat();
    //             break;

    //         case INTEGER:
    //             node = new IntExpressionNode(Integer.parseInt(currentToken.value));
    //             eat();
    //             break;

    //         case HEX_INTEGER:
    //             String hexStr = currentToken.value.substring(1, 3);
    //             node = new IntExpressionNode(Integer.parseInt(hexStr, 16));
    //             eat();
    //             break;

    //         case STRING:
    //             node = new StringExpressionNode(currentToken.value);
    //             eat();
    //             break;

    //         case LEFT_PAREN:
    //             eat();
    //             node = new ParenthesizedExpressionNode(parseExpression());
    //             eat(TokenType.RIGHT_PAREN);
    //             break;

    //         case PLUS:
    //         case MINUS:
    //         case NOT:
    //             eat();
    //             node = new ParenthesizedExpressionNode(parseExpression());
    //             eat(TokenType.RIGHT_PAREN);
    //             break;

    //         case IDENTIFIER:
    //             ExpressionNode subscriptExpr = null, value = null;
    //             String identifier = currentToken.value;
    //             eat();

    //             if (currentToken.type == TokenType.LEFT_BRACKET) {
    //                 eat(TokenType.LEFT_BRACKET);
    //                 subscriptExpr = parseExpression();
    //                 eat(TokenType.RIGHT_BRACKET);
    //             }

    //             if (currentToken.type == TokenType.ASSIGN) {
    //                 eat(TokenType.ASSIGN);
    //                 value = parseExpression();
    //             }

    //             if (subscriptExpr == null && value == null) {
    //                 node = new IdentifierExpressionNode(identifier);
    //             } else if (subscriptExpr == null) {
    //                 node = new IdentifierAssignExpressionNode(identifier, value);
    //             } else if (value == null) {
    //                 node = new ArraySubscriptExpressionNode(identifier, subscriptExpr);
    //             } else {
    //                 node = new ArraySubscriptAssignExpressionNode(identifier, subscriptExpr, value);
    //             }
    //             break;

    //         default: tokenNotExpected(); break;
    //     }
    //     return node;
    // }

    VarDeclStatementNode parseVarDecl() throws IOException {
        String identifier = currentToken.value;
        int size = -1;
        ExpressionNode expression = null;
        eat(TokenType.IDENTIFIER);

        if (currentToken.type == TokenType.LEFT_BRACKET) {
            eat();
            switch (currentToken.type) {
                case INTEGER:
                    size = Integer.parseInt(currentToken.value);
                    eat();
                    break;
    
                case HEX_INTEGER:
                    String hexStr = currentToken.value.substring(1, 3);
                    size = Integer.parseInt(hexStr, 16);
                    eat();
                    break;
    
                default: tokenNotExpected(); break;
            }
            eat(TokenType.RIGHT_BRACKET);
        }

        if (currentToken.type == TokenType.ASSIGN) {
            eat(TokenType.ASSIGN);
            expression = parseExpression();
        }

        return new VarDeclStatementNode(identifier, size, expression);
    }

    VarsDeclStatementNode parseVarsDecl() throws IOException {
        VarsDeclStatementNode node = new VarsDeclStatementNode();
        node.varsDecl.add(parseVarDecl());
        while (currentToken.type == TokenType.COMMA) {
            eat();
            node.varsDecl.add(parseVarDecl());
        }
        System.out.println(node.getClass().getName());
        return node;
    }

    CompoundStatementNode parseCompoundStatement() throws IOException {
        CompoundStatementNode node = new CompoundStatementNode();
        while (currentToken.type != TokenType.RIGHT_BRACES)
            node.stmts.add(parseStatement());
        eat(TokenType.RIGHT_BRACES);
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
        eat(TokenType.SEMICOLON);
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
        eat(TokenType.SEMICOLON);
        return node;
    }

    ReadlnStatementNode parseReadlnStatement() throws IOException {
        ReadlnStatementNode node;
        eat(TokenType.LEFT_PAREN);
        String identifier = currentToken.value;
        eat(TokenType.IDENTIFIER);
        if (currentToken.type == TokenType.LEFT_BRACKET) {
            eat(TokenType.LEFT_BRACKET);
            ExpressionNode expression = parseExpression();
            node = new ReadlnArrayStatementNode(identifier, expression);
            eat(TokenType.RIGHT_BRACKET);
        } else {
            node = new ReadlnVarStatementNode(identifier);
        }
        eat(TokenType.RIGHT_PAREN);
        eat(TokenType.SEMICOLON);
        return node;
    }

    ReturnStatementNode parseReturnStatement() throws IOException {
        ReturnStatementNode node = new ReturnStatementNode(parseExpression());
        eat(TokenType.SEMICOLON);
        return node;
    }

    IfStatementNode parseIfStatement() throws IOException {
        eat(TokenType.LEFT_PAREN);
        ExpressionNode expression = parseExpression();
        eat(TokenType.RIGHT_PAREN);
        eat(TokenType.THEN);
        StatementNode ifStatement = parseStatement();
        StatementNode elseStatement = null;
        if (currentToken.type == TokenType.ELSE) {
            eat();
            elseStatement = parseStatement();
        }
        IfStatementNode node = new IfStatementNode(expression, ifStatement, elseStatement);
        return node;
    }

    ForStatementNode parseForStatement() throws IOException {
        eat(TokenType.LEFT_PAREN);
        ExpressionNode init = null, condition = null, inc = null;

        if (currentToken.type != TokenType.SEMICOLON) init = parseExpression();
        eat(TokenType.SEMICOLON);
        
        if (currentToken.type != TokenType.SEMICOLON) condition = parseExpression();
        eat(TokenType.SEMICOLON);
        
        if (currentToken.type != TokenType.SEMICOLON) inc = parseExpression();

        eat(TokenType.RIGHT_PAREN);
        ForStatementNode node = new ForStatementNode(init, condition, inc, parseStatement());
        return node;
}

    StatementNode parseStatement() throws IOException {
        StatementNode node = null;
        switch (currentToken.type) {
            case LEFT_BRACES:
                eat(TokenType.LEFT_BRACES);
                node = parseCompoundStatement();
                break;

            case WRITE:
                eat(TokenType.WRITE);
                node = parseWriteStatement();
                break;

            case WRITELN:
                eat(TokenType.WRITELN);
                node = parseWritelnStatement();
                break;

            case READLN:
                eat(TokenType.READLN);
                node = parseReadlnStatement();
                break;

            case RETURN:
                eat(TokenType.RETURN);
                node = parseReturnStatement();
                break;

            case INT:
            case CHAR:
            case BOOLEAN:
            case FINAL:
                eat();
                node = parseVarsDecl();
                eat(TokenType.SEMICOLON);
                break;

            case IF:
                eat(TokenType.IF);
                node = parseIfStatement();
                break;

            case FOR:
                eat(TokenType.FOR);
                node = parseForStatement();
                break;

            default:
                node = new ExpressionStatementNode(parseExpression());
                eat(TokenType.SEMICOLON);
                break;
        }
        System.out.println(node.getClass().getName());
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
            eat();
            node.varsDecl = parseVarsDecl();
            eat(TokenType.SEMICOLON);
        }
        eat(TokenType.MAIN);
        node.stmt = parseStatement();
        eat(TokenType.EOF);
        System.out.println(node.getClass().getName());
        return node;
    }
}

