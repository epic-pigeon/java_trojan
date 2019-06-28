package ParserPackage.ASTNodes;

public class IdentifierNode extends Node {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return "identifier";
    }
}
