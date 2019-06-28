package ParserPackage;

public class BinaryOperator {
    private BinaryOperatorAction action;
    private int precedence;

    public BinaryOperator(BinaryOperatorAction action, int precedence) {
        this.action = action;
        this.precedence = precedence;
    }

    public BinaryOperatorAction getAction() {
        return action;
    }

    public void setAction(BinaryOperatorAction action) {
        this.action = action;
    }

    public int getPrecedence() {
        return precedence;
    }

    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }
}
