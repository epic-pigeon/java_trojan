package ParserPackage;

public class PSLClassField extends Value {
    public PSLClassField() {
        super(null);
    }
    private Value value;
    private PSLFunction onGet;
    private PSLFunction onSet;
    public Value get(Value clazz, Environment scope) throws Exception {
            if (onGet == null) {
                return value;
            } else {
                return onGet.apply(new Collection<>());
            }
    }
    public Value set(Value clazz, Value val, Environment scope) throws Exception {
            if (onSet == null) {
                return value = val;
            } else {
                return onSet.apply(new Collection<>(val));
            }
    }

    @Override
    public Object getValue() {
        return super.value;
    }

    @Override
    public void setValue(Object value) {
        super.value = value;
    }

    public Value getDefaultValue() {
        return this.value;
    }

    public void setDefaultValue(Value value) {
        this.value = value;
    }

    public PSLFunction getOnGet() {
        return onGet;
    }

    public void setOnGet(PSLFunction onGet) {
        this.onGet = onGet;
    }

    public PSLFunction getOnSet() {
        return onSet;
    }

    public void setOnSet(PSLFunction onSet) {
        this.onSet = onSet;
    }
}
