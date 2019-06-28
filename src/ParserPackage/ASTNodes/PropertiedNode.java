package ParserPackage.ASTNodes;

import java.util.HashMap;

public class PropertiedNode extends Node {
    private boolean override;
    private Node node;
    private HashMap<String, Node> properties;
    @Override
    public String getType() {
        return "propertied";
    }

    public PropertiedNode(boolean override, Node node, HashMap<String, Node> properties) {
        this.override = override;
        this.node = node;
        this.properties = properties;
    }

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public HashMap<String, Node> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, Node> properties) {
        this.properties = properties;
    }
}
