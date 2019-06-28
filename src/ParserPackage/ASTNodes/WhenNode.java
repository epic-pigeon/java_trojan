package ParserPackage.ASTNodes;

import ParserPackage.Collection;

public class WhenNode extends Node {
    @Override
    public String getType() {
        return "when";
    }

    private Collection<Node> conditions;
    private Collection<Node> bodies;
    private Node otherwise;

    public Collection<Node> getConditions() {
        return conditions;
    }

    public void setConditions(Collection<Node> conditions) {
        this.conditions = conditions;
    }

    public Collection<Node> getBodies() {
        return bodies;
    }

    public void setBodies(Collection<Node> bodies) {
        this.bodies = bodies;
    }

    public Node getOtherwise() {
        return otherwise;
    }

    public void setOtherwise(Node otherwise) {
        this.otherwise = otherwise;
    }
}
