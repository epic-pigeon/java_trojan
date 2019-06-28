package ParserPackage;

public abstract class SettableValue extends Value {
    private Value realValue;
    @Override
    public boolean isSettable() {
        return true;
    }

    public SettableValue(Value value) {
        if (value != null) {
            setValue(value.getValue());
            this.properties = value.getProperties();
        }
        realValue = value;
    }

    public Value getRealValue() {
        return realValue;
    }

    public void setRealValue(Value realValue) {
        this.realValue = realValue;
    }

    abstract public Value set(Value value) throws Exception;
    abstract public Value setProp(String name, Value value) throws Exception;
}
