package lib.kasuga.scripting.module;

public class DuplicatePackageException extends RuntimeException {
    private final String packageName;
    private final String existingSource;

    public DuplicatePackageException(String packageName, String existingSource) {
        super("Duplicate package name: " + packageName + " (already registered from " + existingSource + ")");
        this.packageName = packageName;
        this.existingSource = existingSource;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getExistingSource() {
        return existingSource;
    }
}
