package ParserPackage;

public class ReturnException extends Exception {
    private Value returnValue;

    public Value getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Value returnValue) {
        this.returnValue = returnValue;
    }

    public ReturnException(Value returnValue) {
        super("Bad return position");
        this.returnValue = returnValue;
    }
}
