package ParserPackage.ASTNodes;

public class ExportNode extends Node {
    private Node value;
    private String alias;
    @Override
    public String getType() {
        return "export";
    }

    public Node getValue() {
        return value;
    }

    public void setValue(Node value) {
        this.value = value;
    }

    public ExportNode(Node value, String alias) {
        this.value = value;
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
