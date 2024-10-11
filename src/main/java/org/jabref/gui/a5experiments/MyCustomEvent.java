package org.jabref.gui.a5experiments;

import javafx.event.Event;
import javafx.event.EventType;

class MyCustomEvent extends Event {
    public static final EventType<MyCustomEvent> CUSTOM_EVENT = new EventType<>(Event.ANY, "CUSTOM_EVENT");
    private final String message;

    public MyCustomEvent(String message) {
        super(CUSTOM_EVENT);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
