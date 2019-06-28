package ParserPackage.ASTNodes;

import ParserPackage.Collection;

public class ProgramNode extends Node {
    private Collection<Node> program = new Collection<>();
    @Override
    public String getType() {
        return "program";
    }
    public Collection<Node> getProgram() {
        return program;
    }
    public Node addNode(Node node) {
        program.add(node);
        return node;
    }
    public void setProgram(Collection<Node> program) {
        this.program = program;
    }
}
