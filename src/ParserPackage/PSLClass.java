package ParserPackage;

import ParserPackage.ASTNodes.ClassFieldNode;

import java.util.HashMap;
import java.util.Map;

public class PSLClass extends Value {
    private Environment scope;
    private HashMap<String, ClassFieldNode> classPrototype = new HashMap<>();
    private HashMap<String, PSLClassField> statics = new HashMap<>();
    public PSLClass(Environment environment) {
        super(null);
        scope = environment.extend();
    }

    @Override
    public Value get(String key) throws Exception {
        return statics.get(key) == null ? Value.NULL : statics.get(key).get(this, scope);
    }

    @Override
    public Value put(String key, Value value) throws Exception {
        return statics.get(key).set(this, value, scope);
    }

    public Environment getScope() {
        return scope;
    }

    public void setScope(Environment scope) {
        this.scope = scope;
    }

    public HashMap<String, PSLClassField> getStatics() {
        return statics;
    }

    public void setStatics(HashMap<String, PSLClassField> statics) {
        this.statics = statics;
    }

    public Value instantiate(Collection<Value> args) throws Exception {
        Value result = new ThisValue();
        scope.setThiz(result);
        for (Map.Entry<String, ClassFieldNode> entry: classPrototype.entrySet()) {
            Value value = Evaluator.evaluate(entry.getValue(), scope);
            Value defaultValue = ((PSLClassField) value).getDefaultValue();
            value.setValue(defaultValue == null ? Value.NULL : defaultValue.getValue());
            result.put(entry.getKey(), value);
        }
        if (classPrototype.get("constructor") != null) {
            PSLClassField constructor = (PSLClassField) Evaluator.evaluate(classPrototype.get("constructor"), scope);
            ((PSLFunction) constructor.getDefaultValue().getValue()).apply(args);
        }
        return result;
    }

    public HashMap<String, ClassFieldNode> getClassPrototype() {
        return classPrototype;
    }

    public void setClassPrototype(HashMap<String, ClassFieldNode> classPrototype) {
        this.classPrototype = classPrototype;
    }

    @Override
    public HashMap<String, Value> getProperties() {
        this.properties = new HashMap<>();
        for (Map.Entry<String, PSLClassField> entry: statics.entrySet()) {
            try {
                this.properties.put(entry.getKey(), entry.getValue().get(this, scope));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return properties;
    }
}

