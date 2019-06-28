package ParserPackage.ASTNodes;

public class OperatorNode extends Node {
    @Override
    public String getType() {
        return "operator";
    }
    private String operator;
    private boolean binary;
    private Node function;
    private Node precedence;

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public Node getFunction() {
        return function;
    }

    public void setFunction(Node function) {
        this.function = function;
    }

    public Node getPrecedence() {
        return precedence;
    }

    public void setPrecedence(Node precedence) {
        this.precedence = precedence;
    }
}
