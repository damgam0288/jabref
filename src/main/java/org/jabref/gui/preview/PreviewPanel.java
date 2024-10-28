package org.jabref.gui.preview;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.jabref.gui.DialogService;
import org.jabref.gui.externalfiles.ExternalFilesEntryLinker;
import org.jabref.gui.icon.IconTheme;
import org.jabref.gui.keyboard.KeyBinding;
import org.jabref.gui.keyboard.KeyBindingRepository;
import org.jabref.gui.preferences.GuiPreferences;
import org.jabref.gui.theme.ThemeManager;
import org.jabref.gui.util.OptionalObjectProperty;
import org.jabref.logic.l10n.Localization;
import org.jabref.logic.preview.PreviewLayout;
import org.jabref.logic.search.LuceneManager;
import org.jabref.logic.util.TaskExecutor;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.search.SearchQuery;

import com.tobiasdiez.easybind.EasyBind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreviewPanel extends HBox {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreviewPanel.class);

    private final ExternalFilesEntryLinker fileLinker;
    private final KeyBindingRepository keyBindingRepository;
    private final PreviewViewer previewView;
    private final PreviewPreferences previewPreferences;
    private final DialogService dialogService;
    private StackPane bookCover;
    private BibEntry entry;

    public PreviewPanel(BibDatabaseContext database,
                        DialogService dialogService,
                        KeyBindingRepository keyBindingRepository,
                        GuiPreferences preferences,
                        ThemeManager themeManager,
                        TaskExecutor taskExecutor,
                        LuceneManager luceneManager,
                        OptionalObjectProperty<SearchQuery> searchQueryProperty) {
        this.keyBindingRepository = keyBindingRepository;
        this.dialogService = dialogService;
        this.previewPreferences = preferences.getPreviewPreferences();
        this.fileLinker = new ExternalFilesEntryLinker(preferences.getExternalApplicationsPreferences(), preferences.getFilePreferences(), database, dialogService);
        this.bookCover = new StackPane();

        PreviewPreferences previewPreferences = preferences.getPreviewPreferences();
        previewView = new PreviewViewer(database, dialogService, preferences, themeManager, taskExecutor, searchQueryProperty);
        previewView.setLayout(previewPreferences.getSelectedPreviewLayout());
        previewView.setContextMenu(createPopupMenu());
        previewView.setOnDragDetected(event -> {
            previewView.startFullDrag();

            Dragboard dragboard = previewView.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putHtml(previewView.getSelectionHtmlContent());
            dragboard.setContent(content);

            event.consume();
        });

        previewView.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY, TransferMode.MOVE, TransferMode.LINK);
            }
            event.consume();
        });

        previewView.setOnDragDropped(event -> {
            boolean success = false;
            if (event.getDragboard().hasContent(DataFormat.FILES)) {
                List<Path> files = event.getDragboard().getFiles().stream().map(File::toPath).collect(Collectors.toList());

                if (event.getTransferMode() == TransferMode.MOVE) {
                    LOGGER.debug("Mode MOVE"); // shift on win or no modifier
                    fileLinker.moveFilesToFileDirRenameAndAddToEntry(entry, files, luceneManager);
                }
                if (event.getTransferMode() == TransferMode.LINK) {
                    LOGGER.debug("Node LINK"); // alt on win
                    fileLinker.addFilesToEntry(entry, files);
                }
                if (event.getTransferMode() == TransferMode.COPY) {
                    LOGGER.debug("Mode Copy"); // ctrl on win, no modifier on Xubuntu
                    fileLinker.copyFilesToFileDirAndAddToEntry(entry, files, luceneManager);
                }
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });

        this.getChildren().addFirst(previewView);
        HBox.setHgrow(previewView,Priority.ALWAYS);
        this.getChildren().addLast(bookCover);

        createKeyBindings();
        previewView.setLayout(previewPreferences.getSelectedPreviewLayout());

    }

    private void createKeyBindings() {
        previewView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            Optional<KeyBinding> keyBinding = keyBindingRepository.mapToKeyBinding(event);
            if (keyBinding.isPresent()) {
                if (keyBinding.get() == KeyBinding.COPY_PREVIEW) {
                    previewView.copyPreviewToClipBoard();
                    event.consume();
                }
            }
        });
    }

    private ContextMenu createPopupMenu() {
        MenuItem copyPreview = new MenuItem(Localization.lang("Copy preview"), IconTheme.JabRefIcons.COPY.getGraphicNode());
        keyBindingRepository.getKeyCombination(KeyBinding.COPY_PREVIEW).ifPresent(copyPreview::setAccelerator);
        copyPreview.setOnAction(event -> previewView.copyPreviewToClipBoard());
        MenuItem copySelection = new MenuItem(Localization.lang("Copy selection"));
        copySelection.setOnAction(event -> previewView.copySelectionToClipBoard());
        MenuItem printEntryPreview = new MenuItem(Localization.lang("Print entry preview"), IconTheme.JabRefIcons.PRINTED.getGraphicNode());
        printEntryPreview.setOnAction(event -> previewView.print());
        MenuItem previousPreviewLayout = new MenuItem(Localization.lang("Previous preview layout"));
        keyBindingRepository.getKeyCombination(KeyBinding.PREVIOUS_PREVIEW_LAYOUT).ifPresent(previousPreviewLayout::setAccelerator);
        previousPreviewLayout.setOnAction(event -> this.previousPreviewStyle());
        MenuItem nextPreviewLayout = new MenuItem(Localization.lang("Next preview layout"));
        keyBindingRepository.getKeyCombination(KeyBinding.NEXT_PREVIEW_LAYOUT).ifPresent(nextPreviewLayout::setAccelerator);
        nextPreviewLayout.setOnAction(event -> this.nextPreviewStyle());

        ContextMenu menu = new ContextMenu();
        menu.getItems().add(copyPreview);
        menu.getItems().add(copySelection);
        menu.getItems().add(printEntryPreview);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(nextPreviewLayout);
        menu.getItems().add(previousPreviewLayout);
        return menu;
    }

    public void setEntry(BibEntry entry) {
        // TODO CONTINUE HERE
        System.out.println("org.jabref.gui.preview.PreviewPanel.setEntry");
        this.entry = entry;
        setBookCover(entry);

        previewView.setEntry(entry);
        previewView.setLayout(previewPreferences.getSelectedPreviewLayout());
    }

    // A5 tests
    private void setBookCover(BibEntry entry) {
        // Clear previous content to avoid adding multiple images
        this.bookCover.getChildren().clear();

        // TODO CONTINUE HERE - make the image view just a fixed size, and
        //  maybe make it centered
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setImage(entry.getCoverImage());

        // Bind the imageView's height to the bookCover's height to ensure it resizes properly
        imageView.fitHeightProperty().bind(this.heightProperty());
        imageView.fitWidthProperty().bind(this.widthProperty());

        // Ensure the ImageView resizes when the HBox shrinks
        imageView.setSmooth(true);

        // Add the imageView to the bookCover StackPane and set its alignment
        this.bookCover.getChildren().add(imageView);
        this.bookCover.setAlignment(Pos.BASELINE_CENTER);
    }

    public void print() {
        previewView.print();
    }

    public void nextPreviewStyle() {
        cyclePreview(previewPreferences.getLayoutCyclePosition() + 1);
    }

    public void previousPreviewStyle() {
        cyclePreview(previewPreferences.getLayoutCyclePosition() - 1);
    }

    private void cyclePreview(int newPosition) {
        previewPreferences.setLayoutCyclePosition(newPosition);

        PreviewLayout layout = previewPreferences.getSelectedPreviewLayout();
        previewView.setLayout(layout);
        dialogService.notify(Localization.lang("Preview style changed to: %0", layout.getDisplayName()));
    }
}
