package ParserPackage.ASTNodes;

import ParserPackage.Collection;

public class BodyNode extends Node {
    private Collection<Node> expressions;
    @Override
    public String getType() {
        return "body";
    }

    public Collection<Node> getExpressions() {
        return expressions;
    }

    public void setExpressions(Collection<Node> expressions) {
        this.expressions = expressions;
    }

    public void addExpression(Node node) {
        expressions.add(node);
    }
}
