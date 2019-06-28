package ParserPackage;

import ParserPackage.ASTNodes.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


public class ASTBuilder {
    private TokenHolder tokenHolder;
    private Environment environment;
    private HashMap<String, Integer> precedence;
    private int functionPrecedence;
    private boolean resolveImport;
    private boolean executeImmediately;

    public Node build(TokenHolder tokens, HashMap<String, Integer> precedence, int functionPrecedence) throws Exception {
        return build(tokens, precedence, functionPrecedence, false);
    }

    public Node build(TokenHolder tokens, HashMap<String, Integer> precedence, int functionPrecedence, boolean resolveImport) throws Exception {
        return build(tokens, precedence, functionPrecedence, resolveImport, false, null);
    }

    private void init(TokenHolder tokens, HashMap<String, Integer> precedence, int functionPrecedence,
                      boolean resolveImport, boolean executeImmediately, Environment environment) {
        this.precedence = precedence;
        this.functionPrecedence = functionPrecedence;
        this.resolveImport = resolveImport;
        this.environment = environment;
        this.executeImmediately = executeImmediately;
        tokenHolder = tokens;
    }

    public Node build(TokenHolder tokens, HashMap<String, Integer> precedence, int functionPrecedence,
                      boolean resolveImport, boolean executeImmediately, Environment environment) throws Exception {
        init(tokens, precedence, functionPrecedence, resolveImport, executeImmediately, environment);
        if (!this.executeImmediately) {
            ProgramNode node = new ProgramNode();
            while (tokenHolder.hasNext()) {
                Node expression = parseExpression();
                node.addNode(expression);
                checkAndSkip("SEMICOLON");
            }
            return node;
        } else {
            Value returnValue = new Value(null);
            Value exports = Evaluator.EXPORTS;
            Evaluator.EXPORTS = new Value();
            while (tokenHolder.hasNext()) {
                Node expression = parseExpression();
                Evaluator.evaluate(expression, environment);
                checkAndSkip("SEMICOLON");
            }
            returnValue.setProperties(Evaluator.EXPORTS.getProperties());
            Evaluator.EXPORTS = exports;
            return new ValueNode(returnValue);
        }
    }

    private Token checkAndSkip(String type) throws Exception {
        return checkToken(type) ? skipToken(type) : null;
    }

    private boolean checkToken(String type) {
        Token token;
        try {
            token = tokenHolder.lookUp();
        } catch (Exception e) {
            return false;
        }
        return token != null && token.getName().equals(type);
    }

    private Token skipToken(String type) throws Exception {
        if (checkToken(type)) {
            return tokenHolder.next();
        } else throw new Exception(type + " expected, got " + tokenHolder.lookUp().getName());
    }

    private <T> Collection<T> delimited(String start, String separator, String stop, Supplier<T> parser) throws Exception {
        return delimited(start, separator, stop, parser, false);
    }

    private <T> Collection<T> delimited(String start, String separator, String stop, Supplier<T> parser, boolean separatorOptional) throws Exception {
        Collection<T> nodes = new Collection<>();
        boolean first = true;
        if (start != null) skipToken(start);
        while (tokenHolder.hasNext()) {
            if (stop != null) {
                if (checkToken(stop)) break;
                if (first) {
                    first = false;
                } else {
                    if (separator != null) {
                        if (separatorOptional) {
                            checkAndSkip(separator);
                        } else skipToken(separator);
                    }
                }
                if (checkToken(stop)) break;
                nodes.add(parser.get());
            } else if (separator != null) {
                if (first) {
                    first = false;
                } else {
                    if (checkAndSkip(separator) == null) return nodes;
                }
                nodes.add(parser.get());
            }
        }
        if (stop != null) skipToken(stop);
        return nodes;
    }

    private FunctionNode parseFunction() throws Exception {
        return parseFunction(true);
    }

    private FunctionNode parseFunction(boolean identifierPossible) throws Exception {
        FunctionNode node = new FunctionNode();
        if (identifierPossible && checkToken("IDENTIFIER")) {
            node.setName(parseIdentifier());
        }
        node.setParameters(parseParameters());
        node.setBody(checkToken("LEFT_CURLY_PAREN") ? parseBody(true) : parseExpression());
        return node;
    }

    private BodyNode parseBody() throws Exception {
        return parseBody(false);
    }

    private BodyNode parseBody(boolean functionBody) throws Exception {
        BodyNode node;
        if (functionBody) {
            node = new FunctionBodyNode();
        } else {
            node = new BodyNode();
        }
        node.setExpressions(delimited("LEFT_CURLY_PAREN", null, "RIGHT_CURLY_PAREN", () -> {
            try {
                return parseExpression();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }, true));
        return node;
    }

    private UnaryNode parseUnary() throws Exception {
        UnaryNode unaryNode = new UnaryNode();
        unaryNode.setOperator(skipToken("OPERATOR").getValue());
        unaryNode.setValue(parseAtom());
        return unaryNode;
    }

    private NewNode parseNew() throws Exception {
        skipToken("NEW");
        NewNode newNode = new NewNode();
        newNode.setClazz(parseNoCallAtom());
        newNode.setArguments(parseArguments());
        return newNode;
    }

    private Collection<ParameterNode> parseParameters() throws Exception {
        return delimited("LEFT_PAREN", "COMMA", "RIGHT_PAREN", () -> {
            try {
                return parseParameter();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).to(ParameterNode.class);
    }

    private ReturnNode parseReturn() throws Exception {
        skipToken("RETURN");
        return new ReturnNode(parseExpression());
    }

    private ParameterNode parseParameter() throws Exception {
        ParameterNode node = new ParameterNode();
        node.setExpanded(checkAndSkip("EXPAND") != null);
        node.setReference(checkAndSkip("REFERENCE") != null);
        node.setName(parseIdentifier());
        if (checkToken("OPERATOR") && tokenHolder.lookUp().getValue().equals("=")) {
            skipToken("OPERATOR");
            node.setDefaultValue(parseExpression());
        }
        return node;
    }

    private ImportNode parseImport() throws Exception {
        skipToken("IMPORT");
        ImportNode node = new ImportNode();
        if (checkAndSkip("EVERYTHING") == null) {
            for (Map.Entry<String, String> entry : parseImports()) {
                node.put(entry.getKey(), entry.getValue());
            }
        }
        skipToken("FROM");
        node.setBuilt(checkAndSkip("BUILT") != null);
        node.setNative(checkAndSkip("NATIVE") != null);
        node.setFilename(parseExpression());
        if (resolveImport) {
            if (node.getFilename().getType().equals("value")) {
                if (!node.isBuilt()) {
                    TokenHolder tokenHolder = this.tokenHolder;
                    HashMap<String, Integer> precedence = this.precedence;
                    int functionPrecedence = this.functionPrecedence;
                    boolean resolveImport = this.resolveImport;
                    Parser.compile(((ValueNode) node.getFilename()).getValue().toString());
                    node.setBuilt(true);
                    node.setFilename(new ValueNode(new Value(((ValueNode) node.getFilename()).getValue().toString() + ".build")));
                    this.tokenHolder = tokenHolder;
                    this.precedence = precedence;
                    this.functionPrecedence = functionPrecedence;
                    this.resolveImport = resolveImport;
                }
            } else {
                System.out.println("WARNING! Non-buildable library used while compiling");
            }
        }
        return node;
    }

    private Collection<Map.Entry<String, String>> parseImports() throws Exception {
        return delimited(null, "COMMA", null, () -> {
            try {
                String name = parseIdentifier();
                String[] pseudonym = new String[]{null};
                if (checkAndSkip("AS") != null) pseudonym[0] = parseIdentifier();
                return new Map.Entry<String, String>() {
                    @Override
                    public String getKey() {
                        return name;
                    }

                    @Override
                    public String getValue() {
                        return pseudonym[0] == null ? name : pseudonym[0];
                    }

                    @Override
                    public String setValue(String value) {
                        return pseudonym[0] == null ? name : pseudonym[0];
                    }
                };
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private Node parseExpression() throws Exception {
        return maybeSomething(parseAtom(), 0);
    }

    private TryNode parseTry() throws Exception {
        TryNode tryNode = new TryNode();
        skipToken("TRY");
        tryNode.setToTry(checkToken("LEFT_CURLY_PAREN") ? parseBody() : parseExpression());
        if (checkAndSkip("CATCH") != null) tryNode.setToCatch(parseFunction(false));
        if (checkAndSkip("FINALLY") != null)
            tryNode.setElseFinally(checkToken("LEFT_CURLY_PAREN") ? parseBody() : parseExpression());

        return tryNode;
    }

    private ThrowNode parseThrow() throws Exception {
        skipToken("THROW");
        return new ThrowNode(parseExpression());
    }

    private ValueNode parseNumber() throws Exception {
        Token token = skipToken("NUMBER");
        return new ValueNode(
                new Value(
                        token.getValue().contains(".") ? Double.parseDouble(token.getValue()) : Integer.parseInt(token.getValue())
                )
        );
    }

    private Node maybeCallOrIndex(Supplier<Node> parser) throws Exception {
        Node expr = parser.get();
        while (true) {
            if (checkToken("LEFT_PAREN")) {
                expr = parseCall(expr);
            } else if (checkToken("LEFT_SQUARE_PAREN")) {
                expr = parseIndex(expr);
            } else {
                return expr;
            }
        }
    }

    private IndexNode parseIndex(Node value) throws Exception {
        IndexNode indexNode = new IndexNode(value);
        skipToken("LEFT_SQUARE_PAREN");
        indexNode.setBegin(parseAtom());
        Token operator = checkAndSkip("OPERATOR");
        if (operator != null && operator.getValue().equals("..")) {
            indexNode.setRange(true);
            if (!checkToken("RIGHT_SQUARE_PAREN")) {
                indexNode.setEnd(parseExpression());
            }
        }
        skipToken("RIGHT_SQUARE_PAREN");
        return indexNode;
    }

    private Collection<Node> parseArguments() throws Exception {
        return delimited("LEFT_PAREN", "COMMA", "RIGHT_PAREN", () -> {
            try {
                return checkAndSkip("EXPAND") != null ? new ExpandNode(parseExpression()) : parseExpression();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private OperatorNode parseOperator() throws Exception {
        OperatorNode operatorNode = new OperatorNode();
        operatorNode.setBinary(checkToken("BINARY"));
        tokenHolder.next();
        skipToken("OPERATOR_KEYWORD");
        operatorNode.setOperator(skipToken("OPERATOR").getValue());
        if (checkAndSkip("PRECEDENCE") != null) {
            operatorNode.setPrecedence(parseNumber());
        }
        operatorNode.setFunction(parseFunction(false));
        if (operatorNode.isBinary() && !precedence.containsKey(operatorNode.getOperator())) {
            precedence.put(operatorNode.getOperator(), operatorNode.getPrecedence() != null ? ((Number) ((ValueNode) operatorNode.getPrecedence()).getValue().getValue()).intValue() : 20);
        }
        return operatorNode;
    }

    private CallNode parseCall(Node function) throws Exception {
        CallNode node = new CallNode();
        node.setFunction(function);
        node.setArguments(parseArguments());
        return node;
    }

    private IfNode parseIf() throws Exception {
        IfNode node = new IfNode();
        skipToken("IF");
        skipToken("LEFT_PAREN");
        node.setCondition(parseExpression());
        skipToken("RIGHT_PAREN");
        if (checkToken("LEFT_CURLY_PAREN")) {
            node.setThen(parseBody());
        } else {
            node.setThen(parseExpression());
        }
        if (checkToken("ELSE")) {
            skipToken("ELSE");
            if (checkToken("LEFT_CURLY_PAREN")) {
                node.setOtherwise(parseBody());
            } else {
                node.setOtherwise(parseExpression());
            }
        }
        return node;
    }

    private Node parseAtom() throws Exception {
        return parseAtom(0);
    }

    private Node parseAtom(int precedence) throws Exception {
        if (precedence > functionPrecedence) {
            return parseNoCallAtom();
        } else {
            return maybeSomething(parseNoCallAtom(), functionPrecedence);
        }
    }

    private Node parseNoCallAtom() throws Exception {
        return maybePropertied(() -> {
            try {
                if (checkToken("LEFT_PAREN")) {
                    Collection<Node> expressions = delimited("LEFT_PAREN", "COMMA", "RIGHT_PAREN", () -> {
                        try {
                            return parseExpression();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    });
                    if (expressions.size() == 1) {
                        return new ParentedNode(expressions.get(0));
                    } else {
                        return new EnumerationNode(expressions);
                    }
                } else if (checkToken("CLASS")) {
                    return parseClass();
                } else if (checkToken("IMPORT")) {
                    return parseImport();
                } else if (checkToken("IF")) {
                    return parseIf();
                } else if (checkAndSkip("FUNCTION") != null) {
                    return parseFunction();
                } else if (checkToken("EXPORT")) {
                    return parseExport();
                } else if (checkToken("RETURN")) {
                    return parseReturn();
                } else if (checkToken("NUMBER")) {
                    return parseNumber();
                } else if (checkToken("STRING")) {
                    return parseString();
                } else if (checkToken("NEW")) {
                    return parseNew();
                } else if (checkAndSkip("INITIALIZER") != null) {
                    return parseBody(false);
                } else if (checkToken("LEFT_SQUARE_PAREN")) {
                    return parseArray();
                } else if (checkToken("SINGLE_LINE_COMMENT")) {
                    skipToken("SINGLE_LINE_COMMENT");
                    return new ValueNode(Value.NULL);
                } else if (checkToken("WHILE")) {
                    return parseWhile();
                } else if (checkToken("FOR")) {
                    return parseFor();
                } else if (checkToken("WHEN")) {
                    return parseWhen();
                } else if (checkToken("OPERATOR")) {
                    return parseUnary();
                } else if (checkToken("BINARY") || checkToken("UNARY")) {
                    return parseOperator();
                } else if (checkToken("SWITCH")) {
                    return parseSwitch();
                } else if (checkToken("EXPAND")) {
                    skipToken("EXPAND");
                    return new ExpandNode(parseAtom());
                } else if (checkToken("THROW")) {
                    return parseThrow();
                } else if (checkToken("TRY")) {
                    return parseTry();
                } else if (checkToken("IDENTIFIER") || (tokenHolder.lookUp() != null && tokenHolder.lookUp().getRule().isKeyword())) {
                    return parseVariable();
                } else if (checkToken("LEFT_CURLY_PAREN")) {
                    return new PropertiedNode(false, new ValueNode(Value.NULL), parseProperties());
                } else {
                    Token token = tokenHolder.lookUp();
                    if (token.getRule().getParse() != null) {
                        String code = token.getRule().getParse();
                        TokenHolder tokenHolder = new Lexer().lexFully(code, Parser.getRules(environment), Parser.toSkip);
                        Node func = new ASTBuilder().build(tokenHolder, precedence, functionPrecedence);
                        Environment scope = environment == null ? Environment.DEFAULT_ENVIRONMENT : environment.extend();
                        scope.defVariable("check_token", new Value(
                                new PSLFunction() {
                                    @Override
                                    public Value apply(Collection<Value> t) throws Exception {
                                        return new Value(checkToken((String) t.get(0).getValue()));
                                    }
                                }
                        ));
                        scope.defVariable("skip_token", new Value(
                                new PSLFunction() {
                                    @Override
                                    public Value apply(Collection<Value> t) throws Exception {
                                        Token skipped = skipToken((String) t.get(0).getValue());
                                        return tokenToPSL(skipped);
                                    }
                                }
                        ));
                        scope.defVariable("lookup", new Value(
                                new PSLFunction() {
                                    @Override
                                    public Value apply(Collection<Value> t) throws Exception {
                                        Token skipped = tokenHolder.lookUp();
                                        return tokenToPSL(skipped);
                                    }
                                }
                        ));
                        scope.defVariable("parse_expression", new Value(
                                new PSLFunction() {
                                    @Override
                                    public Value apply(Collection<Value> t) throws Exception {
                                        Node node = parseExpression();
                                        return nodeToPSL(node);
                                    }
                                }
                        ));
                        scope.defVariable("parse_body", new Value(
                                new PSLFunction() {
                                    @Override
                                    public Value apply(Collection<Value> t) throws Exception {
                                        Node node = parseBody();
                                        return nodeToPSL(node);
                                    }
                                }
                        ));
                        Value funcVal;
                        try {
                            funcVal = Evaluator.evaluate(func, scope);
                        } catch (ReturnException e) {
                            funcVal = e.getReturnValue();
                        }
                        if (token.getRule().getRuntimeParse() == null) {
                            return new ValueNode(funcVal);
                        } else {
                            String code1 = token.getRule().getRuntimeParse();
                            TokenHolder tokenHolder1 = new Lexer().lexFully(code1, Parser.getRules(environment), Parser.toSkip);
                            Node func1;
                            //System.out.println(tokenHolder1);
                            ASTBuilder builder = new ASTBuilder();
                            builder.init(tokenHolder1, precedence, functionPrecedence, false, false, null);
                            builder.skipToken("FUNCTION");
                            func1 = builder.parseFunction();
                            return new CustomNode(funcVal, func1);
                        }
                    } else throw new Exception("Unexpected token " + token);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private Node parseWhen() throws Exception {
        WhenNode whenNode = new WhenNode();
        skipToken("WHEN");
        skipToken("LEFT_CURLY_PAREN");
        Collection<Node> conditions = new Collection<>();
        Collection<Node> bodies = new Collection<>();
        while (!checkToken("RIGHT_CURLY_PAREN")) {
            if (checkToken("ELSE")) {
                skipToken("ELSE");
                skipToken("COLON");
                whenNode.setOtherwise(parseExpression());
            } else {
                conditions.add(parseExpression());
                skipToken("COLON");
                if (checkToken("LEFT_CURLY")) {
                    bodies.add(parseBody());
                } else {
                    bodies.add(parseExpression());
                }
            }
        }
        skipToken("RIGHT_CURLY_PAREN");
        whenNode.setConditions(conditions);
        whenNode.setBodies(bodies);
        return whenNode;
    }

    private ForNode parseFor() throws Exception {
        ForNode forNode = new ForNode();
        skipToken("FOR");
        skipToken("LEFT_PAREN");
        //for(IDENTIFIER in EXPRESSION){
        forNode.setName(parseIdentifier());
        skipToken("IN");
        forNode.setCollection(parseExpression());
        skipToken("RIGHT_PAREN");
        if (checkToken("LEFT_CURLY_PAREN")) {
            forNode.setBody(parseBody());
        } else {
            forNode.setBody(parseExpression());
        }
        return forNode;
    }

    private WhileNode parseWhile() throws Exception {
        WhileNode whileNode = new WhileNode();
        skipToken("WHILE");
        skipToken("LEFT_PAREN");
        whileNode.setCondition(parseExpression());
        skipToken("RIGHT_PAREN");
        if (checkToken("LEFT_CURLY_PAREN")) {
            whileNode.setBody(parseBody());
        } else {
            whileNode.setBody(parseExpression());
        }
        return whileNode;
    }

    private ArrayNode parseArray() throws Exception {
        ArrayNode arrayNode = new ArrayNode();
        arrayNode.setArray(delimited("LEFT_SQUARE_PAREN", "COMMA", "RIGHT_SQUARE_PAREN", () -> {
            try {
                return parseExpression();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }));
        return arrayNode;
    }

    private ClassNode parseClass() throws Exception {
        skipToken("CLASS");
        ClassNode classNode = new ClassNode();
        if (checkToken("IDENTIFIER")) {
            classNode.setName(parseIdentifier());
        }
        Collection<Map.Entry<String, ClassFieldNode>> collection = delimited("LEFT_CURLY_PAREN", "SEMICOLON", "RIGHT_CURLY_PAREN", () -> {
            try {
                return parseClassField();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }, true);
        HashMap<String, ClassFieldNode> map = new HashMap<>();
        for (Map.Entry<String, ClassFieldNode> entry : collection) {
            map.put(entry.getKey(), entry.getValue());
        }
        classNode.setFields(map);
        return classNode;
    }

    private SwitchNode parseSwitch() throws Exception {
        SwitchNode switchNode = new SwitchNode();
        skipToken("SWITCH");
        skipToken("LEFT_PAREN");
        switchNode.setValue(parseExpression());
        skipToken("RIGHT_PAREN");
        switchNode.setBranches(delimited("LEFT_CURLY_PAREN", null, "RIGHT_CURLY_PAREN", () -> {
            try {
                return parseSwitchBranch();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }));
        return switchNode;
    }

    private SwitchBranchNode parseSwitchBranch() throws Exception {
        SwitchBranchNode switchBranchNode = new SwitchBranchNode();
        Collection<Node> values = new Collection<>();
        while (checkToken("CASE") || checkToken("DEFAULT")) {
            if (checkAndSkip("CASE") != null) {
                values.add(parseExpression());
                skipToken("COLON");
            } else if (checkAndSkip("DEFAULT") != null) {
                values.add(null);
                skipToken("COLON");
            } else throw new Exception("wtf");
        }
        switchBranchNode.setValues(values);
        switchBranchNode.setThen(checkToken("LEFT_CURLY_BRACE") ? parseBody() : parseExpression());
        return switchBranchNode;
    }

    private Map.Entry<String, ClassFieldNode> parseClassField() throws Exception {
        ClassFieldNode classFieldNode = new ClassFieldNode();

        classFieldNode.setStatic(checkAndSkip("STATIC") != null);

        String name = parseIdentifier();
        if (checkToken("LEFT_PAREN")) {
            classFieldNode.setValue(parseFunction());
        }

        if (checkToken("OPERATOR") && skipToken("OPERATOR").getValue().equals("=")) {
            classFieldNode.setValue(parseExpression());
        }

        while (true) {
            if (checkAndSkip("GET") != null) {
                if (checkToken("LEFT_CURLY_PAREN")) {
                    classFieldNode.setGetAction(parseBody(true));
                }
            } else if (checkAndSkip("SET") != null) {
                if (checkToken("LEFT_CURLY_PAREN")) {
                    classFieldNode.setSetAction(parseBody(true));
                }
            } else break;
        }

        return new Map.Entry<String, ClassFieldNode>() {
            @Override
            public String getKey() {
                return name;
            }

            @Override
            public ClassFieldNode getValue() {
                return classFieldNode;
            }

            @Override
            public ClassFieldNode setValue(ClassFieldNode value) {
                return classFieldNode;
            }
        };
    }

    private Node maybePropertied(Supplier<Node> lambda) throws Exception {
        Node node = lambda.get();
        if (checkToken("LEFT_CURLY_PAREN") || checkToken("OVERRIDE")) {
            return new PropertiedNode(checkAndSkip("OVERRIDE") != null, node, parseProperties());
        } else return node;
    }

    private HashMap<String, Node> parseProperties() throws Exception {
        Collection<Map.Entry<String, Node>> properties =
                delimited("LEFT_CURLY_PAREN", "COMMA", "RIGHT_CURLY_PAREN", () -> {
                    try {
                        return parseProperty();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }, true);
        HashMap<String, Node> result = new HashMap<>();
        for (Map.Entry<String, Node> property : properties) {
            result.put(property.getKey(), property.getValue());
        }
        return result;
    }

    private Map.Entry<String, Node> parseProperty() throws Exception {
        if (checkToken("IDENTIFIER")) {
            String name = parseIdentifier();
            Node value1;
            if (checkAndSkip("COLON") != null) {
                value1 = parseExpression();
            } else value1 = new VariableNode(name);
            Node value = value1;
            return new Map.Entry<String, Node>() {
                @Override
                public String getKey() {
                    return name;
                }

                @Override
                public Node getValue() {
                    return value;
                }

                @Override
                public Node setValue(Node value1) {
                    return value1;
                }
            };
        } else {
            Node expression = parseExpression();
            Field field = expression.getClass().getDeclaredField("name");
            if (field != null) {
                String name = (String) field.get(expression);
                return new Map.Entry<String, Node>() {
                    @Override
                    public String getKey() {
                        return name;
                    }

                    @Override
                    public Node getValue() {
                        return expression;
                    }

                    @Override
                    public Node setValue(Node value1) {
                        return value1;
                    }
                };
            } else {
                throw new Exception("Bad value");
            }

        }
    }

    private ExportNode parseExport() throws Exception {
        skipToken("EXPORT");
        Node expression = parseExpression();
        String as = null;
        if (checkToken("AS")) {
            skipToken("AS");
            as = parseIdentifier();
        }
        return new ExportNode(expression, as);
    }

    private VariableNode parseVariable() throws Exception {
        return new VariableNode(parseIdentifier());
    }

    private String parseIdentifier() throws Exception {
        Token token = tokenHolder.lookUp();
        if (token.getRule().isKeyword() || token.getName().equals("IDENTIFIER")) {
            tokenHolder.next();
            return token.getValue();
        } else throw new Exception("Not an identifier");
    }

    private ValueNode parseString() throws Exception {
        Token token = skipToken("STRING");
        return new ValueNode(
                new Value(
                        token.getValue()
                                .substring(1, token.getValue().length() - 1)
                                .replaceAll("\\\\n", "\n")
                                .replaceAll("\\\\r", "\r")
                                .replaceAll("(\\\\)(.)", "$2")
                )
        );
    }

    private Node maybeBinary(Node left, int myPrecedence) throws Exception {
        if (nextOperator()) {
            Token operator = tokenHolder.lookUp();
            int hisPrecedence = precedence.get(operator.getValue());
            if (hisPrecedence > myPrecedence) {
                tokenHolder.next();
                Node atom = parseAtom(hisPrecedence);
                Node right = hisPrecedence > functionPrecedence ? maybeBinary(atom, hisPrecedence) :
                        maybeCallOrIndex(() -> {
                            try {
                                return maybeBinary(atom, hisPrecedence);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        });
                BinaryNode node = new BinaryNode();
                node.setLeft(left);
                node.setRight(right);
                node.setOperator(operator.getValue());
                return maybeBinary(node, myPrecedence);
            } else return left;
        }
        return left;
    }

    private Node maybeSomething(Node expr, int myPrecedence) throws Exception {
        while (true) {
            if (checkToken("LEFT_PAREN")) {
                expr = parseCall(expr);
            } else if (checkToken("LEFT_SQUARE_PAREN")) {
                expr = parseIndex(expr);
            } else if (nextOperator()) {
                Token operator = tokenHolder.lookUp();
                int hisPrecedence = precedence.get(operator.getValue());
                if (hisPrecedence > myPrecedence) {
                    tokenHolder.next();
                    Node atom = parseAtom(hisPrecedence);
                    Node right = hisPrecedence > functionPrecedence ?
                            maybeBinary(atom, hisPrecedence) :
                            maybeCallOrIndex(() -> {
                                try {
                                    return maybeBinary(atom, hisPrecedence);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return null;
                                }
                            });
                    BinaryNode node = new BinaryNode();
                    node.setLeft(expr);
                    node.setRight(right);
                    node.setOperator(operator.getValue());
                    return maybeBinary(node, myPrecedence);
                } else return expr;
            } else if (checkToken("LEFT_CURLY_PAREN") || checkToken("OVERRIDE")) {
                return new PropertiedNode(checkAndSkip("OVERRIDE") != null, expr, parseProperties());
            } else {
                return expr;
            }
        }
    }

    private Value tokenToPSL(Token token) throws Exception {
        Value returnValue = new Value(token.getName());
        returnValue.put("name", new Value(token.getName()));
        returnValue.put("value", new Value(token.getValue()));
        return returnValue;
    }

    private Value nodeToPSL(Node node) throws Exception {
        Value returnValue = new Value();
        returnValue.put("type", new Value(node.getType()));
        returnValue.put("node", new Value(node));
        return returnValue;
    }

    private boolean nextOperator() {
        return checkToken("OPERATOR");
    }
}
