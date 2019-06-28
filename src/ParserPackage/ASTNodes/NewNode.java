package ParserPackage.ASTNodes;

import ParserPackage.Collection;

public class NewNode extends Node {
    private Node clazz;
    private Collection<Node> arguments;
    @Override
    public String getType() {
        return "new";
    }

    public Node getClazz() {
        return clazz;
    }

    public void setClazz(Node clazz) {
        this.clazz = clazz;
    }

    public Collection<Node> getArguments() {
        return arguments;
    }

    public void setArguments(Collection<Node> arguments) {
        this.arguments = arguments;
    }
}
