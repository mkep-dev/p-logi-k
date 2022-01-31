module org.github.mkepdev.plogik.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.kordamp.bootstrapfx.core;

    opens org.github.mkepdev.plogik.ui to javafx.fxml;
    exports org.github.mkepdev.plogik.ui;
}