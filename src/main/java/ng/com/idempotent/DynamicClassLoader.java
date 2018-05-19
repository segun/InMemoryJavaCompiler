package ng.com.idempotent;

import java.util.HashMap;
import java.util.Map;

public final class DynamicClassLoader extends ClassLoader {

    public final Map<String, CompiledCode> customCompiledCode = new HashMap<>();

    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    public void addCode(CompiledCode cc) {
        customCompiledCode.put(cc.getName(), cc);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        CompiledCode cc = customCompiledCode.get(name);      
        if (cc == null) {
            return super.findClass(name);
        }
        byte[] byteCode = cc.getByteCode();
        return defineClass(name, byteCode, 0, byteCode.length);
    }
    
    public Class<?> getClass(String name) throws ClassNotFoundException {    
        return findClass(name);
    }
}
