package com.escuela.app.sistemaescolarfx.controllers;

import com.escuela.app.sistemaescolarfx.model.ConexionBD;
import com.escuela.app.sistemaescolarfx.model.Inscripcion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class InscripcionController {

    @FXML private ChoiceBox<String> cbPersona;
    @FXML private ChoiceBox<String> cbMateria;
    @FXML private DatePicker dpFecha;
    @FXML private TableView<Inscripcion> tablaInscripciones;
    @FXML private TableColumn<Inscripcion, Integer> colId;
    @FXML private TableColumn<Inscripcion, String> colAlumno;
    @FXML private TableColumn<Inscripcion, String> colMateria;
    @FXML private TableColumn<Inscripcion, LocalDate> colFecha;

    private ObservableList<Inscripcion> listaInscripciones = FXCollections.observableArrayList();

    // 🔹 Mapas para relacionar nombre → ID real
    private Map<String, Integer> mapPersonas = new HashMap<>();
    private Map<String, Integer> mapMaterias = new HashMap<>();

    @FXML
    public void initialize() {
        cargarPersonas();
        cargarMaterias();

        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getIdInscripcion()).asObject());
        colAlumno.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNombrePersona()));
        colMateria.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNombreMateria()));
        colFecha.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getFechaInscripcion()));

        mostrarInscripciones();

        // Autocompletar campos al seleccionar
        tablaInscripciones.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                cbPersona.setValue(newSel.getNombrePersona());
                cbMateria.setValue(newSel.getNombreMateria());
                dpFecha.setValue(newSel.getFechaInscripcion());
            }
        });
    }

    private void cargarPersonas() {
        cbPersona.getItems().clear();
        mapPersonas.clear();
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_persona, nombre, apellido FROM persona_escuela WHERE rol='Alumno'")) {
            while (rs.next()) {
                String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                cbPersona.getItems().add(nombreCompleto);
                mapPersonas.put(nombreCompleto, rs.getInt("id_persona")); // 🔹 Mapeo
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarMaterias() {
        cbMateria.getItems().clear();
        mapMaterias.clear();
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_materia, nombre_materia FROM materias")) {
            while (rs.next()) {
                cbMateria.getItems().add(rs.getString("nombre_materia"));
                mapMaterias.put(rs.getString("nombre_materia"), rs.getInt("id_materia")); // 🔹 Mapeo
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void insertarInscripcion() {
        if (cbPersona.getValue() == null || cbMateria.getValue() == null || dpFecha.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Debe completar todos los campos.").showAndWait();
            return;
        }

        String sql = "INSERT INTO inscripciones (id_persona, id_materia, fecha_inscripcion) VALUES (?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mapPersonas.get(cbPersona.getValue()));
            stmt.setInt(2, mapMaterias.get(cbMateria.getValue()));
            stmt.setDate(3, Date.valueOf(dpFecha.getValue()));

            stmt.executeUpdate();
            new Alert(Alert.AlertType.INFORMATION, "Inscripción registrada correctamente.").showAndWait();
            mostrarInscripciones();

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Error al inscribir: " + e.getMessage()).showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    public void editarInscripcion() {
        Inscripcion seleccionada = tablaInscripciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione una inscripción para editar.").showAndWait();
            return;
        }

        String sql = "UPDATE inscripciones SET id_persona=?, id_materia=?, fecha_inscripcion=? WHERE id_inscripcion=?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mapPersonas.get(cbPersona.getValue()));
            stmt.setInt(2, mapMaterias.get(cbMateria.getValue()));
            stmt.setDate(3, Date.valueOf(dpFecha.getValue()));
            stmt.setInt(4, seleccionada.getIdInscripcion());

            int filas = stmt.executeUpdate();
            if (filas > 0)
                new Alert(Alert.AlertType.INFORMATION, "Inscripción actualizada correctamente.").showAndWait();
            else
                new Alert(Alert.AlertType.WARNING, "No se encontró la inscripción.").showAndWait();

            mostrarInscripciones();

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al actualizar inscripción: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void eliminarInscripcion() {
        Inscripcion seleccionada = tablaInscripciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione una inscripción para eliminar.").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar la inscripción de " + seleccionada.getNombrePersona() +
                        " en " + seleccionada.getNombreMateria() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            String sql = "DELETE FROM inscripciones WHERE id_inscripcion=?";
            try (Connection conn = ConexionBD.getConexion();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, seleccionada.getIdInscripcion());
                int filas = stmt.executeUpdate();

                if (filas > 0)
                    new Alert(Alert.AlertType.INFORMATION, "Inscripción eliminada correctamente.").showAndWait();
                else
                    new Alert(Alert.AlertType.WARNING, "No se encontró la inscripción para eliminar.").showAndWait();

                mostrarInscripciones();

            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Error al eliminar inscripción: " + e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    public void mostrarInscripciones() {
        listaInscripciones.clear();
        String sql = """
                SELECT i.id_inscripcion, p.id_persona, m.id_materia,
                       CONCAT(p.nombre, ' ', p.apellido) AS nombre_persona,
                       m.nombre_materia, i.fecha_inscripcion
                FROM inscripciones i
                JOIN persona_escuela p ON i.id_persona = p.id_persona
                JOIN materias m ON i.id_materia = m.id_materia
                """;

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                listaInscripciones.add(new Inscripcion(
                        rs.getInt("id_inscripcion"),
                        rs.getInt("id_persona"),
                        rs.getInt("id_materia"),
                        rs.getString("nombre_persona"),
                        rs.getString("nombre_materia"),
                        rs.getDate("fecha_inscripcion").toLocalDate()
                ));
            }

            tablaInscripciones.setItems(listaInscripciones);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Limpiar campos visuales
        cbPersona.setValue(null);
        cbMateria.setValue(null);
        dpFecha.setValue(null);
    }
}
