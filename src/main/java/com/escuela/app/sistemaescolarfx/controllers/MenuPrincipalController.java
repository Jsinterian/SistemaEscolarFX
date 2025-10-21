package com.escuela.app.sistemaescolarfx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MenuPrincipalController {

    @FXML
    public void abrirPersonas() throws IOException {
        cambiarVista("/views/persona_view.fxml", "Gestión de Personas");
    }

    @FXML
    public void abrirMaterias() throws IOException {
        cambiarVista("/views/materias_view.fxml", "Gestión de Materias");
    }

    @FXML
    public void abrirInscripciones() throws IOException {
        cambiarVista("/views/inscripciones_view.fxml", "Gestión de Inscripciones");
    }

    @FXML
    public void abrirAsistencias() throws IOException {
        cambiarVista("/views/asistencias_view.fxml", "Gestión de Asistencias");
    }

    private void cambiarVista(String rutaFXML, String titulo) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        stage.setTitle(titulo);
        stage.setScene(scene);
        stage.show();
    }
}
