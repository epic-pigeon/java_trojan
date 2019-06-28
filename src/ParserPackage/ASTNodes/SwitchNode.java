package ParserPackage.ASTNodes;

import ParserPackage.Collection;

public class SwitchNode extends Node {
    @Override
    public String getType() {
        return "switch";
    }
    private Node value;
    private Collection<SwitchBranchNode> branches;

    public Collection<SwitchBranchNode> getBranches() {
        return branches;
    }

    public void setBranches(Collection<SwitchBranchNode> branches) {
        this.branches = branches;
    }

    public Node getValue() {
        return value;
    }

    public void setValue(Node value) {
        this.value = value;
    }
}

