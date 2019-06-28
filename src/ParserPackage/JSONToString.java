package ParserPackage;

import org.json.simple.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class JSONToString {
    public String toString() {
        JSONObject object = new JSONObject();
        Collection<Field> fields = new Collection<>();
        Class clazz = getClass();
        do {
            fields.addAll(new Collection<>(clazz.getDeclaredFields()));
        } while ((clazz = clazz.getSuperclass()) != null);

        fields = fields.reverse();

        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                try {
                    field.setAccessible(true);
                    object.put(field.getName(), field.get(this));
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
        //System.out.println(getClass().getName() + object.keySet());
        return object.toJSONString();
    }
}
