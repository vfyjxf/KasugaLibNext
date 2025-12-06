package lib.kasuga.modelling.core.event;
import lib.kasuga.modelling.core.element.Element;
import lombok.Getter;

import java.util.List;

public abstract class Event {

    @Getter
    protected final boolean cancelable;

    @Getter
    protected boolean canceled;

    protected final List<Element> eventPath;


    public Event(boolean cancelable, List<Element> eventPath) {
        this.cancelable = cancelable;
        this.eventPath = eventPath;
    }

    public void stopPropagation() {
        if(cancelable)
            canceled = true;
    }

    public void emitAsPath() {
        for (Element domElement : this.eventPath) {
            domElement.dispatchEvent(this, true);
            if(canceled)
                return;
        }

        for (Element domElement : this.eventPath.reversed()) {
            domElement.dispatchEvent(this);
            if(canceled)
                return;
        }
    }

    public abstract EventType<?> getType();
}
