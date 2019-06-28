package ParserPackage;

import java.util.regex.Pattern;

public class KeywordRule extends Rule {
    public KeywordRule(String name, String pattern) {
        super(name, Pattern.compile(pattern + "[^0-9a-zA-Z_$]"));
    }

    @Override
    public boolean isKeyword() {
        return true;
    }
}
