package ParserPackage;

import ParserPackage.ASTNodes.Node;

@FunctionalInterface
public interface UnaryOperatorAction {
    Value apply(Node node, Environment environment) throws Exception;
}
