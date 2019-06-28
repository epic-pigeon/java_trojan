package ParserPackage.ASTNodes;

import ParserPackage.Collection;

public class CallNode extends Node {
    private Node function;
    private Collection<Node> arguments = new Collection<>();
    @Override
    public String getType() {
        return "call";
    }

    public Node getFunction() {
        return function;
    }

    public void setFunction(Node function) {
        this.function = function;
    }

    public Collection<Node> getArguments() {
        return arguments;
    }

    public void setArguments(Collection<Node> arguments) {
        this.arguments = arguments;
    }

    public void addArgument(Node argument) {
        this.arguments.add(argument);
    }
}
