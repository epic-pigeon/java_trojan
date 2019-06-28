package ParserPackage;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Value //extends JSONToString
    implements Serializable
{
    private static final long serialVersionUID = 13987458314534165L;
    private HashMap<String, Value> prototype;
    public boolean isSettable() {
        return false;
    }
    protected Object value;
    protected HashMap<String, Value> properties = new HashMap<>();

    public Value() {
        this(null);
    }
    public Value(Object value) {
        this(value, null);
    }

    public static final Value NULL = new Value(null);

    public Value(Object value, HashMap<String, Value> properties) {
        if (value instanceof Double) {
            if ((Double) value == ((Double) value).intValue()) {
                value = ((Double) value).intValue();
            }
        }
        setValue(value);
        if (properties != null) this.properties = properties;
    }

    public HashMap<String, Value> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, Value> properties) {
        this.properties = properties;
    }

    public Value get(String key) throws Exception {
        return properties.getOrDefault(key, prototype != null ? prototype.getOrDefault(key, Value.NULL) : Value.NULL);
    }

    public Value put(String key, Value value) throws Exception {
        return properties.put(key, value);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        if (value instanceof Number) {
            prototype = Prototypes.NUMBER;
        } else if (value instanceof String) {
            prototype = Prototypes.STRING;
        } else if (value instanceof Collection) {
            prototype = Prototypes.ARRAY;
        }
        this.value = value;
    }

    public String stringifyProps() {
        //return properties.toString();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\n");
        for (Map.Entry<String, Value> entry : properties.entrySet()) {
            stringBuilder.append(String.join("", Collections.nCopies(4, " "))).append(entry.getKey()).append(": ");
            int offset = 4 + entry.getKey().length() + 2;
            String val = entry.getValue().toString();
            Collection<String> collection = new Collection<>(val.split("\\n"));
            val = collection.get(0) + "\n" + new Collection<>(collection.subList(1, collection.size()).toArray())
                    .map(s -> String.join("", Collections.nCopies(offset, " ")) + s).join("\n");
            stringBuilder.append(val);//.append("\n");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public String toString() {
        if (value == null) {
            if (properties.isEmpty()) {
                return "null";
            } else {
                return stringifyProps();
            }
        } else {
            if (properties.isEmpty()) {
                return value.toString();
            } else {
                return value.toString() + " " + stringifyProps();
            }
        }
    }

    public void putAll(Map<? extends String, ? extends Value> m) {
        properties.putAll(m);
    }

    @Override
    public boolean equals(Object obj) {
        return equals(obj, true);
    }

    public boolean equals(Object obj, boolean strict) {
        if (obj instanceof Value) {
            Value value = (Value) obj;
            if (!compareValues(getValue(), value.getValue(), strict)) return false;
            for (Map.Entry<String, Value> entry: properties.entrySet()) {
                if (value.properties.containsKey(entry.getKey())
                 && value.properties.get(entry.getKey()).equals(entry.getValue(), strict)) {

                } else return false;
            }
            return true;
        } else return super.equals(obj);
    }
    private static boolean compareValues(Object obj1, Object obj2, boolean strict) {
        if (!strict) {
            return String.valueOf(obj1).equals(String.valueOf(obj2));
        } else {
            return Objects.equals(obj1, obj2); //&& obj1.getClass() == obj2.getClass();
        }
    }

    public HashMap<String, Value> getPrototype() {
        return prototype;
    }

    public void setPrototype(HashMap<String, Value> prototype) {
        this.prototype = prototype;
    }
}
