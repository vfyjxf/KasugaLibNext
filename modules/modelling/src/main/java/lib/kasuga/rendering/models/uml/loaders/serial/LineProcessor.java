package lib.kasuga.rendering.models.uml.loaders.serial;

import lombok.Getter;

import java.util.function.Predicate;

public abstract class LineProcessor<I extends ContextData<I>> {

    public void input(String line, SerialContext<I> context) {
        if (!isValidInput(line, context)) return;
        process(line, context);
    }

    public abstract void process(String line, SerialContext<I> context);

    public abstract boolean isValidInput(String line, SerialContext<I> context);
}
