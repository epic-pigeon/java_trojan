package ParserPackage.ASTNodes;

import java.util.HashMap;

public class ClassNode extends Node {
    private String name;
    private HashMap<String, ClassFieldNode> fields;
    @Override
    public String getType() {
        return "class";
    }

    public HashMap<String, ClassFieldNode> getFields() {
        return fields;
    }

    public void setFields(HashMap<String, ClassFieldNode> fields) {
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
