import java.io.IOException;
import java.io.PushbackReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.io.InputStreamReader;

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
    CHAR_CONST,
    HEX_CHAR,
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
                        return new Token(TokenType.HEX_CHAR, str);
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
                    return new Token(TokenType.HEX_CHAR, str);
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
                return new Token(TokenType.HEX_CHAR, str);
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
        return new Token(TokenType.CHAR_CONST, c);
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

    boolean skipComments() throws IOException {
        char c = read();
        if (c == '/') { skipSingleLineComment(); return true; }
        else if (c == '*') { skipMultiLineComment(); return true; }
        else { reader.unread(c); }

        return false;
    }

    char skipSpacesAndComments(char c) throws IOException {
        while (isSpace(c) || c == '/') {
            if (isSpace(c)) {
                if (c == '\n') line++;
                c = skipSpaces();
            }

            if (c == '/') {
                if(skipComments())
                    c = read();
                else break;
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
    int end;
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
        this.end = expression.end;
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

        switch(type){
            case INT: size2 = 2; break;
            case INTEGER: size2 = 2; break;
            case BOOLEAN: size2 = 1; break;
            case BOOLEAN_CONST: size2 = 1; break;
            case CHAR: size2 = 1; break;
            case CHAR_CONST: size2 = 1; break;
            case HEX_CHAR: size2 = 1; break;
            case STRING: size2 = 1; break;
            default:
                // System.out.printf("ERROR: Type not supported: %s, size = %d\n", type.toString(), size);
                break;
        }

        if(size != 0)
            size2 *= size;

        if(type == TokenType.STRING) size2++; // Dólar

        return size2;
    }

    public static boolean isString(TokenType tokenType, int size){
        return tokenType == TokenType.CHAR && size > 0;
    }

    public static boolean isVet(Symbol s){
        return s.symbolClass == SymbolClass.VAR && s.size > 0;
    }

    public static boolean isArithmetic(String operator){
        return operator.equals("+") || operator.equals("-") || operator.equals("*") || operator.equals("/")
            || operator.equals("%");
    }

    public static boolean isRelational(String operator){
        return  operator.equals("=") || operator.equals("<>") || operator.equals("<")
                || operator.equals(">") || operator.equals("<=") || operator.equals(">=");
    }

    public static boolean isLogical(String operator){
        return operator.equals("not") || operator.equals("and") || operator.equals("or");
    }
}

class Parser {
    Lexer lexer;
    Token currentToken;
    Semantic semantic;
    CodeGenerator codegen;

    Parser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        this.currentToken = lexer.next();
        this.semantic = new Semantic(lexer);
        this.codegen = new CodeGenerator();
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
                boolean bvalue = currentToken.value.equals("TRUE");
                node = new BooleanExpressionNode(bvalue);
                node.end = this.codegen.createBoolTemp(bvalue);
                eat();
                break;

            case CHAR_CONST:
                char cvalue = currentToken.value.charAt(0);
                node = new CharExpressionNode(cvalue);
                node.end = this.codegen.createCharTemp(cvalue);
                eat();
                break;

            case HEX_CHAR:
                String hexStr = currentToken.value.substring(1, 3);
                char hcvalue = (char) Integer.parseInt(hexStr, 16);
                node = new CharExpressionNode(hcvalue);
                node.end = this.codegen.createCharTemp(hcvalue);
                eat();
                break;

            case INTEGER:
                int ivalue = Integer.parseInt(currentToken.value);
                node = new IntExpressionNode(ivalue);
                node.end = this.codegen.createIntTemp(ivalue);
                eat();
                break;

            case STRING:
                node = new StringExpressionNode(currentToken.value);
                node.end = this.codegen.createStrTemp(currentToken.value);
                eat();
                break;
        }
        return node;
    }

    ExpressionNode parseSignedConstExpression() throws IOException {
        ExpressionNode node = null;
        String operator = currentToken.value;
        switch (currentToken.type) {
            case PLUS:
                eat();
                node = parseConstExpression();
                //Semantic Action
                this.semantic.verifyUnaryOperator(
                    operator,
                    this.semantic.getExpressionType(node)
                );
                break;
            case MINUS:
                eat();
                node = parseConstExpression();
                //Semantic Action
                this.semantic.verifyUnaryOperator(
                    operator,
                    this.semantic.getExpressionType(node)
                );
                ((IntExpressionNode) node).value *= -1;
                break;
        }
        return node == null ? parseConstExpression() : node;
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
                    node.end = this.semantic.getDeclaredSymbol(identifier).address;
                } else {
                    node = new ArraySubscriptExpressionNode(identifier, subscriptExpr);
                    //Comentado por enquanto, não tem mensagem no tp pra isso
                    //this.semantic.verifyVetIndex(semantic.getDeclaredSymbol(identifier), subscriptExpr); //Semantic Action

                    node.end = this.codegen.getArrayElement(
                        this.semantic.getDeclaredSymbol(identifier),
                        subscriptExpr.end
                    );
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
                ExpressionNode notOperand = parseUnaryExpression();
                node = new UnaryExpressionNode(operator, notOperand);
                node.end = this.codegen.negate(notOperand.end);
                //Semantic Action
                this.semantic.verifyUnaryOperator(
                    operator,
                    this.semantic.getExpressionType(notOperand)
                );
                break;
            case PLUS:
                eat();
                ExpressionNode plusOperand = parseUnaryExpression();
                node = new UnaryExpressionNode(operator, plusOperand);
                node.end = plusOperand.end;
                //Semantic Action
                this.semantic.verifyUnaryOperator(
                    operator,
                    this.semantic.getExpressionType(plusOperand)
                );
                break;
            case MINUS:
                eat();
                ExpressionNode minusOperand = parseUnaryExpression();
                node = new UnaryExpressionNode(operator, minusOperand);
                node.end = this.codegen.unaryMinus(minusOperand.end);
                //Semantic Action
                this.semantic.verifyUnaryOperator(
                    operator,
                    this.semantic.getExpressionType(minusOperand)
                );
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
            BinaryExpressionNode binaryNode = (BinaryExpressionNode) node;

            this.semantic.verifyTypeCompability((BinaryExpressionNode)node); //Semantic ACtion

            node.end = this.codegen.doMultiplicativeExpression(
                operator, binaryNode.leftExpression.end, binaryNode.rightExpression.end
            );
            
        }
        return node;
    }

    ExpressionNode parseAdditiveExpression() throws IOException {
        ExpressionNode node = parseMultiplicativeExpression();
        while (
                currentToken.type == TokenType.PLUS
                        || currentToken.type == TokenType.MINUS
                        || currentToken.type == TokenType.OR
        ) {
            String operator = currentToken.value;
            eat();

            node = new BinaryExpressionNode(node, operator, parseMultiplicativeExpression());
            BinaryExpressionNode binaryNode = (BinaryExpressionNode) node;

            this.semantic.verifyTypeCompability(binaryNode); //Semantic Action

            node.end = this.codegen.doAdditiveExpression(
                operator, binaryNode.leftExpression.end, binaryNode.rightExpression.end
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
            if(node instanceof IdentifierExpressionNode){
                //Semantic Action
                if(ParserUtils.isVet(semantic.getDeclaredSymbol(((IdentifierExpressionNode) node).identifier))){
                    SemanticErros.incompatibleTypes(lexer.line);
                }
            }

            String operator = currentToken.value;
            eat();

            node = new BinaryExpressionNode(node, operator, parseAdditiveExpression());

            BinaryExpressionNode binaryNode = (BinaryExpressionNode) node;
            this.semantic.verifyTypeCompability(binaryNode); //Semantic Action

            node.end = this.codegen.doRelationalExpression(
                operator, binaryNode.leftExpression.end, binaryNode.rightExpression.end
            );
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

        if (currentToken.type == TokenType.LEFT_BRACKET) {
            eat();
            size = parseConstExpression();
            if (size == null) tokenNotExpected();
            if (!size.getClass().equals(IntExpressionNode.class))
                SemanticErros.incompatibleTypes(lexer.line);

            IntExpressionNode intNode = (IntExpressionNode)size;
            vetSize = intNode.value;

            //Semantic Action
            this.semantic.verifyVetSize(ParserUtils.getTypeSize(tokenType, vetSize));

            eat(TokenType.RIGHT_BRACKET);
        }

        else if (currentToken.type == TokenType.ASSIGN) {
            eat();
            TokenType currentTokenType = currentToken.type;

            value = parseSignedConstExpression();
            if (value == null) tokenNotExpected();

            //Semantic Action
            this.semantic.verifyTypeCompatibility(tokenType, this.semantic.getExpressionType(value));
        }

        //Semantic Action
        Symbol s = new Symbol(identifier, SymbolClass.VAR, tokenType, value, vetSize);
        this.semantic.addSymbol(s);
        this.codegen.declSymbol(s);

        return new VarDeclStatementNode(identifier, size, value);
    }

    VarDeclsStatementNode parseVarDecls(TokenType tokenType) throws IOException {
        VarDeclsStatementNode node = new VarDeclsStatementNode();
        node.varsDecl.add(parseVarDecl(tokenType));
        while (currentToken.type == TokenType.COMMA) {
            eat();
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

        value = parseSignedConstExpression();

        if (value == null) tokenNotExpected();

        Symbol s = new Symbol(
            identifier, SymbolClass.CONST, this.semantic.getExpressionType(value), value, 0
        );
        this.semantic.addSymbol(s); //Semantic Action
        this.codegen.declSymbol(s);

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
        ExpressionNode expr = parseExpression();
        node.args.add(expr);
        this.codegen.writeExpression(expr.end, this.semantic.getExpressionType(expr));
        while (currentToken.type == TokenType.COMMA) {
            eat(TokenType.COMMA);
            expr = parseExpression();
            node.args.add(expr);
            this.codegen.writeExpression(expr.end, this.semantic.getExpressionType(expr));
        }
        eat(TokenType.RIGHT_PAREN);
        return node;
    }

    WritelnStatementNode parseWritelnStatement() throws IOException {
        WritelnStatementNode node = new WritelnStatementNode();
        eat(TokenType.LEFT_PAREN);

        ExpressionNode expr = parseExpression();
        node.args.add(expr);
        this.codegen.writeExpression(expr.end, this.semantic.getExpressionType(expr));

        while (currentToken.type == TokenType.COMMA) {
            eat(TokenType.COMMA);
            expr = parseExpression();
            node.args.add(expr);
            this.codegen.writeExpression(expr.end, this.semantic.getExpressionType(expr));
        }

        this.codegen.write(this.codegen.newLineTemp);
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

        if(this.semantic.getExpressionType(expression) != TokenType.BOOLEAN){
            SemanticErros.incompatibleTypes(lexer.line);
        }

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
        Symbol s = this.semantic.getDeclaredSymbol(identifier);

        eat();

        if (currentToken.type == TokenType.LEFT_BRACKET) {
            eat();
            subscriptExpr = parseExpression();
            eat(TokenType.RIGHT_BRACKET);
        }

        eat(TokenType.ASSIGN);

        if (subscriptExpr == null) {
            node = new IdentifierAssignStatementNode(identifier, parseExpression());

            if(s.type == TokenType.STRING && this.semantic.getExpressionType(node.value) != TokenType.STRING){
                SemanticErros.incompatibleTypes(lexer.line);
            }
            if(s.type != TokenType.STRING && ParserUtils.isVet(s)){
                SemanticErros.incompatibleTypes(lexer.line);
            }

        } else {
            node = new ArraySubscriptAssignStatementNode(
                    identifier, subscriptExpr, parseExpression()
            );
        }

        //Semantic Action
        this.semantic.verifyTypeCompability(identifier, semantic.getExpressionType(node.value));
        this.semantic.verifyClassCompatibility(identifier);

        Symbol identifierSymbol = s;
        int idAddr = identifierSymbol.address;
        TokenType idType = identifierSymbol.type;

        if(subscriptExpr == null) {
            if(identifierSymbol.type == TokenType.CHAR) {
                this.codegen.doStringAssignStatement(
                    idAddr, node.value.end, idType, identifierSymbol
                );
            }
            else {
                this.codegen.doAssignStatement(
                    idAddr, node.value.end, idType
                );
            }
        }
        else {
            int idIndexAddr = subscriptExpr.end;
            int exprAddr = node.value.end;
            this.codegen.doArrayAssignStatement(
                idAddr, exprAddr, idType, idIndexAddr
            );
        }
        

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

        if(this.semantic.getExpressionType(condition) != TokenType.BOOLEAN){
            SemanticErros.incompatibleTypes(lexer.line);
        }

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

        // System.out.println(this.semantic.symTable);

        return node;
    }
}

/* Semantico Area */

class Semantic{
    public SymTable symTable;
    public Lexer lexer;

    public Semantic(Lexer lexer){
        this.symTable = new SymTable();
        this.lexer = lexer;
    }

    public void addSymbol(Symbol s) throws IOException{
        Symbol symbol = symTable.getSymbol(s.getName());

        if(symbol == null){
            if(ParserUtils.isString(s.type, s.size))
                s.type = TokenType.STRING;

            symTable.addSymbol(s);
        }
        else{
            SemanticErros.declaredVariable(s.getName(), lexer.line);
        }
    }

    public Symbol getDeclaredSymbol(String symbolName){
        Symbol symbol = symTable.getSymbol(symbolName);

        if(symbol == null){
            SemanticErros.undeclaredVariable(symbolName, lexer.line);
        }

        return symbol;
    }

    public void verifyVetSize(int size){
        if(size < 1 || size > 8000){
            SemanticErros.vetOverflow(lexer.line);
        }
    }

    public void verifyVetIndex(Symbol s, ExpressionNode node){
        if(!(node instanceof IntExpressionNode)){
            SemanticErros.incompatibleTypes(lexer.line); //Semantic Action
        }else{
            int index = ((IntExpressionNode)node).value;
            if(index < 0 || index > s.size){
                SemanticErros.incompatibleTypes(lexer.line);
            }
        }
    }

    public void verifyClassCompatibility(String symbolName){
        Symbol symbol = getDeclaredSymbol(symbolName);

        if(symbol.symbolClass == SymbolClass.CONST){
            SemanticErros.changeConst(symbolName, lexer.line);
        }
    }

    public TokenType getExpressionType(ExpressionNode node){
        TokenType tokenTypeLeft = null;
        TokenType tokenTypeRight = null;
        TokenType tokenType = null;

        if(node instanceof ParenthesizedExpressionNode){
            tokenType = getExpressionType(((ParenthesizedExpressionNode) node).expression);
        }else if(node instanceof UnaryExpressionNode){
            tokenType = getExpressionType(((UnaryExpressionNode) node).expression);
            verifyUnaryOperator(((UnaryExpressionNode) node).operator, tokenType);
        }else if(node instanceof BinaryExpressionNode){
            tokenTypeLeft = getExpressionType(((BinaryExpressionNode) node).leftExpression);
            tokenTypeRight = getExpressionType(((BinaryExpressionNode) node).rightExpression);

            String operator = ((BinaryExpressionNode) node).operator;
            if(ParserUtils.isRelational(operator)){
                tokenType = TokenType.BOOLEAN;
            }
            else if(tokenTypeLeft != null && tokenTypeRight != null) {
                tokenType = getNodeType(tokenTypeLeft,tokenTypeRight);
            }

        }else if(node instanceof ArraySubscriptExpressionNode){
            tokenType = getExpressionType(((ArraySubscriptExpressionNode) node).subscriptExpr);

            if(tokenType != TokenType.INT)
                SemanticErros.incompatibleTypes(lexer.line);

            tokenType = getDeclaredSymbol(((ArraySubscriptExpressionNode) node).identifier).type;
        }else if(node instanceof IdentifierExpressionNode){
            Symbol s = getDeclaredSymbol(((IdentifierExpressionNode) node).identifier);

            return s.type;
        }

        if(node instanceof IntExpressionNode){
            tokenType = TokenType.INT;
        }else if(node instanceof BooleanExpressionNode){
            tokenType = TokenType.BOOLEAN;
        }else if(node instanceof CharExpressionNode){
            tokenType = TokenType.CHAR;
        }else if(node instanceof StringExpressionNode){
            tokenType = TokenType.STRING;
        }else if(node instanceof IdentifierExpressionNode){
            Symbol s = getDeclaredSymbol(((IdentifierExpressionNode)node).identifier);

            tokenType = s.type;
        }

        return tokenType;
    }

    public void verifyTypeCompability(String identifier, TokenType tokenTypeRight){
        Symbol s = getDeclaredSymbol(identifier);

        verifyTypeCompatibility(s.type, tokenTypeRight);
    }

    public void verifyTypeCompability(BinaryExpressionNode node){
        TokenType tokenTypeLeft = getExpressionType(node.leftExpression);
        TokenType tokenTypeRight = getExpressionType(node.rightExpression);

        verifyOperatorCompatibility(node.operator, tokenTypeLeft, tokenTypeRight);
        verifyTypeCompatibility(tokenTypeLeft, tokenTypeRight);

        if(tokenTypeLeft != tokenTypeRight){
            SemanticErros.incompatibleTypes(lexer.line);
        }

    }

    public void verifyTypeCompatibility(TokenType tokenTypeLeft, TokenType tokenTypeRight){
        if(tokenTypeLeft == TokenType.INT && (tokenTypeRight != TokenType.INTEGER
                && tokenTypeRight != TokenType.INT))
        {
            SemanticErros.incompatibleTypes(lexer.line);
        }else if(tokenTypeLeft == TokenType.CHAR && tokenTypeRight != TokenType.CHAR)
        {
            SemanticErros.incompatibleTypes(lexer.line);
        }else if(tokenTypeLeft == TokenType.STRING
                && (tokenTypeRight != TokenType.CHAR && tokenTypeRight != TokenType.STRING))
        {
            SemanticErros.incompatibleTypes(lexer.line);
        }else if(tokenTypeLeft == TokenType.BOOLEAN && (tokenTypeRight != TokenType.BOOLEAN
                && tokenTypeRight != TokenType.BOOLEAN_CONST))
        {
            SemanticErros.incompatibleTypes(lexer.line);
        }
    }

    public void verifyOperatorCompatibility(String operator, TokenType tokenTypeLeft, TokenType tokenTypeRight){
        if(operator != null){
            if(ParserUtils.isArithmetic(operator) && tokenTypeRight != TokenType.INT)
            {
                SemanticErros.incompatibleTypes(lexer.line);
            }
            if(tokenTypeLeft == TokenType.STRING && operator.equals("=") && (tokenTypeRight != TokenType.STRING))
            {
                SemanticErros.incompatibleTypes(lexer.line);
            }
            if(ParserUtils.isLogical(operator) && tokenTypeRight != TokenType.BOOLEAN)
            {
                SemanticErros.incompatibleTypes(lexer.line);
            }
            if(ParserUtils.isRelational(operator) && (tokenTypeLeft != tokenTypeRight
                    || (tokenTypeLeft == TokenType.BOOLEAN || tokenTypeRight == TokenType.BOOLEAN)) )
            {
                SemanticErros.incompatibleTypes(lexer.line);
            }
        }
    }

    public void verifyUnaryOperator(String operator, TokenType tokenTypeRight){
        if(operator.equals("not") && tokenTypeRight != TokenType.BOOLEAN)
        {
            SemanticErros.incompatibleTypes(lexer.line);
        }
        if((operator.equals("+") || operator.equals("-")) && (tokenTypeRight != TokenType.INT))
        {
            SemanticErros.incompatibleTypes(lexer.line);
        }
    }

    private TokenType getNodeType(TokenType tokenTypeLeft, TokenType tokenTypeRight){
        if(tokenTypeLeft == TokenType.INT && (tokenTypeRight == TokenType.INTEGER
                || tokenTypeRight == TokenType.INT)){
            return TokenType.INT;
        }else if(tokenTypeLeft == TokenType.CHAR && (tokenTypeRight == TokenType.CHAR
                || tokenTypeRight == TokenType.HEX_CHAR || tokenTypeRight == TokenType.STRING)){
            return TokenType.CHAR;
        }else if(tokenTypeLeft == TokenType.BOOLEAN && (tokenTypeRight == TokenType.BOOLEAN
                || tokenTypeRight == TokenType.BOOLEAN_CONST)){
            return TokenType.BOOLEAN;
        }else if(tokenTypeLeft == TokenType.IDENTIFIER){
            return tokenTypeRight;
        }

        SemanticErros.incompatibleTypes(lexer.line);
        return null;
    }


    private boolean containsSymbol(String symbolName){
        Symbol symbol = symTable.getSymbol(symbolName);

        return symbol != null;
    }
}

class SemanticErros{
    public static void declaredVariable(String variableName, int line){
        System.out.println(String.format("%d\nidentificador ja declarado [%s].", line, variableName));
        System.exit(0);
    }

    public static void undeclaredVariable(String variableName, int line){
        System.out.println(String.format("%d\nidentificador nao declarado [%s].", line, variableName));
        System.exit(0);
    }

    public static void vetOverflow(int line){
        System.out.println(String.format("%d\ntamanho do vetor excede o maximo permitido.", line));
        System.exit(0);
    }

    public static void changeConst(String symbolName, int line){
        System.out.println(String.format("%d\nclasse de identificador incompativel [%s].", line, symbolName));
        System.exit(0);
    }

    public static void incompatibleTypes(int line){
        System.out.println(String.format("%d\ntipos incompativeis.", line));
        System.exit(0);
    }
}

class Symbol{
    public String name;
    public SymbolClass symbolClass;
    public TokenType type;
    public int size;
    public ExpressionNode value;
    public int address;

    public Symbol(
        String name,
        SymbolClass symbolClass,
        TokenType type,
        ExpressionNode value,
        int size
    ){
        this.name = name;
        this.symbolClass = symbolClass;
        this.type = type;
        this.value = value;
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
        str += String.format("%-15s  %-12s  %-8s  %-5s  %-7s\n",
            "NAME", "SYMBOL CLASS", "TYPE", "SIZE", "ADDRESS");

        for (Entry<String, Symbol> entry : symTable.entrySet()) {
            Symbol s = entry.getValue();
            str+= String.format("%-15s  %-12s  %-8s  %-5d  %-7d\n",
                    s.getName(), s.symbolClass, s.type, s.size, s.address);
        }

        return str;
    }
}

class CodeGenerator {
    List<String> dataSection = new ArrayList<>();
    List<String> codeSection = new ArrayList<>();
    int address = 16384; // Next variable address relatively to DS register
    int temp = 0; // Adress of the last temporary value relatively to DS register
    int newLineTemp = -1;

    public CodeGenerator() {
        newLineTemp = address;
        addData("db 13, 10, '$'");
        address += 3;
    }

    private void addData(String line) {
        dataSection.add("    " + line);
    }

    private void addCode(String line) {
        codeSection.add("    " + line);
    }

    public int createBoolTemp(boolean value) {
        int addr = temp;
        addCode(String.format("createBoolTemp %d %d", value ? 1 : 0, addr));
        temp += 1;
        return addr;
    }

    public int createCharTemp(char value) {
        int addr = temp;
        if (value >= ' ' && value <= '~')
            addCode(String.format("createCharTemp '%c' %d", value, addr));
        else
            addCode(String.format("createCharTemp %d %d", (int) value, addr));
        temp += 1;
        return addr;
    }

    public int createIntTemp(int value) {
        int addr = temp;
        addCode(String.format("createIntTemp %d, %d", value, addr));
        temp += 2;
        return addr;
    }

    public int createStrTemp(String value) {
        int addr = address;
        addData(String.format("db \"%s\", '$'", value));
        address += value.length() + 1;
        return addr;
    }

    public int getArrayElement(Symbol arr, int subscriptExprEnd) {
        int addr = temp;
        if (arr.type == TokenType.INT) {
            addCode(String.format("getIntArrayElement %d, %d, %d", arr.address, subscriptExprEnd, addr));
            temp += 2;
        } else {
            addCode(String.format("getNonIntArrayElement %d, %d, %d", arr.address, subscriptExprEnd, addr));
            temp += 1;
        }
        return addr;
    }

    public int negate(int valuePtr) {
        int addr = temp;
        addCode(String.format("negate %d %d", valuePtr, addr));
        temp++;
        return addr;
    }

    public int unaryMinus(int valuePtr) {
        int addr = temp;
        addCode(String.format("unaryMinus %d %d", valuePtr, addr));
        temp += 2;
        return addr;
    }

    public int doAdditiveExpression(String operator, int op1Addr, int op2Addr) {
        int addr = temp;
        switch (operator) {
            case "+":
                addCode(String.format("sum %d %d %d", op1Addr, op2Addr, addr));
                temp += 2;
                break;

            case "-":
                addCode(String.format("subtract %d %d %d", op1Addr, op2Addr, addr));
                temp += 2;
                break;
    
            case "or":
                temp += 1;
                break;
        }
        return addr;
    }

    public int doMultiplicativeExpression(String operator, int op1Addr, int op2Addr) {
        int addr = temp; // '*' | '/' | '%' | 'and'

        switch (operator) {
            case "*":
                addCode(String.format("multiply %d %d %d", op1Addr, op2Addr, addr));
                temp += 2;
                break;

            case "/":
                addCode(String.format("divide %d %d %d", op1Addr, op2Addr, addr));
                temp += 2;
                break;

            case "%":
                addCode(String.format("mod %d %d %d", op1Addr, op2Addr, addr));
                temp += 2;
                break;

            case "and":
                addCode(String.format("land %d %d %d", op1Addr, op2Addr, addr));
                temp += 1;
                break;
        }

        return addr;
    }

    public int doRelationalExpression(String operator, int op1Addr, int op2Addr) {
        int addr = temp;
        switch (operator) {
            case "=":
                addCode(String.format("relEquals %d %d %d", op1Addr, op2Addr, addr));
                temp += 1;
                break;
            case "<>":
                addCode(String.format("relNotEquals %d %d %d", op1Addr, op2Addr, addr));
                temp += 1;
                break;
            case "<":
                addCode(String.format("relLessThan %d %d %d", op1Addr, op2Addr, addr));
                temp += 1;
                break;
            case ">=":
                addCode(String.format("relGreaterThanOrEqualTo %d %d %d", op1Addr, op2Addr, addr));
                temp += 1;
                break;
            case ">":
                addCode(String.format("relGreaterThan %d %d %d", op1Addr, op2Addr, addr));
                temp += 1;
                break;
            case "<=":
                addCode(String.format("relGreaterThanOrEqualTo %d %d %d", op1Addr, op2Addr, addr));
                temp += 1;
                break;
        }
        return addr;
    }

    public int doStringAssignStatement(
        int idAddr, int exprAddr, TokenType idType, Symbol identifierSymbol
    ) {
        int addr = temp;
        int idSize = identifierSymbol.size;

        addCode(String.format("assignStringVar %d %d %d", idAddr, exprAddr, idSize));
        
        return addr;
    }

    public int doAssignStatement(int idAddr, int exprAddr, TokenType idType) {
        int addr = temp;
        
        if(idType == TokenType.CHAR) 
            addCode(String.format("assignStringVar %d %d", idAddr, exprAddr));
        else
            addCode(String.format("assignVar %d %d", idAddr, exprAddr));

        
        addr = (idType == TokenType.INTEGER)? addr+2 : addr+1;

        return addr;
    }

    public int doArrayAssignStatement(int idAddr, int exprAddr, TokenType idType, int idIndexAddr) {
        int addr = temp;

        if(idType == TokenType.INT) {
            addCode(String.format("assignArray %d %d %d %d", idAddr, exprAddr, 2, idIndexAddr));
            addr += 2;
        }

        else {
            addCode(String.format("assignArray %d %d %d %d", idAddr, exprAddr, 1, idIndexAddr));
            addr += 1;
        }

        return addr;
    }

    public void write(int addr) {
        addCode(String.format("print %d", addr));
    }

    private int charToStr(int addr) {
        int strAddr = createStrTemp("0");
        addCode(String.format("charToStr %d %d", addr, strAddr));
        return strAddr;
    }

    private int boolToStr(int addr) {
        int strAddr = createStrTemp("0");
        addCode(String.format("boolToStr %d %d", addr, strAddr));
        return strAddr;
    }

    private int intToStr(int addr) {
        int strAddr = createStrTemp("-00000");
        addCode(String.format("intToStr %d %d", addr, strAddr));
        return strAddr;
    }

    public void writeExpression(int addr, TokenType type) {
        switch (type) {
            case STRING: write(addr); break;
            case CHAR: write(charToStr(addr)); break;
            case BOOLEAN: write(boolToStr(addr)); break;
            case INT: write(intToStr(addr)); break;
            default:
                // System.err.println("WRITE RECEBEU EXPRESSAO COM TIPO ERRADO");
                break;
        }
    }

    public void declSymbol(Symbol s) {
        int size = ParserUtils.getTypeSize(s.type, s.size);
        s.address = address;
        address += size;
        if (s.size > 0) {
            if (s.type == TokenType.INT)
                addData(String.format("%s dw %d DUP(?)", s.name, s.size));
            else
                addData(String.format("%s db %d DUP(?)", s.name, s.size));
        }
        else declNonVetSymbol(s);
    }

    private void declNonVetSymbol(Symbol s) {
        if (s.type == TokenType.INT) {
            IntExpressionNode expr = (IntExpressionNode) s.value;
            if (expr == null)
                addData(String.format("%s dw %d", s.name, 0));
            else {
                int value = expr.value;
                addData(String.format("%s dw %d", s.name, value));
            }
        }
        else if (s.type == TokenType.CHAR) {
            CharExpressionNode expr = (CharExpressionNode) s.value;
            if (expr == null)
                addData(String.format("%s db %d", s.name, 0));
            else {
                char value = expr.value;
                if (value >= ' ' && value <= '~')
                    addData(String.format("%s db '%c'", s.name, value));
                else
                    addData(String.format("%s db %d", s.name, (int) value));
            }
        }
        else if (s.type == TokenType.BOOLEAN) {
            BooleanExpressionNode expr = (BooleanExpressionNode) s.value;
            if (expr == null)
                addData(String.format("%s db %d", s.name, 0));
            else {
                boolean value = expr.value;
                addData(String.format("%s db %d", s.name, value ? 1 : 0));
            }
        }
    }

    @Override
    public String toString() {
        String str = null;
        try {
            str = String.format(
                new String(Files.readAllBytes(Paths.get("template.asm"))),
                String.join("\r\n", dataSection),
                String.join("\r\n", codeSection)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }
}

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

        PushbackReader input = new PushbackReader(new InputStreamReader(System.in), 3);
        Lexer lexer = new Lexer(input);
        // Token tok = lexer.next();
        // while (tok.type != TokenType.EOF) {
        //     // System.out.printf("id = %s, lexem = '%s'\n", tok.type.name(), tok.value);
        //     tok = lexer.next();
        // }
        Parser parser = new Parser(lexer);
        ProgramNode node = parser.parseProgram();
        System.out.println(parser.codegen);
        // System.out.printf(new CodeGenerator().generate(node));
        // System.out.printf("%d linhas compiladas.\n", lexer.line);
    }
}
