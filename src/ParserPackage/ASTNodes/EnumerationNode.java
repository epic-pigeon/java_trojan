package ParserPackage.ASTNodes;

import ParserPackage.Collection;

public class EnumerationNode extends Node {
    private Collection<Node> nodes;
    @Override
    public String getType() {
        return "enumeration";
    }

    public EnumerationNode(Collection<Node> nodes) {
        this.nodes = nodes;
    }

    public Collection<Node> getNodes() {
        return nodes;
    }

    public void setNodes(Collection<Node> nodes) {
        this.nodes = nodes;
    }
}
