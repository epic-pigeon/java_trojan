package ParserPackage;

public class PSLException extends Exception {
    private Value value;

    public PSLException(Value value) {
        super("Uncaught " + value);
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}
