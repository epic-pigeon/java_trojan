package ParserPackage.ASTNodes;

public class WhileNode extends Node {
    @Override
    public String getType() {
        return "while";
    }
    private Node condition;
    private Node body;

    public Node getCondition() {
        return condition;
    }

    public void setCondition(Node condition) {
        this.condition = condition;
    }

    public Node getBody() {
        return body;
    }

    public void setBody(Node body) {
        this.body = body;
    }
}
