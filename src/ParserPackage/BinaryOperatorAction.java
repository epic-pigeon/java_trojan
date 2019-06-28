package ParserPackage;

import ParserPackage.ASTNodes.Node;

@FunctionalInterface
public interface BinaryOperatorAction {
    Value apply(Node value1, Node value2, Environment environment) throws Exception;
}
