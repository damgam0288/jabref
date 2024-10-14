package org.jabref.gui.a5experiments;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class Listener {

    @Subscribe
    public void listen(DummyObject object) {
        System.out.println(object.getValue());
    }

}
