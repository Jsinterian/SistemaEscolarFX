package com.escuela.app.sistemaescolarfx.controllers;

import com.escuela.app.sistemaescolarfx.model.Asistencia;
import com.escuela.app.sistemaescolarfx.model.ConexionBD;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class AsistenciaController {

    @FXML private ChoiceBox<String> cbInscripcion;
    @FXML private DatePicker dpFecha; // (opcional en DB; no se usa en INSERT por compatibilidad)
    @FXML private TableView<Asistencia> tablaAsistencias;
    @FXML private TableColumn<Asistencia, String> colAlumno;
    @FXML private TableColumn<Asistencia, String> colMateria;
    @FXML private TableColumn<Asistencia, LocalDate> colFecha;
    @FXML private TableColumn<Asistencia, Boolean> colPresente;

    private final ObservableList<Asistencia> listaAsistencias = FXCollections.observableArrayList();
    private final Map<String, Integer> mapInscripciones = new HashMap<>();

    @FXML
    public void initialize() {
        cargarInscripciones();
        configurarTabla();
        mostrarAsistencias();
    }

    /** Construye "Alumno - Materia" desde inscripciones/personas_escuela/materias */
    private void cargarInscripciones() {
        cbInscripcion.getItems().clear();
        mapInscripciones.clear();

        String sql = """
                SELECT i.id_inscripcion,
                       CONCAT(p.nombre, ' ', p.apellido, ' - ', m.descripcion) AS descripcion
                FROM inscripciones i
                JOIN personas_escuela p ON i.persona_id = p.id_persona
                JOIN materias         m ON i.materia_id = m.id_materia
                ORDER BY p.apellido, p.nombre, m.descripcion
                """;
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String texto = rs.getString("descripcion");
                cbInscripcion.getItems().add(texto);
                mapInscripciones.put(texto, rs.getInt("id_inscripcion"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al cargar inscripciones: " + e.getMessage()).showAndWait();
        }
    }

    private void configurarTabla() {
        colAlumno.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAlumno()));
        colMateria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMateria()));
        colFecha.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getFechaAsistencia()));
        colPresente.setCellValueFactory(d -> {
            SimpleBooleanProperty prop = new SimpleBooleanProperty(d.getValue().isPresente());
            prop.addListener((obs, oldVal, newVal) -> actualizarAsistencia(d.getValue(), newVal));
            return prop;
        });
        colPresente.setCellFactory(CheckBoxTableCell.forTableColumn(colPresente));
    }

    @FXML
    public void registrarAsistencia() {
        if (cbInscripcion.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione una inscripción.").showAndWait();
            return;
        }

        // Inserción mínima segura (evita columna fecha inexistente)
        String sql = "INSERT INTO asistencias (id_inscripcion, presente) VALUES (?, false)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mapInscripciones.get(cbInscripcion.getValue()));
            stmt.executeUpdate();

            new Alert(Alert.AlertType.INFORMATION, "Asistencia registrada.").showAndWait();
            mostrarAsistencias();

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al registrar asistencia: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void mostrarAsistencias() {
        listaAsistencias.clear();

        String sql = """
                SELECT a.id_asistencia,
                       a.id_inscripcion,
                       CONCAT(p.nombre, ' ', p.apellido) AS alumno,
                       m.descripcion                    AS materia,
                       COALESCE(a.fecha_asistencia, a.fecha, DATE(a.created_at)) AS fecha_mostrar,
                       a.presente
                FROM asistencias a
                JOIN inscripciones     i ON a.id_inscripcion = i.id_inscripcion
                JOIN personas_escuela  p ON i.persona_id     = p.id_persona
                JOIN materias          m ON i.materia_id     = m.id_materia
                ORDER BY fecha_mostrar DESC, alumno
                """;

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Date f = rs.getDate("fecha_mostrar"); // puede venir de created_at (DATE)
                listaAsistencias.add(new Asistencia(
                        rs.getInt("id_asistencia"),
                        rs.getInt("id_inscripcion"),
                        rs.getString("alumno"),
                        rs.getString("materia"),
                        f != null ? f.toLocalDate() : null,
                        rs.getBoolean("presente")
                ));
            }

            tablaAsistencias.setItems(listaAsistencias);

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al mostrar asistencias: " + e.getMessage()).showAndWait();
        }
    }

    private void actualizarAsistencia(Asistencia asistencia, boolean presente) {
        String sql = "UPDATE asistencias SET presente = ? WHERE id_asistencia = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, presente);
            stmt.setInt(2, asistencia.getIdAsistencia());
            stmt.executeUpdate();

            asistencia.setPresente(presente);

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al actualizar asistencia: " + e.getMessage()).showAndWait();
        }
    }
}
