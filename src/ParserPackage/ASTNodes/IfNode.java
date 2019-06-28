package ParserPackage.ASTNodes;

public class IfNode extends Node {
    private Node condition;
    private Node then;
    private Node otherwise;
    @Override
    public String getType() {
        return "if";
    }

    public Node getCondition() {
        return condition;
    }

    public void setCondition(Node condition) {
        this.condition = condition;
    }

    public Node getThen() {
        return then;
    }

    public void setThen(Node then) {
        this.then = then;
    }

    public Node getOtherwise() {
        return otherwise;
    }

    public void setOtherwise(Node otherwise) {
        this.otherwise = otherwise;
    }
}
