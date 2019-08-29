import java.net.URL;
import java.net.URLClassLoader;

public class ByteCodeClassLoader extends URLClassLoader {
    private String name;
    private byte[] bytecode;

    public ByteCodeClassLoader(String name, byte[] bytecode) {
        super(new URL[0]);
        this.name = name;
        this.bytecode = bytecode;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        byte[] classBytes = this.name.equals(name) ? bytecode : null;
        if (classBytes != null) {
            return defineClass(name, classBytes, 0, classBytes.length);
        }
        return super.findClass(name);
    }
}
