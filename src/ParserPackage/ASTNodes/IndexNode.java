package ParserPackage.ASTNodes;

public class IndexNode extends Node {
    @Override
    public String getType() {
        return "index";
    }

    private Node begin;
    private Node end;
    private boolean range;
    private Node value;

    public IndexNode(Node value) {
        this.value = value;
    }

    public Node getBegin() {
        return begin;
    }

    public void setBegin(Node begin) {
        this.begin = begin;
    }

    public Node getEnd() {
        return end;
    }

    public void setEnd(Node end) {
        this.end = end;
    }

    public boolean isRange() {
        return range;
    }

    public void setRange(boolean range) {
        this.range = range;
    }

    public Node getValue() {
        return value;
    }

    public void setValue(Node value) {
        this.value = value;
    }
}
