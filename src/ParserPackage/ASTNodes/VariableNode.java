package ParserPackage.ASTNodes;

public class VariableNode extends Node {
    private String name;

    @Override
    public String getType() {
        return "variable";
    }

    public String getValue() {
        return name;
    }

    public void setValue(String value) {
        this.name = value;
    }

    public VariableNode(String value) {
        this.name = value;
    }
}