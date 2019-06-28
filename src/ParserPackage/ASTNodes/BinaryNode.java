package ParserPackage.ASTNodes;

public class BinaryNode extends Node {
    private Node left;
    private Node right;
    private String operator;
    @Override
    public String getType() {
        return "binary";
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
