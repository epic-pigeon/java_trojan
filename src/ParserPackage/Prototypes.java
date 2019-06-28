package ParserPackage;

import java.util.HashMap;

public class Prototypes {
    public static HashMap<String, Value> ARRAY = new HashMap<>();
    public static HashMap<String, Value> STRING = new HashMap<>();
    public static HashMap<String, Value> NUMBER = new HashMap<>();

    static {
        ARRAY.put("push", new Value(
                new PSLFunction() {
                    @Override
                    public Value apply(Collection<Value> t, Environment environment) throws Exception {
                        ((Collection<Value>) environment.getThiz().getValue()).addAll(t);
                        return environment.getThiz();
                    }
                }
        ));
        ARRAY.put("repeat", new Value(
                new PSLFunction() {
                    @Override
                    public Value apply(Collection<Value> t, Environment environment) throws Exception {
                        Collection<Value> thiz = (Collection<Value>) environment.getThiz().getValue();
                        return new Value(thiz.repeat(((Number) t.get(0).getValue()).intValue()));
                    }
                }
        ));
    }
}
