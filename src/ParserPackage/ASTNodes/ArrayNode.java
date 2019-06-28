package ParserPackage.ASTNodes;

import ParserPackage.Collection;

public class ArrayNode extends Node {
    @Override
    public String getType() {
        return "array";
    }

    private Collection<Node> array;

    public Collection<Node> getArray() {
        return array;
    }

    public void setArray(Collection<Node> array) {
        this.array = array;
    }
}
