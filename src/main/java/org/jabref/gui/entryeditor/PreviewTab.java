package org.jabref.gui.entryeditor;

import org.jabref.gui.icon.IconTheme;
import org.jabref.gui.preview.PreviewPanel;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.preferences.PreferencesService;

public class PreviewTab extends EntryEditorTab implements OffersPreview {
    public static final String NAME = "Preview";
    private final BibDatabaseContext databaseContext;
    private final PreferencesService preferences;
    private PreviewPanel previewPanel;

    public PreviewTab(BibDatabaseContext databaseContext,
                      PreferencesService preferences,
                      PreviewPanel previewPanel) {
        this.databaseContext = databaseContext;
        this.preferences = preferences;
        this.previewPanel = previewPanel;

        setGraphic(IconTheme.JabRefIcons.TOGGLE_ENTRY_PREVIEW.getGraphicNode());
        setText(Localization.lang("Preview"));
        setContent(previewPanel);
    }

    @Override
    public void nextPreviewStyle() {
        if (previewPanel != null) {
            previewPanel.nextPreviewStyle();
        }
    }

    @Override
    public void previousPreviewStyle() {
        if (previewPanel != null) {
            previewPanel.previousPreviewStyle();
        }
    }

    @Override
    public boolean shouldShow(BibEntry entry) {
        return preferences.getPreviewPreferences().shouldShowPreviewAsExtraTab();
    }

    @Override
    protected void bindToEntry(BibEntry entry) {
        previewPanel.setDatabase(databaseContext);
        previewPanel.setEntry(entry);
    }
}
