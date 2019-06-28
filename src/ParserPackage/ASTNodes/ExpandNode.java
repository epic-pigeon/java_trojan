package ParserPackage.ASTNodes;

public class ExpandNode extends Node {
    private Node node;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public ExpandNode(Node node) {
        this.node = node;
    }

    @Override
    public String getType() {
        return "expand";
    }
}
