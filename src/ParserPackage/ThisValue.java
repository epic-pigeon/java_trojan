package ParserPackage;

public class ThisValue extends Value {
    public Value get(String key) throws Exception {
        Value value = super.get(key);
        if (value == null) return null;
        if (value.getClass().equals(PSLClassField.class)) {
            if (((PSLClassField) value).getOnGet() != null) {
                return ((PSLClassField) value).getOnGet().apply(new Collection<>(value));
            }
        }
        return value;
    }

    public Value put(String key, Value setValue) throws Exception {
        Value value = super.get(key);
        if (value != null) {
            if (value.getClass().equals(PSLClassField.class)) {
                if (((PSLClassField) value).getOnSet() != null) {
                    return ((PSLClassField) value).getOnSet().apply(new Collection<>(value, setValue));
                }
            }
        }
        return super.put(key, setValue);
    }
}
