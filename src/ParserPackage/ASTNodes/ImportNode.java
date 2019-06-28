package ParserPackage.ASTNodes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ImportNode extends Node {
    private HashMap<String, String> imports = new HashMap<>();
    private Node filename;
    private boolean built;
    private boolean native1;

    public boolean isNative() {
        return native1;
    }

    public void setNative(boolean native1) {
        this.native1 = native1;
    }

    public boolean isBuilt() {
        return built;
    }

    public void setBuilt(boolean built) {
        this.built = built;
    }

    @Override
    public String getType() {
        return "import";
    }

    public HashMap<String, String> getImports() {
        return imports;
    }

    public void setImports(HashMap<String, String> imports) {
        this.imports = imports;
    }

    public Node getFilename() {
        return filename;
    }

    public void setFilename(Node filename) {
        this.filename = filename;
    }

    public int size() {
        return imports.size();
    }

    public boolean isEmpty() {
        return imports.isEmpty();
    }

    public String get(Object key) {
        return imports.get(key);
    }

    public boolean containsKey(Object key) {
        return imports.containsKey(key);
    }

    public String put(String key, String value) {
        return imports.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends String> m) {
        imports.putAll(m);
    }

    public String remove(Object key) {
        return imports.remove(key);
    }

    public void clear() {
        imports.clear();
    }

    public boolean containsValue(Object value) {
        return imports.containsValue(value);
    }

    public Set<String> keySet() {
        return imports.keySet();
    }

    public Collection<String> values() {
        return imports.values();
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return imports.entrySet();
    }

    public String getOrDefault(Object key, String defaultValue) {
        return imports.getOrDefault(key, defaultValue);
    }

    public String putIfAbsent(String key, String value) {
        return imports.putIfAbsent(key, value);
    }

    public boolean remove(Object key, Object value) {
        return imports.remove(key, value);
    }

    public boolean replace(String key, String oldValue, String newValue) {
        return imports.replace(key, oldValue, newValue);
    }

    public String replace(String key, String value) {
        return imports.replace(key, value);
    }

    public String computeIfAbsent(String key, Function<? super String, ? extends String> mappingFunction) {
        return imports.computeIfAbsent(key, mappingFunction);
    }

    public String computeIfPresent(String key, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
        return imports.computeIfPresent(key, remappingFunction);
    }

    public String compute(String key, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
        return imports.compute(key, remappingFunction);
    }

    public String merge(String key, String value, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
        return imports.merge(key, value, remappingFunction);
    }

    public void forEach(BiConsumer<? super String, ? super String> action) {
        imports.forEach(action);
    }

    public void replaceAll(BiFunction<? super String, ? super String, ? extends String> function) {
        imports.replaceAll(function);
    }

    @Override
    public Object clone() {
        return imports.clone();
    }
}
