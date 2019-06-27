import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Map;

public class Data {
    private String data;
    private Map<String, Object> map;
    private String type;

    public Data(String data) throws ParseException {
        this.data = data;
        map = (JSONObject) new JSONParser().parse(data);
        type = (String) map.get("type");
    }

    public String getData() {
        return data;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return data;
    }
}
