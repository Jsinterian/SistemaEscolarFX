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

    private final ObservableList<Inscripcion> listaInscripciones = FXCollections.observableArrayList();

    // Mapa: etiqueta visible -> ID real
    private final Map<String, Integer> mapPersonas = new HashMap<>();
    private final Map<String, Integer> mapMaterias = new HashMap<>();

    @FXML
    public void initialize() {
        cargarPersonas();
        cargarMaterias();

        colId.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getIdInscripcion()).asObject());
        colAlumno.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNombrePersona()));
        colMateria.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNombreMateria()));
        colFecha.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getFechaInscripcion()));

        mostrarInscripciones();

        // Autocompletar al seleccionar una fila
        tablaInscripciones.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                cbPersona.setValue(newSel.getNombrePersona());
                cbMateria.setValue(newSel.getNombreMateria());
                dpFecha.setValue(newSel.getFechaInscripcion());
            }
        });
    }

    /** Carga alumnos desde personas_escuela (id_rol = 1) */
    private void cargarPersonas() {
        cbPersona.getItems().clear();
        mapPersonas.clear();

        String sql = """
                SELECT id_persona, nombre, apellido
                FROM personas_escuela
                WHERE id_rol = 1
                ORDER BY apellido, nombre
                """;
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                cbPersona.getItems().add(nombreCompleto);
                mapPersonas.put(nombreCompleto, rs.getInt("id_persona"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al cargar personas: " + e.getMessage()).showAndWait();
        }
    }

    /** Carga materias: usar 'descripcion' como nombre visible */
    private void cargarMaterias() {
        cbMateria.getItems().clear();
        mapMaterias.clear();

        String sql = """
                SELECT id_materia, descripcion
                FROM materias
                ORDER BY descripcion
                """;
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String nombreMateria = rs.getString("descripcion");
                cbMateria.getItems().add(nombreMateria);
                mapMaterias.put(nombreMateria, rs.getInt("id_materia"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al cargar materias: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void insertarInscripcion() {
        if (cbPersona.getValue() == null || cbMateria.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione alumno y materia.").showAndWait();
            return;
        }

        // Inserción mínima segura (evita campo fecha no existente)
        String sql = "INSERT INTO inscripciones (persona_id, materia_id) VALUES (?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mapPersonas.get(cbPersona.getValue()));
            stmt.setInt(2, mapMaterias.get(cbMateria.getValue()));
            stmt.executeUpdate();

            new Alert(Alert.AlertType.INFORMATION, "Inscripción registrada correctamente.").showAndWait();
            mostrarInscripciones();

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al inscribir: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void editarInscripcion() {
        Inscripcion sel = tablaInscripciones.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione una inscripción para editar.").showAndWait();
            return;
        }
        if (cbPersona.getValue() == null || cbMateria.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione alumno y materia.").showAndWait();
            return;
        }

        String sql = "UPDATE inscripciones SET persona_id = ?, materia_id = ? WHERE id_inscripcion = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mapPersonas.get(cbPersona.getValue()));
            stmt.setInt(2, mapMaterias.get(cbMateria.getValue()));
            stmt.setInt(3, sel.getIdInscripcion());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                new Alert(Alert.AlertType.INFORMATION, "Inscripción actualizada.").showAndWait();
            } else {
                new Alert(Alert.AlertType.WARNING, "No se encontró la inscripción.").showAndWait();
            }
            mostrarInscripciones();

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al actualizar inscripción: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void eliminarInscripcion() {
        Inscripcion sel = tablaInscripciones.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione una inscripción para eliminar.").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la inscripción de " + sel.getNombrePersona() + " en " + sel.getNombreMateria() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            String sql = "DELETE FROM inscripciones WHERE id_inscripcion = ?";
            try (Connection conn = ConexionBD.getConexion();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, sel.getIdInscripcion());
                int filas = stmt.executeUpdate();

                if (filas > 0)
                    new Alert(Alert.AlertType.INFORMATION, "Inscripción eliminada.").showAndWait();
                else
                    new Alert(Alert.AlertType.WARNING, "No se encontró la inscripción.").showAndWait();

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
                SELECT i.id_inscripcion,
                       p.id_persona,
                       m.id_materia,
                       CONCAT(p.nombre, ' ', p.apellido)         AS nombre_persona,
                       m.descripcion                              AS nombre_materia,
                       COALESCE(i.fecha_inscripcion, i.fecha, DATE(i.created_at)) AS fecha_inscripcion
                FROM inscripciones i
                JOIN personas_escuela p ON i.persona_id = p.id_persona
                JOIN materias         m ON i.materia_id = m.id_materia
                ORDER BY i.id_inscripcion DESC
                """;

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LocalDate f = rs.getDate("fecha_inscripcion") != null
                        ? rs.getDate("fecha_inscripcion").toLocalDate()
                        : null;

                listaInscripciones.add(new Inscripcion(
                        rs.getInt("id_inscripcion"),
                        rs.getInt("id_persona"),
                        rs.getInt("id_materia"),
                        rs.getString("nombre_persona"),
                        rs.getString("nombre_materia"),
                        f
                ));
            }

            tablaInscripciones.setItems(listaInscripciones);

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al mostrar inscripciones: " + e.getMessage()).showAndWait();
        }

        // Limpiar UI
        cbPersona.setValue(null);
        cbMateria.setValue(null);
        dpFecha.setValue(null);
    }
}
