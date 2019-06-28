package ParserPackage;

import java.util.regex.Pattern;

public class Rule {
    private Collection<Pattern> patterns = new Collection<>();
    private String name;
    private String parse;
    private String runtimeParse;

    public Rule(String name, Pattern pattern) {
        this.patterns.add(pattern);
        this.name = name;
    }

    public Rule(Collection<Pattern> patterns, String name) {
        this.patterns = patterns;
        this.name = name;
    }

    public Rule(String name, Collection<String> patterns) {
        this.patterns = new Collection<>();
        for (String pattern: patterns) this.patterns.add(Pattern.compile(pattern));
        this.name = name;
    }

    public Rule(String name, String regex) {
        patterns.add(Pattern.compile(regex));
        this.name = name;
    }

    public Rule(Pattern pattern) {
        this.patterns.add(pattern);
    }

    public Rule(String pattern) {
        this.patterns.add(Pattern.compile(pattern));
    }

    public Collection<Pattern> getPatterns() {
        return patterns;
    }

    public void addPattern(Pattern pattern) {
        this.patterns.add(pattern);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPatterns(Collection<Pattern> patterns) {
        this.patterns = patterns;
    }

    public String getParse() {
        return parse;
    }

    public void setParse(String parse) {
        this.parse = parse;
    }

    public String getRuntimeParse() {
        return runtimeParse;
    }

    public void setRuntimeParse(String runtimeParse) {
        this.runtimeParse = runtimeParse;
    }

    public boolean isKeyword() {
        return false;
    }

    @Override
    public String toString() {
        return name + ", " + patterns;
    }
}