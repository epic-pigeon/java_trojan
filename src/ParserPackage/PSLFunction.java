package ParserPackage;

import java.io.Serializable;

public abstract class PSLFunction implements Serializable {
    public Value apply(Collection<Value> t) throws Exception {
        return apply(t, null);
    }
    public Value apply(Collection<Value> t, Environment environment) throws Exception {
        return apply(t);
    }
    public String toString() {
        return "[native function]";
    }
}
