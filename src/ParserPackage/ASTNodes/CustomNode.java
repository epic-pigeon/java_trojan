package ParserPackage.ASTNodes;

import ParserPackage.Value;

public class CustomNode extends Node {
    @Override
    public String getType() {
        return "custom";
    }

    private Value value;
    private Node parse;

    public CustomNode(Value value, Node parse) {
        this.value = value;
        this.parse = parse;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public Node getParse() {
        return parse;
    }

    public void setParse(Node parse) {
        this.parse = parse;
    }
}
