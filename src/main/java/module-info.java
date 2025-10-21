module com.escuela.app.sistemaescolarfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql; // 👈 ESTA LÍNEA ES LA CLAVE

    opens com.escuela.app.sistemaescolarfx to javafx.fxml;
    exports com.escuela.app.sistemaescolarfx;

    opens com.escuela.app.sistemaescolarfx.model to javafx.fxml;
    exports com.escuela.app.sistemaescolarfx.model;

    opens com.escuela.app.sistemaescolarfx.controllers to javafx.fxml;
    exports com.escuela.app.sistemaescolarfx.controllers;

}
