package lib.kasuga.utils;

import org.slf4j.Logger;

import java.util.HashMap;

public class ValueInspectionUtils {
    public static final ValueInspectionUtils INSTANCE = new ValueInspectionUtils();
    protected HashMap<String, Long> lastLoggedHashcode = new HashMap<>();
    public void print(String key, Object value, Runnable debugger) {
        long hashCode = value.hashCode();
        if(hashCode != lastLoggedHashcode.getOrDefault(key, 0L)) {
            debugger.run();
            lastLoggedHashcode.put(key, hashCode);
        }
    }
}
