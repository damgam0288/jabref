package org.jabref.gui.a5experiments;

import org.jabref.model.entry.BibEntry;

public class DummyObject {
    private int value;
    private BibEntry entry;

    public DummyObject(int value) {
        this.value = value;
    }

    public DummyObject(BibEntry entry) {
        this.entry = entry;
    }

    public int getValue() {
        return value;
    }
}
