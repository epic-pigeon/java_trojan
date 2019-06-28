package ParserPackage;

public class UnaryOperator {
    private UnaryOperatorAction action;

    public UnaryOperator(UnaryOperatorAction action) {
        this.action = action;
    }

    public UnaryOperatorAction getAction() {
        return action;
    }

    public void setAction(UnaryOperatorAction action) {
        this.action = action;
    }
}
