package ParserPackage.ASTNodes;

public class ParentedNode extends Node {
    private Node node;
    @Override
    public String getType() {
        return "parented";
    }

    public ParentedNode(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}
