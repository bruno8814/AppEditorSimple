module com.example.appeditorsimple {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires jdk.jsobject;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires vosk;

    opens com.example.appeditorsimple to javafx.fxml;
    opens com.example.appeditorsimple.nui to javafx.fxml;

    exports com.example.appeditorsimple;
    exports com.example.appeditorsimple.nui;
}