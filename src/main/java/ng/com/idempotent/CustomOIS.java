/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ng.com.idempotent;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 *
 * @author aardvocate
 */
public class CustomOIS extends ObjectInputStream {

    DynamicClassLoader cl;
    
    public CustomOIS(InputStream in, DynamicClassLoader cl) throws IOException {
        super(in);
        this.cl = cl;
    }

    
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        return Class.forName(desc.getName(), true, cl);
    }       
}
