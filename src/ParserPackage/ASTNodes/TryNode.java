package ParserPackage.ASTNodes;

public class TryNode extends Node {
    @Override
    public String getType() {
        return "try";
    }

    private Node toTry;
    private Node toCatch;
    private Node elseFinally;

    public Node getToTry() {
        return toTry;
    }

    public void setToTry(Node toTry) {
        this.toTry = toTry;
    }

    public Node getToCatch() {
        return toCatch;
    }

    public void setToCatch(Node toCatch) {
        this.toCatch = toCatch;
    }

    public Node getElseFinally() {
        return elseFinally;
    }

    public void setElseFinally(Node elseFinally) {
        this.elseFinally = elseFinally;
    }
}
