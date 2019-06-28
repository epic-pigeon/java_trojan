package ParserPackage.ASTNodes;

import ParserPackage.Collection;

public class FunctionNode extends Node {
    private String name;
    private Collection<ParameterNode> parameters = new Collection<>();
    private Node body;

    @Override
    public String getType() {
        return "function";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<ParameterNode> getParameters() {
        return parameters;
    }

    public void setParameters(Collection<ParameterNode> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(ParameterNode parameter) {
        parameters.add(parameter);
    }

    public Node getBody() {
        return body;
    }

    public void setBody(Node body) {
        this.body = body;
    }
}