package ParserPackage;

import ParserPackage.ASTNodes.VariableNode;

import java.util.*;

public class Environment {
    private HashMap<String, Variable> variables = new HashMap<>();
    private HashMap<String, BinaryOperator> binaryOperators = new HashMap<>();
    private HashMap<String, UnaryOperator> unaryOperators = new HashMap<>();
    private Value thiz = Value.NULL;
    private Environment parent;
    public Environment(Environment parent) {
        this.parent = parent;
        variables.putAll(parent != null ? parent.variables : new HashMap<>());
        binaryOperators.putAll(parent != null ? parent.binaryOperators : new HashMap<>());
        if (parent != null) thiz = parent.thiz;
    }
    public Environment extend() {
        return new Environment(this);
    }
    public Environment lookupVariable(String name) {
        Environment environment = this;
        while (environment != null) {
            if (environment.variables.containsKey(name)) return environment;
            environment = environment.parent;
        }
        return null;
    }
    public Variable defVariable(String name, Value value, boolean settable) {
        Variable variable = new Variable(value);
        variable.setSettableValue(settable);
        variables.put(name, variable);
        return variable;
    }
    public Variable defVariable(String name, Value value) {
        return defVariable(name, value, false);
    }
    public Variable getVariable(String name) {
        Environment environment = lookupVariable(name);
        if (environment == null) return null; else return environment.variables.getOrDefault(name, null);
    }
    public Variable setVariable(String name, Value value) {
        Environment environment = lookupVariable(name);
        if (environment == null && parent != null) return null;
        if (getVariable(name) != null) {
            getVariable(name).setValue(value);
        } else {
            defVariable(name, value);
        }
        return new Variable(value);
    }
    public Variable deleteVariable(String name) {
        return variables.remove(name);
    }

    public BinaryOperator defBinaryOperator(String name, BinaryOperator operator) {
        return binaryOperators.put(name, operator);
    }

    public Environment lookUpBinaryOperator(String name) {
        Environment environment = this;
        while (environment != null) {
            if (environment.binaryOperators.containsKey(name)) return environment;
            environment = environment.parent;
        }
        return null;
    }
    public BinaryOperator getBinaryOperator(String name) {
        Environment environment = lookUpBinaryOperator(name);
        if (environment == null) return null; else return environment.binaryOperators.getOrDefault(name, null);
    }

    public BinaryOperator setBinaryOperator(String name, BinaryOperator operator) {
        Environment environment = lookUpBinaryOperator(name);
        if (environment == null && parent != null) return null;
        if (getBinaryOperator(name) != null) {
            binaryOperators.put(name, operator);
        } else {
            defBinaryOperator(name, operator);
        }
        return getBinaryOperator(name);
    }

    public HashMap<String, Variable> getVariables() {
        return variables;
    }

    public void setVariables(HashMap<String, Variable> variables) {
        this.variables = variables;
    }

    public HashMap<String, BinaryOperator> getBinaryOperators() {
        return binaryOperators;
    }

    public void setBinaryOperators(HashMap<String, BinaryOperator> binaryOperators) {
        this.binaryOperators = binaryOperators;
    }

    public static final Environment DEFAULT_ENVIRONMENT = new Environment(null);

    static {
        DEFAULT_ENVIRONMENT.defBinaryOperator("+", new BinaryOperator(
                (node1, node2, environment) -> {
                    Value v1 = Evaluator.evaluate(node1, environment);
                    Value v2 = Evaluator.evaluate(node2, environment);
                    Object value1 = v1.getValue();
                    Object value2 = v2.getValue();
                    Value result;
                    if (value1 instanceof Number) {
                        if (value2 instanceof Number) {
                            result = new Value(((Number) value1).doubleValue() + ((Number) value2).doubleValue());
                        } else {
                            result = new Value(value1 + String.valueOf(value2));
                        }
                    } else if (value1 instanceof PSLFunction && value2 instanceof PSLFunction) {
                        result = new Value(
                                new PSLFunction() {
                                    @Override
                                    public Value apply(Collection<Value> t) throws Exception {
                                        return ((PSLFunction) value2).apply(
                                                node1.getType().equals("extend") ?
                                                        (Collection<Value>) ((PSLFunction) value1).apply(t).getValue()
                                                        :
                                                        new Collection<>(
                                                                ((PSLFunction) value1).apply(t)
                                                        )
                                        );
                                    }
                                }
                        );
                    } else {
                        result = new Value(String.valueOf(value1) + value2);
                    }
                    result.putAll(v1.getProperties());
                    result.putAll(v2.getProperties());
                    return result;
                }, 20
        ));
        DEFAULT_ENVIRONMENT.defBinaryOperator("*", new BinaryOperator(
                (node1, node2, environment) -> {
                    Value v1 = Evaluator.evaluate(node1, environment);
                    Value v2 = Evaluator.evaluate(node2, environment);
                    Object value1 = v1.getValue();
                    Object value2 = v2.getValue();
                    Value result;
                    if (value1 instanceof Number) {
                        if (value2 instanceof Number) {
                            result = new Value(((Number) value1).doubleValue() * ((Number) value2).doubleValue());
                        } else {
                            result = new Value(new Collection<>(Collections.nCopies(((Number) value1).intValue(), String.valueOf(value2))).join(""));
                        }
                    } else {
                        if (value2 instanceof Number) {
                            result = new Value(new Collection<>(Collections.nCopies(((Number) value2).intValue(), String.valueOf(value1))).join(""));
                        } else {
                            throw new Exception("Invalid multipliers");
                        }
                    }
                    return result;
                }, 30
        ));
        DEFAULT_ENVIRONMENT.defBinaryOperator("/", new BinaryOperator(
                (node1, node2, environment) -> {
                    Value v1 = Evaluator.evaluate(node1, environment);
                    Value v2 = Evaluator.evaluate(node2, environment);
                    Object value1 = v1.getValue();
                    Object value2 = v2.getValue();
                    Value result;
                    if (value1 instanceof Number && value2 instanceof Number) {
                        result = new Value(((Number) value1).doubleValue() / ((Number) value2).doubleValue());
                    } else throw new Exception("Both arguments should be numbers");
                    return result;
                }, 30
        ));
        DEFAULT_ENVIRONMENT.defBinaryOperator("==", new BinaryOperator(
                (node1, node2, environment) -> {
                    Value v1 = Evaluator.evaluate(node1, environment);
                    Value v2 = Evaluator.evaluate(node2, environment);
                    return new Value((v1 == v2) || (v1 != null && v1.equals(v2, false)));
                }, 18
        ));
        DEFAULT_ENVIRONMENT.defBinaryOperator("..", new BinaryOperator(
                (node1, node2, environment) -> {
                    Value v1 = Evaluator.evaluate(node1, environment);
                    Value v2 = Evaluator.evaluate(node2, environment);
                    if (v1.value instanceof Number && v2.value instanceof Number) {
                        int start = ((Number) v1.value).intValue();
                        int end = ((Number) v2.value).intValue();
                        Collection<Value> result = new Collection<>();
                        for (int i = start; i < end; i++) {
                            result.add(new Value((double) i));
                        }
                        return new Value(result);
                    } else throw new Exception("Both values should be numbers");
                }, 18
        ));
        DEFAULT_ENVIRONMENT.defBinaryOperator("===", new BinaryOperator(
                (node1, node2, environment) -> {
                    Value v1 = Evaluator.evaluate(node1, environment);
                    Value v2 = Evaluator.evaluate(node2, environment);
                    return new Value(Objects.equals(v1, v2));
                }, 18
        ));
        DEFAULT_ENVIRONMENT.defBinaryOperator("<", new BinaryOperator(
                (node1, node2, environment) -> {
                    Value v1 = Evaluator.evaluate(node1, environment);
                    Value v2 = Evaluator.evaluate(node2, environment);
                    if (v1.getValue() instanceof Number && v2.getValue() instanceof Number) {
                        return new Value(((Number) v1.getValue()).doubleValue() < ((Number) v2.getValue()).doubleValue());
                    } else throw new Exception("Not numbers");
                }, 18
        ));
        DEFAULT_ENVIRONMENT.defBinaryOperator(">", new BinaryOperator(
                (node1, node2, environment) -> {
                    Value v1 = Evaluator.evaluate(node1, environment);
                    Value v2 = Evaluator.evaluate(node2, environment);
                    if (v1.getValue() instanceof Number && v2.getValue() instanceof Number) {
                        return new Value(((Number) v1.getValue()).doubleValue() > ((Number) v2.getValue()).doubleValue());
                    } else throw new Exception("Not numbers");
                }, 18
        ));
        DEFAULT_ENVIRONMENT.defBinaryOperator("-", new BinaryOperator(
                (node1, node2, environment) -> {
                    Value v1 = Evaluator.evaluate(node1, environment);
                    Value v2 = Evaluator.evaluate(node2, environment);
                    Object value1 = v1.getValue();
                    Object value2 = v2.getValue();
                    Value result;
                    if (value1 instanceof Number) {
                        if (value2 instanceof Number) {
                            result = new Value(((Number) value1).doubleValue() - ((Number) value2).doubleValue());
                        } else {
                            result = new Value(subtract(String.valueOf(value1), String.valueOf(value2)));
                        }
                    } else {
                        result = new Value(subtract(String.valueOf(value1), String.valueOf(value2)));
                    }
                    for (Map.Entry<String, Value> entry : v1.properties.entrySet()) {
                        if (!v2.properties.containsKey(entry.getKey())) {
                            result.properties.put(entry.getKey(), entry.getValue());
                        }
                    }
                    return result;
                }, 20
        ));
        DEFAULT_ENVIRONMENT.defBinaryOperator("=", new BinaryOperator(
                (node1, node2, environment) -> {
                    Value v1 = Evaluator.evaluate(node1, environment);
                    Value v2 = Evaluator.evaluate(node2, environment);
                    if (v1.isSettable()) {
                        ((SettableValue) v1).set(v2);
                        if (v2.isSettable()) {
                            return new SettableValue(v2) {
                                @Override
                                public Value set(Value value) throws Exception {
                                    ((SettableValue) v1).set(value);
                                    return ((SettableValue) v2).set(value);
                                }

                                @Override
                                public Value setProp(String name, Value value) throws Exception {
                                    ((SettableValue) v1).setProp(name, value);
                                    return ((SettableValue) v2).setProp(name, value);
                                }
                            };
                        } else {
                            return ((SettableValue) v1).set(v2);
                        }
                    } else throw new Exception(v1 + " is not an lvalue");
                }, 10
        ));
        DEFAULT_ENVIRONMENT.defBinaryOperator("||", new BinaryOperator(
                (node1, node2, environment) -> {
                    Value value1 = Evaluator.evaluate(node1, environment);
                    if (Evaluator.toBoolean(value1)) {
                        return value1;
                    } else {
                        Value value2 = Evaluator.evaluate(node2, environment);
                        if (Evaluator.toBoolean(value2)) {
                            return value2;
                        } else {
                            return new Value(false);
                        }
                    }
                }, 15
        ));
        DEFAULT_ENVIRONMENT.defBinaryOperator("<<", new BinaryOperator(
                (node1, node2, environment) -> {
                    Value value1 = Evaluator.evaluate(node1, environment);
                    if (value1.get("write") != null) {
                        Value value2 = Evaluator.evaluate(node2, environment);
                        ((PSLFunction) value1.get("write").getValue()).apply(new Collection<>(value2));
                        return value1;
                    } else {
                        throw new Exception("Is not a stream");
                    }
                }, 12
        ));
        DEFAULT_ENVIRONMENT.defBinaryOperator(">>", new BinaryOperator(
                (node1, node2, environment) -> {
                    Value value1 = Evaluator.evaluate(node1, environment);
                    if (value1.get("read") != null) {
                        Value value2 = Evaluator.evaluate(node2, environment);
                        ((SettableValue) value2).set(((PSLFunction) value1.get("read").getValue()).apply(new Collection<>(value2)));
                        return value1;
                    } else {
                        throw new Exception("Is not a stream");
                    }
                }, 12
        ));
        DEFAULT_ENVIRONMENT.defBinaryOperator("&&", new BinaryOperator(
                (node1, node2, environment) -> {
                    Value value1 = Evaluator.evaluate(node1, environment);
                    if (!Evaluator.toBoolean(value1)) {
                        return new Value(false);
                    } else {
                        Value value2 = Evaluator.evaluate(node2, environment);
                        if (Evaluator.toBoolean(value2)) {
                            return value2;
                        } else {
                            return new Value(false);
                        }
                    }
                }, 15
        ));
        DEFAULT_ENVIRONMENT.defBinaryOperator("|>", new BinaryOperator(
                (node1, node2, environment) -> {
                    Value value1 = Evaluator.evaluate(node1, environment);
                    Value value2 = Evaluator.evaluate(node2, environment);
                    return ((PSLFunction) value2.getValue()).apply(new Collection<>(value1));
                }, 16
        ));
        DEFAULT_ENVIRONMENT.defBinaryOperator(".", new BinaryOperator(
                (node1, node2, environment) -> {
                    Value value2 = Evaluator.evaluate(node1, environment);
                    String prop;
                    if (node2.getType().equals("variable")) {
                        prop = ((VariableNode) node2).getValue();
                    } else {
                        prop = Evaluator.evaluate(node2, environment).toString();
                    }
                    Environment scope = environment.extend();
                    scope.setThiz(value2);
                    if (value2.isSettable()) {
                        Value val = value2.get(prop);
                        return new SettableValue(
                                new EnvironmentValue(val, scope)
                        ) {
                            @Override
                            public Value set(Value value1) throws Exception {
                                return ((SettableValue)value2).setProp(prop, value1);
                            }

                            @Override
                            public Value setProp(String name, Value value1) throws Exception {
                                return value2.get(prop).put(name, value1);
                            }
                        };
                    } else return new EnvironmentValue(value2.get(prop), scope);
                }, 200
        ));
        Value system = new Value(null);
        Value stdout = new Value(null);
        Value stdin = new Value(null);
        try {
            stdout.put("println", new Value(
                    new PSLFunction() {
                        @Override
                        public Value apply(Collection<Value> args) throws Exception {
                            for (Value arg : args) {
                                System.out.println(arg);
                            }
                            return Value.NULL;
                        }
                    }
            ));
            stdout.put("print", new Value(
                    new PSLFunction() {
                        @Override
                        public Value apply(Collection<Value> args) throws Exception {
                            for (Value arg : args) {
                                System.out.print(arg);
                            }
                            return Value.NULL;
                        }
                    }
            ));
            stdout.put("write_line", new Value(
                    new PSLFunction() {
                        @Override
                        public Value apply(Collection<Value> args) throws Exception {
                            for (Value arg : args) {
                                System.out.println(arg);
                            }
                            return Value.NULL;
                        }
                    }
            ));
            stdout.put("write", new Value(
                    new PSLFunction() {
                        @Override
                        public Value apply(Collection<Value> args) throws Exception {
                            for (Value arg : args) {
                                System.out.print(arg);
                            }
                            return Value.NULL;
                        }
                    }
            ));
            Value streamValue = new Value();
            streamValue.put("write", new Value(new PSLFunction() {
                @Override
                public Value apply(Collection<Value> t) throws Exception {
                    for (Value arg : t) {
                        System.out.print(arg);
                    }
                    return Value.NULL;
                }
            }));
            stdout.put("stream", streamValue);
            Value inLineStream = new Value();
            inLineStream.put("read", new Value(
                    new PSLFunction() {
                        @Override
                        public Value apply(Collection<Value> t) throws Exception {
                            return new Value(new Scanner(System.in).nextLine());
                        }
                    }
            ));
            Value inWordStream = new Value();
            inWordStream.put("read", new Value(
                    new PSLFunction() {
                        @Override
                        public Value apply(Collection<Value> t) throws Exception {
                            return new Value(new Scanner(System.in).next());
                        }
                    }
            ));
            Value inNumberStream = new Value();
            inNumberStream.put("read", new Value(
                    new PSLFunction() {
                        @Override
                        public Value apply(Collection<Value> t) throws Exception {
                            return new Value(new Scanner(System.in).nextDouble());
                        }
                    }
            ));
            stdin.put("number_stream", inNumberStream);
            stdin.put("line_stream", inLineStream);
            stdin.put("word_stream", inWordStream);
            stdin.put("read_line", new Value(
                    new PSLFunction() {
                        @Override
                        public Value apply(Collection<Value> t) throws Exception {
                            return new Value(new Scanner(System.in).nextLine());
                        }
                    }
            ));
            stdin.put("read_number", new Value(
                    new PSLFunction() {
                        @Override
                        public Value apply(Collection<Value> t) throws Exception {
                            return new Value(new Scanner(System.in).nextDouble());
                        }
                    }
            ));
            stdin.put("read_word", new Value(
                    new PSLFunction() {
                        @Override
                        public Value apply(Collection<Value> t) throws Exception {
                            return new Value(new Scanner(System.in).next());
                        }
                    }
            ));
            stdin.put("read_char", new Value(
                    new PSLFunction() {
                        @Override
                        public Value apply(Collection<Value> t) throws Exception {
                            return new Value(new Scanner(System.in).nextLine().substring(0, 1));
                        }
                    }
            ));
            system.put("stdout", stdout);
            system.put("stdin", stdin);
        } catch (Exception ignored) {
            new Exception("Internal Error").printStackTrace();
        }
        DEFAULT_ENVIRONMENT.defVariable("system", system);
        DEFAULT_ENVIRONMENT.defVariable("null", new Value() {
            @Override
            public boolean isSettable() {
                return false;
            }

            @Override
            public HashMap<String, Value> getProperties() {
                return new HashMap<>();
            }

            @Override
            public void setProperties(HashMap<String, Value> properties) {

            }

            @Override
            public Value get(String key) throws Exception {
                return Value.NULL;
            }

            @Override
            public Value put(String key, Value value) throws Exception {
                return Value.NULL;
            }

            @Override
            public Object getValue() {
                return null;
            }

            @Override
            public void setValue(Object value) {

            }

            @Override
            public String stringifyProps() {
                return "{}";
            }

            @Override
            public String toString() {
                return "null";
            }

            @Override
            public void putAll(Map<? extends String, ? extends Value> m) {

            }

            @Override
            public boolean equals(Object obj) {
                return super.equals(obj);
            }

            @Override
            public boolean equals(Object obj, boolean strict) {
                return super.equals(obj, strict);
            }
        });
        DEFAULT_ENVIRONMENT.defVariable("typeof", new Value(
                new PSLFunction() {
                    @Override
                    public Value apply(Collection<Value> t) throws Exception {
                        return new Value(t.get(0).getClass());
                    }
                }
        ));
        DEFAULT_ENVIRONMENT.defVariable("true", new Value(true));
        DEFAULT_ENVIRONMENT.defVariable("false", new Value(false));
        DEFAULT_ENVIRONMENT.defUnaryOperator("-", new UnaryOperator(
                (node, environment) -> {
                    Value value = Evaluator.evaluate(node, environment);
                    if (value.getValue() instanceof Number) {
                        return new Value(-((Number) value.getValue()).doubleValue());
                    } else throw new Exception("Invalid value");
                }
        ));
        DEFAULT_ENVIRONMENT.defUnaryOperator("!", new UnaryOperator(
                (node, environment) -> {
                    Value value = Evaluator.evaluate(node, environment);
                    return new Value(!Evaluator.toBoolean(value));
                }
        ));
        DEFAULT_ENVIRONMENT.defUnaryOperator("++", new UnaryOperator(
                (node, environment) -> {
                    Value value = Evaluator.evaluate(node, environment);
                    if (value.isSettable()) {
                        Object val = value.value;
                        if (val instanceof Number) {
                            return ((SettableValue) value).set(new Value(((Number) val).doubleValue() + 1));
                        } else throw new Exception("Invalid value");
                    } else throw new Exception("Immutable value");
                }
        ));
    }

    public HashMap<String, UnaryOperator> getUnaryOperators() {
        return unaryOperators;
    }

    public void setUnaryOperators(HashMap<String, UnaryOperator> unaryOperators) {
        this.unaryOperators = unaryOperators;
    }

    public UnaryOperator defUnaryOperator(String name, UnaryOperator operator) {
        return unaryOperators.put(name, operator);
    }

    public Environment lookUpUnaryOperator(String name) {
        Environment environment = this;
        while (environment != null) {
            if (environment.unaryOperators.containsKey(name)) return environment;
            environment = environment.parent;
        }
        return null;
    }
    public UnaryOperator getUnaryOperator(String name) {
        Environment environment = lookUpUnaryOperator(name);
        if (environment == null) return null; else return environment.unaryOperators.getOrDefault(name, null);
    }

    public UnaryOperator setUnaryOperator(String name, UnaryOperator operator) {
        Environment environment = lookUpUnaryOperator(name);
        if (environment == null && parent != null) return null;
        if (getUnaryOperator(name) != null) {
            unaryOperators.put(name, operator);
        } else {
            defUnaryOperator(name, operator);
        }
        return getUnaryOperator(name);
    }

    public Value getThiz() {
        return thiz;
    }

    public void setThiz(Value thiz) {
        this.thiz = thiz;
    }

    private static String subtract(String value1, String value2) throws Exception {
        if (String.valueOf(value1).contains(String.valueOf(value2))) {
            return String.valueOf(value1).substring(0, String.valueOf(value1).lastIndexOf(String.valueOf(value2))) +
                    String.valueOf(value1).substring(String.valueOf(value1).lastIndexOf(String.valueOf(value2)) + String.valueOf(value2).length());
        }else{
            throw new Exception("Dima pidor");
        }
    }
}