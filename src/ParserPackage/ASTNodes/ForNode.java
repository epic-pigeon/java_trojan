package ParserPackage.ASTNodes;

public class ForNode extends Node{
    @Override
    public String getType() {
        return "for";
    }

    private String name;
    private Node collection;
    private Node body;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Node getCollection() {
        return collection;
    }

    public void setCollection(Node collection) {
        this.collection = collection;
    }

    public Node getBody() {
        return body;
    }

    public void setBody(Node body) {
        this.body = body;
    }
}
