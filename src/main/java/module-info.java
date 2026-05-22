module io.github.rikkakawaii0612.classtop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.prefs;
    requires java.datatransfer;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires com.fasterxml.jackson.annotation;
    requires com.sun.jna.platform;
    requires java.management;

    opens io.github.rikkakawaii0612.classtop;
    opens io.github.rikkakawaii0612.classtop.util;
    opens io.github.rikkakawaii0612.classtop.model;
}