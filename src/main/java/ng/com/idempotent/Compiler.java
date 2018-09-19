package ng.com.idempotent;

import java.io.File;
import java.io.InvalidClassException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author aardvocate
 */
public final class Compiler {

    /**
     * @param source the file containing the source code e.g
     * /home/dev/src/java/com/test/sjb/HelloWorld.java
     * @param fullyQualifiedClassName the fully qualified name of the class e.g
     * com.test.sjb.HelloWorld
     * @return a HashMap containing the base64 encoded string of the instance of
     * the class and a json docs of the name of the class, methods and
     * parameters. The returned String will be uploaded to the blockchain.
     * @throws Exception
     */
    public static final HashMap<String, Object> compile(File source, String fullyQualifiedClassName) throws Exception {
        String sourceCode = new String(Files.readAllBytes(Paths.get(source.getAbsolutePath())));
        return compile(sourceCode, fullyQualifiedClassName);
    }

    /**
     *
     * @param sourceCode a String containing the source code
     * @param fullyQualifiedClassName the fully qualified name of the class e.g
     * com.test.sjb.HelloWorld
     * @return a HashMap containing the base64 encoded string of the instance of
     * the class and a json docs of the name of the class, methods and
     * parameters. The returned String will be uploaded to the blockchain.
     * @throws Exception
     */
    public static final HashMap<String, Object> compile(String sourceCode, String fullyQualifiedClassName) throws Exception {
        InMemoryJavaCompiler compiler = InMemoryJavaCompiler.newInstance();
        Class<?> compiled = compiler.compile(fullyQualifiedClassName, sourceCode);

        Class[] interfaces = compiled.getInterfaces();
        boolean hasSmartBean = false;
        for (Class c : interfaces) {
            if (c.getName().contains("SmartBean")) {
                hasSmartBean = true;
                break;
            }
        }

        if (!hasSmartBean) {
            throw new InvalidClassException(fullyQualifiedClassName + " does not implement the SmartBean Interface. All Smart Classes must implement the smart bean interface");
        }

        Field f = compiled.getDeclaredField("serialVersionUID");
        System.out.println("Found Field: [" + f +"]");
        if (f == null) {
            throw new InvalidClassException(fullyQualifiedClassName + " does not have a variable called serialVersionUID. All Smart Classes must implement have this variable");
        }
        
        try {
            int foundMods = f.getModifiers();
            
            if(!Modifier.isFinal(foundMods)) {
                throw new InvalidClassException(fullyQualifiedClassName + " serialVersionUID does not have a final modifier. Please make serialVersionUID static final");
            }
            
            if(!Modifier.isStatic(foundMods)) {
                throw new InvalidClassException(fullyQualifiedClassName + " serialVersionUID does not have a static modifier. Please make serialVersionUID static final");
            }
            
            if(!f.getType().getName().equals("long")) {
                throw new InvalidClassException(fullyQualifiedClassName + " serialVersionUID must be of type long");
            }            
        } catch(Exception e) {
            throw new InvalidClassException(e.getMessage());
        }

        CompiledCode cc = compiler.classLoader.customCompiledCode.get(fullyQualifiedClassName);
        byte[] byteCode = cc.getByteCode();

        HashMap<String, Object> docsMap = new HashMap<>();
        docsMap.put("name", fullyQualifiedClassName);

        List<HashMap<String, Object>> methodsList = new ArrayList<>();
        Method[] methods = compiled.getMethods();

        for (Method m : methods) {
            String declaringClass = m.getDeclaringClass().getName();
            if (declaringClass.equals(fullyQualifiedClassName)) {
                HashMap<String, Object> methodsMap = new HashMap<>();
                String name = m.getName();
                methodsMap.put("name", name);

                Parameter[] parameters = m.getParameters();
                List<String> parametersList = new ArrayList<>();

                for (Parameter p : parameters) {
                    parametersList.add(p.getType().getCanonicalName());
                }
                methodsMap.put("parameters", parametersList);
                methodsMap.put("returnType", m.getReturnType().getCanonicalName());
                methodsList.add(methodsMap);
            }
        }

        docsMap.put("methods", methodsList);

        HashMap<String, Object> returnValue = new HashMap<>();
        returnValue.put("byteCode", Base64.getEncoder().encodeToString(byteCode));
        returnValue.put("documentation", docsMap);

        return returnValue;
    }
}
