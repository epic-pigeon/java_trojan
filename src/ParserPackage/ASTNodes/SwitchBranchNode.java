package ParserPackage.ASTNodes;

import ParserPackage.Collection;

public class SwitchBranchNode extends Node {
    private Collection<Node> values;
    private Node then;

    public Collection<Node> getValues() {
        return values;
    }

    public void setValues(Collection<Node> conditions) {
        this.values = conditions;
    }

    public Node getThen() {
        return then;
    }

    public void setThen(Node then) {
        this.then = then;
    }

    @Override
    public String getType() {
        return "switchBranch";
    }
}
