module io.github.rikkakawaii0612.classtop {
    requires javafx.controls;
    requires javafx.fxml;


    opens io.github.rikkakawaii0612.classtop to javafx.fxml;
    exports io.github.rikkakawaii0612.classtop;
}