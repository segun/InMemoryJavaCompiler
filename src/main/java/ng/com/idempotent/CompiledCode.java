package ng.com.idempotent;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by trung on 5/3/15.
 */
public final class CompiledCode extends SimpleJavaFileObject {

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private String className;

    public CompiledCode(String className) throws Exception {
        super(new URI(className), Kind.CLASS);
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return baos;
    }

    public byte[] getByteCode() {
        return baos.toByteArray();
    }

    public void setByteCode(byte[] byteCode) {
        baos = new ByteArrayOutputStream();
        try {
            baos.write(byteCode);
        } catch (IOException ex) {
            Logger.getAnonymousLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
