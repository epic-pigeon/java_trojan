package ParserPackage.ASTNodes;

import ParserPackage.Value;

public class ValueNode extends Node {
    private Value value;
    @Override
    public String getType() {
        return "value";
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public ValueNode(Value value) {
        this.value = value;
    }
}
