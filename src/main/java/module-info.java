module edu.vidura.inclass {
    requires javafx.controls;
    requires javafx.fxml;


    opens edu.vidura.inclass.controller to javafx.fxml;
    exports edu.vidura.inclass;
}