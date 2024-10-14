package org.jabref.gui.a5experiments;

import javafx.scene.control.Label;

import org.jabref.model.entry.BibEntry;

import com.google.common.eventbus.Subscribe;

public class CoverImagePane extends Label {

    public CoverImagePane() {
        this.setText("default text");
        System.out.println("COVER PANE NEW");
    }

    @Subscribe
    public void listen(BibEntry entry) {
        this.setText(String.valueOf(entry.getCoverImage()));
    }
}
