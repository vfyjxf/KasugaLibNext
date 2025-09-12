package lib.kasuga.inject;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

public class CombinedClassLoader extends ClassLoader {
    private final ClassLoader[] delegates;

    public CombinedClassLoader(ClassLoader... delegates) {
        super(null);
        this.delegates = delegates;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        for (ClassLoader cl : delegates) {
            try {
                return cl.loadClass(name);
            } catch (ClassNotFoundException ignored) {}
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    public URL getResource(String name) {
        for (ClassLoader cl : delegates) {
            URL res = cl.getResource(name);
            if (res != null) return res;
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Vector<URL> all = new Vector<>();
        for (ClassLoader cl : delegates) {
            Enumeration<URL> urls = cl.getResources(name);
            while (urls.hasMoreElements()) {
                all.add(urls.nextElement());
            }
        }
        return all.elements();
    }
}
