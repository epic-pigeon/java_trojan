package ParserPackage.ASTNodes;

public class ThrowNode extends Node {
    @Override
    public String getType() {
        return "throw";
    }
    private Node node;

    public ThrowNode(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}
