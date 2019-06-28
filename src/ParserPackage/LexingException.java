package ParserPackage;


public class LexingException extends Exception {
    private Collection<Token> tokens;
    private int position;

    public LexingException(Collection<Token> tokens, int position) {
        super("Lexing exception on position " + position + ", tokens: " + tokens);
        this.tokens = tokens;
        this.position = position;
    }

    public Collection<Token> getTokens() {
        return tokens;
    }

    public void setTokens(Collection<Token> tokens) {
        this.tokens = tokens;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
