package ParserPackage;

import ParserPackage.ASTNodes.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Evaluator {
    public static Value EXPORTS = new Value(null);
    public static Value evaluate(Node node, Environment environment) throws Exception {
        switch (node.getType()) {
            case "program":
                for (Node node1 : ((ProgramNode) node).getProgram()) {
                    Evaluator.evaluate(node1, environment);
                }
                Value returnValue = EXPORTS;
                EXPORTS = new Value(null);
                return returnValue;
            case "value":
                return ((ValueNode) node).getValue();
            case "function":
                Value func = makeFunction((FunctionNode) node, environment);
                if (((FunctionNode) node).getName() != null) {
                    environment.setVariable(((FunctionNode) node).getName(), func);
                }
                return func;
            case "call":
                return evalCall(node, environment);
            case "variable":
                String name = ((VariableNode) node).getValue();
                if (name.equals("global")) {
                    Value value = new SettableValue(Value.NULL) {
                        @Override
                        public Value set(Value value) throws Exception {
                            return this;
                        }

                        @Override
                        public Value setProp(String name, Value value) throws Exception {
                            environment.setVariable(name, value);
                            return value;
                        }
                    };
                    for (Map.Entry<String, Variable> entry : environment.getVariables().entrySet()) {
                        value.put(entry.getKey(), new SettableValue(entry.getValue().getValue()) {
                            @Override
                            public Value set(Value value) throws Exception {
                                return ((SettableValue)value).setProp(name, value);
                            }

                            @Override
                            public Value setProp(String name1, Value value) throws Exception {
                                Variable variable =  environment.getVariable(name);
                                if (variable.getValue() == null) variable.setValue(Value.NULL);
                                return variable.put(name1, value);
                            }
                        });
                    }
                    return value;
                }
                Variable variable = environment.getVariable(name);
                if (variable != null) {
                    Value val = variable.getValue();
                    return new SettableValue(val) {
                        @Override
                        public Value get(String key) throws Exception {
                            return val.get(key);
                        }

                        @Override
                        public Value put(String key, Value value) throws Exception {
                            return val.put(key, value);
                        }

                        @Override
                        public Value set(Value value) {
                            variable.setValue(value);
                            if (variable.isSettableValue() && val.isSettable()) {
                                try {
                                    ((SettableValue) val).set(value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            return value;
                        }

                        @Override
                        public Value setProp(String name, Value value) throws Exception {
                            variable.put(name, value);
                            if (variable.isSettableValue() && val.isSettable()) {
                                try {
                                    ((SettableValue) val).setProp(name, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            return value;
                        }
                    };
                } else {
                    return new SettableValue(null) {
                        @Override
                        public Value set(Value value) {
                            return environment.defVariable(((VariableNode) node).getValue(), value).getValue();
                        }

                        @Override
                        public Value setProp(String name, Value value) throws Exception {
                            throw new Exception(((VariableNode) node).getValue() + " is not defined");
                        }
                    };
                }
            case "export":
                Node exportedNode = ((ExportNode) node).getValue();
                String as = ((ExportNode) node).getAlias();
                Value evaluated = Evaluator.evaluate(exportedNode, environment);
                if (as == null) {
                    try {
                        Field field = exportedNode.getClass().getDeclaredField("name");
                        field.setAccessible(true);
                        if (field.get(exportedNode) != null) {
                            EXPORTS.put((String) field.get(exportedNode), evaluated);
                        } else throw new NoSuchFieldException();
                    } catch (NoSuchFieldException e) {
                        EXPORTS.setValue(evaluated.getValue());
                    }
                } else {
                    EXPORTS.put(as, evaluated);
                }
                return evaluated;
            case "binary":
                BinaryOperator operator = environment.getBinaryOperator(((BinaryNode) node).getOperator());
                return operator.getAction().apply(
                        ((BinaryNode) node).getLeft(),
                        ((BinaryNode) node).getRight(),
                        environment
                );
            case "body":
            case "functionBody":
                Collection<Value> result = new Collection<>();
                for (Node node1: ((BodyNode) node).getExpressions()) {
                    result.add(Evaluator.evaluate(node1, environment));
                }
                return new Value(result);
            case "return":
                throw new ReturnException(evaluate(((ReturnNode) node).getValue(), environment));
            case "enumeration":
                Collection<Value> values = ((EnumerationNode) node).getNodes().map(node1 -> {
                    try {
                        return evaluate(node1, environment);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                });
                return new SettableValue(new Value(values)) {
                    @Override
                    public Value set(Value value) throws Exception {
                        Value lastValue = null;
                        for (Value value1: values) {
                            try {
                                lastValue = ((SettableValue) value1).set(value);
                            } catch (ClassCastException e) {
                                throw new Exception(value1 + " is not an lvalue");
                            }
                        }
                        return lastValue;
                    }

                    @Override
                    public Value setProp(String name, Value value) throws Exception {
                        Value lastValue = null;
                        for (Value value1: values) {
                            try {
                                lastValue = ((SettableValue) value1).setProp(name, value);
                            } catch (ClassCastException e) {
                                throw new Exception(value1 + " is not an lvalue");
                            }
                        }
                        return lastValue;
                    }
                };
            case "import":
                ImportNode importNode = (ImportNode) node;
                Value exports;
                String filename = Evaluator.evaluate(importNode.getFilename(), environment).getValue().toString();
                File file = new File(filename);
                if (new File(filename + ".build").exists() || importNode.isBuilt()) {
                    exports = Parser.run(filename + (importNode.isBuilt() ? "" : ".build"));
                } else if (importNode.isNative()) {
                    URL url = file.getParentFile().toURI().toURL();
                    URL[] urls = new URL[]{url};

                    ClassLoader cl = new URLClassLoader(urls);

                    Class clazz = cl.loadClass(file.getName());

                    PSLClass cls = buildClass(clazz, environment);

                    exports = new Value();
                    String className;
                    try {
                        Field field = clazz.getDeclaredField("__NAME__");
                        field.setAccessible(true);
                        className = (String) field.get(null);
                    } catch (Exception e) {
                        className = clazz.getName();
                    }
                    exports.put(className, cls);
                } else {
                    Value filenameValue = Evaluator.evaluate(importNode.getFilename(), environment);
                    if (String.class.isAssignableFrom(filenameValue.getValue().getClass())) {
                        exports = Parser.interpret(filename, environment);
                    } else throw new Exception("String expected after FROM");
                }
                if (!importNode.isEmpty()) for (Map.Entry<String, String> entry: importNode.entrySet()) {
                    if (exports.get(entry.getKey()) != null) {
                        environment.setVariable(entry.getValue(), exports.get(entry.getKey()));
                    } else throw new Exception("Bad import name: " + entry.getKey());
                } else for (Map.Entry<String, Value> entry: exports.getProperties().entrySet()) {
                    environment.setVariable(entry.getKey(), entry.getValue());
                }
                return exports;
            case "parented":
                return Evaluator.evaluate(((ParentedNode) node).getNode(), environment);
            case "propertied":
                PropertiedNode propertiedNode = (PropertiedNode) node;
                Value val = Evaluator.evaluate(propertiedNode.getNode(), environment);
                if (propertiedNode.isOverride()) {
                    HashMap<String, Value> props = new HashMap<>();
                    for (Map.Entry<String, Node> entry: propertiedNode.getProperties().entrySet()) {
                        props.put(entry.getKey(), Evaluator.evaluate(entry.getValue(), environment));
                    }
                    val.setProperties(props);
                } else {
                    for (Map.Entry<String, Node> entry: propertiedNode.getProperties().entrySet()) {
                        if (entry.getValue() != null) {
                            val.put(entry.getKey(), Evaluator.evaluate(entry.getValue(), environment));
                        } else {
                            val.put(entry.getKey(), Evaluator.evaluate(new VariableNode(entry.getKey()), environment));
                        }
                    }
                }
                return new SettableValue(val) {
                    @Override
                    public Value set(Value value) throws Exception {
                        for (Map.Entry<String, Value> entry: val.getProperties().entrySet()) {
                            if (entry.getValue().isSettable()) {
                                if (value.getProperties().containsKey(entry.getKey())) {
                                    ((SettableValue) entry.getValue()).set(value.get(entry.getKey()));
                                } else {
                                    ((SettableValue) entry.getValue()).set(Value.NULL);
                                }
                            }
                        }
                        return val;
                    }

                    @Override
                    public Value setProp(String name, Value value) throws Exception {
                        for (Map.Entry<String, Value> entry: val.getProperties().entrySet()) {
                            if (entry.getValue().isSettable()) {
                                if (value.getProperties().containsKey(entry.getKey())) {
                                    ((SettableValue) entry.getValue()).setProp(name, value.get(entry.getKey()));
                                } else {
                                    ((SettableValue) entry.getValue()).setProp(name, Value.NULL);
                                }
                            }
                        }
                        return val;
                    }
                };
            case "new":
                NewNode newNode = (NewNode) node;
                Value classValue = Evaluator.evaluate(newNode.getClazz(), environment);
                if (classValue.getClass().equals(PSLClass.class)) {
                    return ((PSLClass) classValue).instantiate(newNode.getArguments().map(node1 -> {
                        try {
                            return Evaluator.evaluate(node1, environment);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }));
                } else return ((PSLClass) ((SettableValue) classValue).getRealValue()).instantiate(newNode.getArguments().map(node1 -> {
                    try {
                        return Evaluator.evaluate(node1, environment);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }));
            case "class":
                PSLClass pslClass = new PSLClass(environment);
                ClassNode classNode = (ClassNode) node;
                HashMap<String, PSLClassField> statics = new HashMap<>();
                HashMap<String, ClassFieldNode> prototype = new HashMap<>();
                for (Map.Entry<String, ClassFieldNode> entry: classNode.getFields().entrySet()) {
                    if (entry.getValue().isStatic()) {
                        statics.put(entry.getKey(), (PSLClassField) Evaluator.evaluate(entry.getValue(), pslClass.getScope()));
                    } else {
                        prototype.put(entry.getKey(), entry.getValue());
                    }
                }
                pslClass.setClassPrototype(prototype);
                pslClass.setStatics(statics);
                return pslClass;
            case "field":
                PSLClassField pslClassField = new PSLClassField();
                ClassFieldNode classFieldNode = (ClassFieldNode) node;
                if (classFieldNode.getGetAction() != null) {
                    FunctionNode functionNode = new FunctionNode();
                    ParameterNode parameterNode = new ParameterNode();
                    parameterNode.setName("value");
                    functionNode.setParameters(new Collection<>(parameterNode));
                    functionNode.setBody(classFieldNode.getGetAction());
                    pslClassField.setOnGet((PSLFunction) Evaluator.evaluate(functionNode, environment).getValue());
                }
                if (classFieldNode.getSetAction() != null) {
                    FunctionNode functionNode = new FunctionNode();
                    functionNode.setBody(classFieldNode.getSetAction());
                    ParameterNode parameterNode0 = new ParameterNode();
                    parameterNode0.setName("currentValue");
                    ParameterNode parameterNode1 = new ParameterNode();
                    parameterNode1.setName("value");
                    functionNode.setParameters(new Collection<>(parameterNode0, parameterNode1));
                    pslClassField.setOnSet((PSLFunction) Evaluator.evaluate(functionNode, environment).getValue());
                }
                if (classFieldNode.getValue() != null)
                    pslClassField.setDefaultValue(Evaluator.evaluate(classFieldNode.getValue(), environment));
                return pslClassField;
            case "if":
                IfNode ifNode = (IfNode) node;
                if (toBoolean(Evaluator.evaluate(ifNode.getCondition(), environment))) {
                    return Evaluator.evaluate(ifNode.getThen(), environment);
                } else {
                    if (ifNode.getOtherwise() != null) {
                        return Evaluator.evaluate(ifNode.getOtherwise(), environment);
                    } else return Value.NULL;
                }
            case "array":
                Collection<Value> array = new Collection<>();
                for (Node node1 : ((ArrayNode) node).getArray()) {
                    array.add(Evaluator.evaluate(node1, environment));
                }

                return new SettableValue(new Value(array)) {
                    @Override
                    public Value set(Value value3) throws Exception {
                        if (value3.getValue() instanceof Collection) {
                            Collection<Value> values1 = (Collection<Value>) value3.getValue();
                            for (int i = 0; i < array.size(); i++) {
                                Value element = array.get(i);
                                if (element.isSettable()) {
                                    if (i < values1.size()) {
                                        ((SettableValue) element).set(values1.get(i));
                                    } else {
                                        ((SettableValue) element).set(Value.NULL);
                                    }
                                }
                            }
                        } else {
                            for (Value element: array) {
                                if (element.isSettable()) ((SettableValue) element).set(value3);
                            }
                        }
                        return value3;
                    }

                    @Override
                    public Value setProp(String name1, Value value3) throws Exception {
                        if (value3.getValue() instanceof Collection) {
                            Collection<Value> values1 = (Collection<Value>) value3.getValue();
                            for (int i = 0; i < array.size(); i++) {
                                Value element = array.get(i);
                                if (element.isSettable()) {
                                    if (i < values1.size()) {
                                        ((SettableValue) element).setProp(name1, values1.get(i));
                                    } else {
                                        ((SettableValue) element).setProp(name1, Value.NULL);
                                    }
                                }
                            }
                        } else {
                            for (Value element: array) {
                                if (element.isSettable()) ((SettableValue) element).setProp(name1, value3);
                            }
                        }
                        return value3;
                    }
                };
            case "index":
                IndexNode indexNode = (IndexNode) node;
                Value value = Evaluator.evaluate(indexNode.getValue(), environment);
                if (value.getValue() instanceof Collection) {
                    Collection<Value> arr = (Collection<Value>) value.getValue();
                    if (indexNode.isRange()) {
                        Value start = Evaluator.evaluate(indexNode.getBegin(), environment);
                        if (start.getValue() instanceof Number) {
                            int begin = ((Number) start.getValue()).intValue();
                            if (indexNode.getEnd() == null) {
                                return new Value(arr.slice(begin));
                            } else {
                                Value finish = Evaluator.evaluate(indexNode.getEnd(), environment);
                                if (finish.getValue() instanceof Number) {
                                    int end = ((Number) finish.getValue()).intValue();
                                    if (end < 0) end += arr.size() - 1;
                                    return new Value(arr.slice(begin, end));
                                } else throw new Exception("Index value should be an integer");
                            }
                        } else {
                            throw new Exception("Index value should be an integer");
                        }
                    } else {
                        Value index = Evaluator.evaluate(indexNode.getBegin(), environment);
                        if (index.getValue() instanceof Number) {
                            int ind = ((Number) index.getValue()).intValue();
                            return new SettableValue(arr.size() > ind ? arr.get(ind) : Value.NULL) {
                                @Override
                                public Value set(Value value) throws Exception {
                                    while (ind >= arr.size()) {
                                        arr.add(Value.NULL);
                                    }
                                    return arr.set(ind, value);
                                }

                                @Override
                                public Value setProp(String name, Value value) throws Exception {
                                    arr.get(ind).put(name, value);
                                    return value;
                                }
                            };
                        } else {
                            throw new Exception("Index value should be an integer");
                        }
                    }
                } else if (value.getValue() instanceof String) {
                    String string = (String) value.getValue();
                    if (indexNode.isRange()) {
                        Value start = Evaluator.evaluate(indexNode.getBegin(), environment);
                        if (start.getValue() instanceof Number) {
                            int begin = ((Number) start.getValue()).intValue();
                            if (indexNode.getEnd() == null) {
                                return new Value(string.substring(begin));
                            } else {
                                Value finish = Evaluator.evaluate(indexNode.getEnd(), environment);
                                if (finish.getValue() instanceof Number) {
                                    int end = ((Number) finish.getValue()).intValue();
                                    if (end < 0) end += string.length() - 1;
                                    return new Value(string.substring(begin, end + 1));
                                } else throw new Exception("Index value should be an integer");
                            }
                        } else {
                            throw new Exception("Index value should be an integer");
                        }
                    } else {
                        Value index = Evaluator.evaluate(indexNode.getBegin(), environment);
                        if (index.getValue() instanceof Number) {
                            return new Value(String.valueOf(string.charAt(((Number) index.getValue()).intValue())));
                        } else {
                            throw new Exception("Index value should be an integer");
                        }
                    }
                } else throw new Exception("Bad value for index");
            case "while":
                WhileNode whileNode = (WhileNode) node;
                Collection<Value> arr = new Collection<>();
                while (toBoolean(Evaluator.evaluate(whileNode.getCondition(), environment))) {
                    arr.add(Evaluator.evaluate(whileNode.getBody(), environment));
                }
                return new Value(arr);
            case "for":
                ForNode forNode = (ForNode) node;
                Collection<Value> kar = (Collection<Value>) Evaluator.evaluate(forNode.getCollection(), environment).getValue();
                Collection<Value> res = new Collection<>();
                for (Value k: kar) {
                    Environment scope = environment.extend();
                    scope.defVariable(forNode.getName(), k);
                    res.add(Evaluator.evaluate(forNode.getBody(), scope));
                }
                return new Value(res);
            case "when":
                WhenNode whenNode = (WhenNode)node;
                for (int j = 0; j < whenNode.getConditions().size(); j++) {
                    if (toBoolean(Evaluator.evaluate(whenNode.getConditions().get(j) , environment))){
                        return Evaluator.evaluate(whenNode.getBodies().get(j) , environment);
                    }
                }
                return whenNode.getOtherwise() == null ? Value.NULL : Evaluator.evaluate(whenNode.getOtherwise(), environment);
            case "unary":
                UnaryNode unaryNode = (UnaryNode) node;
                UnaryOperator unaryOperator = environment.getUnaryOperator(unaryNode.getOperator());
                if (unaryOperator == null) throw new Exception("Unary operator '" + unaryNode.getOperator() + "' not found");
                return unaryOperator.getAction().apply(unaryNode.getValue(), environment);
            case "operator":
                OperatorNode operatorNode  = (OperatorNode) node;
                PSLFunction function = (PSLFunction) Evaluator.evaluate(operatorNode.getFunction(), environment).getValue();
                if (operatorNode.isBinary()) {
                    environment.defBinaryOperator(
                            operatorNode.getOperator(),
                            new BinaryOperator(
                                    (node1, node2, env) -> function.apply(new Collection<>(
                                                    Evaluator.evaluate(node1, env),
                                                    Evaluator.evaluate(node2, env)
                                            )),
                                    operatorNode.getPrecedence() == null ? 15 : ((Number) Evaluator.evaluate(operatorNode.getPrecedence(), environment).getValue()).intValue()
                            )
                    );
                } else {
                    environment.defUnaryOperator(
                            operatorNode.getOperator(),
                            new UnaryOperator(
                                    (node1, env) -> function.apply(new Collection<>(
                                                    Evaluator.evaluate(node1, env)
                                            ))
                                    )
                    );
                }
                return Value.NULL;
            case "expand":
                return Evaluator.evaluate(((ExpandNode) node).getNode(), environment);
            case "switch":
                SwitchNode switchNode = (SwitchNode) node;
                Value switched = Evaluator.evaluate(switchNode.getValue(), environment);
                for (SwitchBranchNode switchBranchNode: switchNode.getBranches()) {
                    for (Node caseValue: switchBranchNode.getValues()) {
                        if (caseValue == null || equals(switched, evaluate(caseValue, environment), true)) {
                            return evaluate(switchBranchNode.getThen(), environment);
                        }
                    }
                }
                return Value.NULL;
            case "custom":
                CustomNode customNode = (CustomNode) node;
                Value value1 = customNode.getValue();
                Value parse = Evaluator.evaluate(customNode.getParse(), environment);
                return ((PSLFunction) parse.getValue()).apply(
                        new Collection<>(value1, environmentToPSL(environment))
                );
            case "throw":
                throw new PSLException(Evaluator.evaluate(((ThrowNode) node).getNode(), environment));
            case "try":
                TryNode tryNode = (TryNode) node;
                Value ret = Value.NULL;
                boolean changed = false;
                try {
                    ret = Evaluator.evaluate(tryNode.getToTry(), environment);
                    changed = true;
                } catch (PSLException e) {
                    if (tryNode.getToCatch() != null) {
                        ret = ((PSLFunction) Evaluator.evaluate(tryNode.getToCatch(), environment).getValue()).apply(new Collection<>(e.getValue()));
                        changed = true;
                    }
                }
                if (tryNode.getElseFinally() != null) {
                    Value value2 = Evaluator.evaluate(tryNode.getElseFinally(), environment);
                    if (!changed) ret = value2;
                }
                return ret;
            default: throw new Exception("Don't know how to evaluate " + node.getType());
        }
    }
    private static Value makeFunction(FunctionNode node, Environment environment) {
        Value value = new Value(null);
        PSLFunction function = new PSLFunction() {
            @Override
            public Value apply(Collection<Value> arguments) throws Exception {
                Environment scope = environment.extend();
                Collection<ParameterNode> parameters = node.getParameters();
                for (int i = 0; i < parameters.size(); i++) {
                    ParameterNode parameter = parameters.get(i);
                    if (parameter.isExpanded()) {
                        Collection<Value> values;
                        try {
                            values = new Collection<>(
                                    arguments.subList(i, arguments.size())
                            );
                        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                            values = new Collection<>();
                        }
                        scope.defVariable(parameter.getName(), new Value(values));
                        break;
                    }
                    Value argument1;
                    try {
                        argument1 = arguments.get(i);
                    } catch (IndexOutOfBoundsException e) {
                        argument1 = parameter.getDefaultValue() != null ?
                                Evaluator.evaluate(parameter.getDefaultValue(), environment)
                                : Value.NULL;
                    }
                    final Value argument = argument1;
                    if (argument != null) {
                        if (!parameter.isReference()) {
                            scope.defVariable(parameter.getName(), argument);
                        } else if (argument.isSettable()) {
                            scope.defVariable(parameter.getName(), argument, true);
                        } else {
                            throw new Exception("Is not settable");
                        }
                    }
                }
                try {
                    return Evaluator.evaluate(node.getBody(), scope);
                } catch (ReturnException e) {
                    return e.getReturnValue();
                }
            }

            @Override
            public String toString() {
                return "[PSL function]";
            }
        };
        value.setValue(function);
        return value;
    }
    private static Value evalCall(Node node, Environment environment) throws Exception {
        Value functionValue = Evaluator.evaluate(((CallNode) node).getFunction(), environment);
        PSLFunction function = (PSLFunction) (functionValue.getValue());
        if (function == null) throw new Exception("Undefined function: " + ((CallNode) node).getFunction());

        Collection<Value> arguments = new Collection<>();
        for (Node node1 : ((CallNode) node).getArguments()) {
            Value val = Evaluator.evaluate(node1, environment);
            if (node1 instanceof ExpandNode) {
                arguments.addAll((Collection<Value>) val.getValue());
            } else {
                arguments.add(val);
            }
        }
        Environment scope = environment;

        if (functionValue instanceof EnvironmentValue) {
            scope = ((EnvironmentValue) functionValue).getEnvironment();
        } else if (functionValue instanceof SettableValue
                && ((SettableValue) functionValue).getRealValue() instanceof EnvironmentValue) {
            scope = ((EnvironmentValue) ((SettableValue) functionValue).getRealValue()).getEnvironment();
        }

        return function.apply(arguments, scope);
    }
    private static Exception croak(String reason) {
        Exception throwable = new Exception(reason);
        throwable.printStackTrace();
        return throwable;
    }
    public static boolean toBoolean(Value value) {
        Object val = value.getValue();
        if (val != null) {
            if (val.getClass() == String.class) {
                return ((String) val).length() > 0;
            } else if (val instanceof Number) {
                return ((Number) val).doubleValue() != 0;
            } else if (val.getClass() == Boolean.class) {
                return (Boolean) val;
            } else return true;
        } else {
            return !value.properties.isEmpty();
        }
    }
    public static boolean equals(Value value1, Value value2, boolean strict) throws Exception {
        try{
            return value1.equals(value2, strict);
        }catch (Throwable e){
            throw new Exception("Ahuet");
        }
    }
    private static Value objectToValue(Object value, Environment environment) throws Exception {
        if (value == null || value instanceof String || getWrapperTypes().contains(value.getClass())) {
            return new Value(value);
        } else if (value.getClass().isAnnotationPresent(FunctionalInterface.class)) {
            return methodToValue(value.getClass().getDeclaredMethods()[0], value, environment);
        } else {
            Value result = new Value(value);
            for (Method method: value.getClass().getDeclaredMethods()) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    method.setAccessible(true);
                    result.put(method.getName(), methodToValue(method, environment));
                }
            }
            for (Field field: value.getClass().getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    result.put(field.getName(), objectToValue(field.get(value), environment));
                }
            }
            for (Class clazz: value.getClass().getDeclaredClasses()) {
                if (!Modifier.isStatic(clazz.getModifiers())) {
                    result.put(clazz.getName(), buildClass(clazz, environment));
                }
            }
            return result;
        }
    }
    private static Value methodToValue(Method method, Object thiz, Environment environment) {
        return new Value(
                new PSLFunction() {
                    @Override
                    public Value apply(Collection<Value> t) throws Exception {
                        Object[] arr = t.map(Value::getValue).toArray();
                        Object result = method.invoke(thiz, arr);
                        return objectToValue(result, environment);
                    }
                }
        );
    }
    private static Value methodToValue(Method method, Environment environment) {
        return methodToValue(method, null, environment);
    }
    private static Set<Class<?>> getWrapperTypes() {
        Set<Class<?>> result = new HashSet<>();
        result.add(Boolean  .class);
        result.add(Character.class);
        result.add(Byte     .class);
        result.add(Short    .class);
        result.add(Integer  .class);
        result.add(Long     .class);
        result.add(Float    .class);
        result.add(Double   .class);
        result.add(Void     .class);
        return result;
    }
    private static PSLClass buildClass(Class clazz, Environment environment) throws Exception {
        PSLClass cls = new PSLClass(environment);

        for (Method method: clazz.getDeclaredMethods()) {
            method.setAccessible(true);
            if (Modifier.isStatic(method.getModifiers())) {
                method.setAccessible(true);
                PSLClassField classField = new PSLClassField();
                classField.setDefaultValue(methodToValue(method, environment));
                cls.getStatics().put(method.getName(), classField);
            } else {
                ClassFieldNode classFieldNode = new ClassFieldNode();
                classFieldNode.setValue(new ValueNode(methodToValue(method, environment)));

                cls.getClassPrototype().put(method.getName(), classFieldNode);
            }
        }

        for (Field field: clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (Modifier.isStatic(field.getModifiers()) && !field.getName().equals("__NAME__")) {
                field.setAccessible(true);
                PSLClassField classField = new PSLClassField();
                classField.setDefaultValue(objectToValue(field.get(null), environment));

                cls.getStatics().put(field.getName(), classField);
            } else {
                ClassFieldNode classFieldNode = new ClassFieldNode();

                cls.getClassPrototype().put(field.getName(), classFieldNode);
            }
        }

        for (Class clas: clazz.getDeclaredClasses()) {
            if (Modifier.isStatic(clas.getModifiers())) {
                PSLClassField classField = new PSLClassField();
                classField.setDefaultValue(buildClass(clas, environment));

                cls.getStatics().put(clas.getSimpleName(), classField);
            } else {
                ClassFieldNode classFieldNode = new ClassFieldNode();
                classFieldNode.setValue(new ValueNode(buildClass(clas, environment)));

                cls.getClassPrototype().put(clas.getSimpleName(), classFieldNode);
            }
        }

        ClassFieldNode constructorNode = new ClassFieldNode();
        Value result = new Value(null);
        constructorNode.setValue(new ValueNode(
                new Value(
                        new PSLFunction() {
                            @Override
                            public Value apply(Collection<Value> t) throws Exception {
                                Constructor constructor = clazz.getConstructor(t.map(e -> e.getValue().getClass()).map(
                                        e -> {
                                            if (getWrapperTypes().contains(e)) {
                                                if (e == Integer.class) return int.class;
                                                if (e == Boolean.class) return boolean.class;
                                                if (e == Double.class) return double.class;
                                                return null;
                                            } else return e;
                                        }
                                ).toArray(new Class[]{}));
                                Value instance = objectToValue(constructor.newInstance(t.map(Value::getValue).toArray()), environment);
                                result.setValue(instance.getValue());
                                result.setProperties(instance.getProperties());
                                return Value.NULL;
                            }
                        }
                )
        ));
        cls.getClassPrototype().put("constructor", constructorNode);

        return cls;
    }

    private static Value environmentToPSL(Environment environment) throws Exception {
        Value value = new Value();
        value.put("evaluate", new Value(
                new PSLFunction() {
                    @Override
                    public Value apply(Collection<Value> t) throws Exception {
                        return Evaluator.evaluate((Node) t.get(0).get("node").getValue(), environment);
                    }
                }
        ));
        value.put("define_variable", new Value(
                new PSLFunction() {
                    @Override
                    public Value apply(Collection<Value> t) throws Exception {
                        return environment.defVariable((String) t.get(0).getValue(), t.get(1)).getValue();
                    }
                }
        ));
        value.put("set_variable", new Value(
                new PSLFunction() {
                    @Override
                    public Value apply(Collection<Value> t) throws Exception {
                        return environment.setVariable((String) t.get(0).getValue(), t.get(1)).getValue();
                    }
                }
        ));
        return value;
    }
}
