package ParserPackage.ASTNodes;

public class ClassFieldNode extends Node {
    private Node value;
    private Node getAction;
    private Node setAction;
    private boolean isStatic;

    @Override
    public String getType() {
        return "field";
    }

    public Node getValue() {
        return value;
    }

    public void setValue(Node value) {
        this.value = value;
    }

    public Node getGetAction() {
        return getAction;
    }

    public void setGetAction(Node getAction) {
        this.getAction = getAction;
    }

    public Node getSetAction() {
        return setAction;
    }

    public void setSetAction(Node setAction) {
        this.setAction = setAction;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }
}
