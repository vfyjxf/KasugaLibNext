package lib.kasuga.scripting.discovery;

import java.io.IOException;

public class PackageLoadingError extends RuntimeException{
    public PackageLoadingError(IOException e) {
        super(e);
    }

    public PackageLoadingError(String s) {
        super(s);
    }
}
