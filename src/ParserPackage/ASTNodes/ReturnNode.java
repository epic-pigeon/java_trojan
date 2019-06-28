package ParserPackage.ASTNodes;

public class ReturnNode extends Node {
    private Node value;
    @Override
    public String getType() {
        return "return";
    }

    public Node getValue() {
        return value;
    }

    public void setValue(Node value) {
        this.value = value;
    }

    public ReturnNode(Node value) {
        this.value = value;
    }
}
