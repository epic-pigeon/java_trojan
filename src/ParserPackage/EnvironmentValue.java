package ParserPackage;

public class EnvironmentValue extends Value {
    private Environment environment;

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public EnvironmentValue(Value value, Environment environment) {
        super(value.getValue());
        setProperties(value.getProperties());
        setEnvironment(environment);
    }
}
