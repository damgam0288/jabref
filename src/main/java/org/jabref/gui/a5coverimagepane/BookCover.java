package org.jabref.gui.a5coverimagepane;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class BookCover extends StackPane {

    private ImageView imageView;
    private Image image;

    public BookCover() {
        imageView = new ImageView();
    }

}
