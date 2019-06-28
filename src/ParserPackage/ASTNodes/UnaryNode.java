package ParserPackage.ASTNodes;

public class UnaryNode extends Node {
    @Override
    public String getType() {
        return "unary";
    }
    private String operator;
    private Node value;

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Node getValue() {
        return value;
    }

    public void setValue(Node value) {
        this.value = value;
    }
}
