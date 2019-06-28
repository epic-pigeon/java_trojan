package ParserPackage.ASTNodes;

import ParserPackage.JSONToString;

import java.io.Serializable;

abstract public class Node extends JSONToString implements Serializable {
    protected String type = getType();
    abstract public String getType();
}
