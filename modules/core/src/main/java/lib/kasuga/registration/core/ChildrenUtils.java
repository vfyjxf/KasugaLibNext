package lib.kasuga.registration.core;

import lib.kasuga.registration.Reg;
import lib.kasuga.registration.core.IAdaptedObject;
import lib.kasuga.registration.core.IChildrenConfiguration;

import java.util.function.Consumer;
import java.util.function.Function;

public class ChildrenUtils {
    public static void traverse(IChildrenConfiguration<?> entry, Consumer<Reg<?, ?>> consumer) {

        Object oEntry = entry;

        while(oEntry instanceof IAdaptedObject<?> adapted) {
            oEntry = adapted.getOriginal();
        }

        if(oEntry instanceof Reg<?, ?> reg) {
            consumer.accept(reg);
        }

        for (Reg<?, ?> child : entry.getChildren()) {
            traverse(child, consumer);
        }
    }

    public static void traverse(Reg<?, ?> entry, Consumer<Reg<?, ?>> consumer) {
        consumer.accept(entry);
        for (Reg<?, ?> child : entry.getChildren()) {
            traverse(child, consumer);
        }
    }

    public static <R> R traverseRI(IChildrenConfiguration<?> entry, Function<Reg<?, ?>, R> function) {

        Object oEntry = entry;

        while(oEntry instanceof IAdaptedObject<?> adapted) {
            oEntry = adapted.getOriginal();
        }

        if(oEntry instanceof Reg<?, ?> reg) {
            R r = function.apply(reg);
            if(r != null)
                return r;
        }

        for (Reg<?, ?> child : entry.getChildren()) {
            R r = traverseRI(child, function);
            if(r != null)
                return r;
        }

        return null;
    }

    public static <R> R traverseRI(Reg<?, ?> entry, Function<Reg<?, ?>, R> consumer) {
        R r = consumer.apply(entry);
        if(r != null)
            return r;
        for (Reg<?, ?> child : entry.getChildren()) {
            R _r = traverseRI(child, consumer);
            if(_r != null)
                return _r;
        }
        return null;
    }
}
