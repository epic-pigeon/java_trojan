package ParserPackage;

import ParserPackage.ASTNodes.Node;
import ParserPackage.ASTNodes.ProgramNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parser {
    private static final Collection<Rule> rules = new Collection<>(
            new Rule("LEFT_PAREN", Pattern.compile("\\(")),
            new Rule("RIGHT_PAREN", Pattern.compile("\\)")),
            new Rule("LEFT_CURLY_PAREN", Pattern.compile("\\{")),
            new Rule("LEFT_SQUARE_PAREN", Pattern.compile("\\[")),
            new Rule("RIGHT_SQUARE_PAREN", Pattern.compile("]")),
            new Rule("RIGHT_CURLY_PAREN", Pattern.compile("}")),
            new Rule("COMMA", Pattern.compile(",")),
            new Rule("SINGLE_LINE_COMMENT", Pattern.compile("//[^\n]*")),
            new Rule("NUMBER", Pattern.compile("\\d+(\\.\\d+)?")),
            new Rule("STRING", Pattern.compile("('([^']|(\\\\.))*')|(\"([^\"]|(\\\\.))*\")")),
            new Rule("SEMICOLON", Pattern.compile(";")),
            new KeywordRule("FUNCTION", "function"),
            new KeywordRule("WHEN", "when"),
            new KeywordRule("NATIVE", "native"),
            new Rule("EXPAND", Pattern.compile("\\.\\.\\.")),
            new Rule("COLON", Pattern.compile(":")),
            new Rule("REFERENCE", Pattern.compile("&")),
            new KeywordRule("EXPORT", "export"),
            new KeywordRule("WHILE", "while"),
            new KeywordRule("FOR", "for"),
            new KeywordRule("IN", "in"),
            new KeywordRule("AS", "as"),
            new KeywordRule("NEW", "new"),
            new KeywordRule("IF", "if"),
            new KeywordRule("ELSE", "else"),
            new KeywordRule("IMPORT", "import"),
            new KeywordRule("BUILT", "built"),
            new KeywordRule("CLASS", "class"),
            new KeywordRule("DEFAULT", "default"),
            new KeywordRule("GET", "get"),
            new KeywordRule("INITIALIZER", "initializer"),
            new KeywordRule("SWITCH", "switch"),
            new KeywordRule("CASE", "case"),
            new KeywordRule("SET", "set"),
            new KeywordRule("OVERRIDE", "override"),
            new KeywordRule("PRECEDENCE", "precedence"),
            new KeywordRule("OPERATOR_KEYWORD", "operator"),
            new KeywordRule("BINARY", "binary"),
            new KeywordRule("UNARY", "unary"),
            new KeywordRule("STATIC", "static"),
            new KeywordRule("FROM", "from"),
            new KeywordRule("EVERYTHING", "everything"),
            new KeywordRule("RETURN", "return"),
            new KeywordRule("TRY", "try"),
            new KeywordRule("CATCH", "catch"),
            new KeywordRule("FINALLY", "finally"),
            new KeywordRule("THROW", "throw"),
            new Rule("MULTILINE_COMMENT", Pattern.compile("/\\*([^/])*\\*/"))
    );
    static {
        rules.add(new Rule("IDENTIFIER", Pattern.compile("[a-zA-Z$_][a-zA-Z&_0-9]*")));
    }
    public static Rule toSkip = new Rule("kar", Pattern.compile("\\s+"));
    public static void compile(String filename) throws Exception {
        compile(filename, filename + ".build", Environment.DEFAULT_ENVIRONMENT);
    }
    public static void compile(String filename, Environment environment) throws Exception {
        compile(filename, filename + ".build", environment);
    }
    public static void compile(String filename, String toFilename) throws Exception {
        compile(filename, toFilename, Environment.DEFAULT_ENVIRONMENT);
    }
    public static Collection<Rule> getRules(Environment environment) {
        Collection<Rule> rules = Parser.rules;
        if (environment != null) {
            Rule operatorRule = new Rule("OPERATOR", new Collection<>());
            Set<String> set = Stream.concat(
                    environment.getBinaryOperators().keySet().stream(),
                    environment.getUnaryOperators().keySet().stream()).collect(Collectors.toSet());
            for (String name : new Collection<>(set.toArray())
                    .to(String.class)
                    .qsort((o1, o2) -> o2.length() - o1.length())) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Character character : name.toCharArray()) {
                    stringBuilder.append("\\").append(character);
                }
                operatorRule.getPatterns().add(
                        Pattern.compile(stringBuilder.toString())
                );
            }

            rules.add(operatorRule);
        }
        return rules;
    }
    public static void compile(String filename, String toFilename, Environment environment) throws Exception {
        System.out.println("Reading file...");
        File file = new File(filename);
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();

        String code = new String(data, StandardCharsets.UTF_8);

        Collection<Rule> rules = getRules(environment);

        System.out.println("Lexing...");
        TokenHolder tokenHolder = new Lexer().lexFully(code, rules, toSkip);

        //System.out.println(tokenHolder);

        HashMap<String, Integer> precedence = new HashMap<>();
        for (Map.Entry<String, BinaryOperator> entry: environment.getBinaryOperators().entrySet()) {
            precedence.put(entry.getKey(), entry.getValue().getPrecedence());
        }
        System.out.println("Building AST...");
        Node program = new ASTBuilder().build(tokenHolder, precedence, 100, true);
        //System.out.println(program);
        System.out.println("Writing to file...");
        FileOutputStream fileOut = new FileOutputStream(toFilename);
        ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
        objectOut.writeObject(program);
        objectOut.close();
        System.out.println("Finished!");
    }
    public static Value run(String filename) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(filename);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        ProgramNode program = (ProgramNode) objectInputStream.readObject();
        Value result = Evaluator.evaluate(program, Environment.DEFAULT_ENVIRONMENT);
        return result;
    }
    public static void kar() {}
    public static Value interpret(String filename) throws Exception {
        return interpret(filename, Environment.DEFAULT_ENVIRONMENT);
    }
    public static Value interpret(String filename, Environment environment) throws Exception {
        File file = new File(filename);
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();

        String code = new String(data, StandardCharsets.UTF_8);

        Collection<Rule> rules = getRules(environment);

        TokenHolder tokenHolder = new Lexer().lexFully(code, rules, toSkip);

        HashMap<String, Integer> precedence = new HashMap<>();
        for (Map.Entry<String, BinaryOperator> entry: environment.getBinaryOperators().entrySet()) {
            precedence.put(entry.getKey(), entry.getValue().getPrecedence());
        }
        Node program = new ASTBuilder().build(tokenHolder, precedence, 100, false, false, environment);

        return Evaluator.evaluate(program, Environment.DEFAULT_ENVIRONMENT);
    }
}
