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
    @FXML private DatePicker dpFecha;
    @FXML private TableView<Asistencia> tablaAsistencias;
    @FXML private TableColumn<Asistencia, String> colAlumno;
    @FXML private TableColumn<Asistencia, String> colMateria;
    @FXML private TableColumn<Asistencia, LocalDate> colFecha;
    @FXML private TableColumn<Asistencia, Boolean> colPresente;

    private ObservableList<Asistencia> listaAsistencias = FXCollections.observableArrayList();
    private Map<String, Integer> mapInscripciones = new HashMap<>();

    @FXML
    public void initialize() {
        cargarInscripciones();
        configurarTabla();
        mostrarAsistencias();
    }

    private void cargarInscripciones() {
        cbInscripcion.getItems().clear();
        mapInscripciones.clear();

        String sql = """
                SELECT i.id_inscripcion, CONCAT(p.nombre, ' ', p.apellido, ' - ', m.nombre_materia) AS descripcion
                FROM inscripciones i
                JOIN persona_escuela p ON i.id_persona = p.id_persona
                JOIN materias m ON i.id_materia = m.id_materia
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
        }
    }

    private void configurarTabla() {
        colAlumno.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAlumno()));
        colMateria.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMateria()));
        colFecha.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getFechaAsistencia()));
        colPresente.setCellValueFactory(data -> {
            SimpleBooleanProperty prop = new SimpleBooleanProperty(data.getValue().isPresente());
            prop.addListener((obs, oldVal, newVal) -> actualizarAsistencia(data.getValue(), newVal));
            return prop;
        });

        colPresente.setCellFactory(CheckBoxTableCell.forTableColumn(colPresente));
    }

    @FXML
    public void registrarAsistencia() {
        if (cbInscripcion.getValue() == null || dpFecha.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione inscripción y fecha.").showAndWait();
            return;
        }

        String sql = "INSERT INTO asistencias (id_inscripcion, fecha_asistencia, presente) VALUES (?, ?, false)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mapInscripciones.get(cbInscripcion.getValue()));
            stmt.setDate(2, Date.valueOf(dpFecha.getValue()));
            stmt.executeUpdate();

            new Alert(Alert.AlertType.INFORMATION, "Asistencia registrada correctamente.").showAndWait();
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
                SELECT a.id_asistencia, a.id_inscripcion, a.fecha_asistencia, a.presente,
                       CONCAT(p.nombre, ' ', p.apellido) AS alumno,
                       m.nombre_materia AS materia
                FROM asistencias a
                JOIN inscripciones i ON a.id_inscripcion = i.id_inscripcion
                JOIN persona_escuela p ON i.id_persona = p.id_persona
                JOIN materias m ON i.id_materia = m.id_materia
                ORDER BY a.fecha_asistencia DESC
                """;

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                listaAsistencias.add(new Asistencia(
                        rs.getInt("id_asistencia"),
                        rs.getInt("id_inscripcion"),
                        rs.getString("alumno"),
                        rs.getString("materia"),
                        rs.getDate("fecha_asistencia").toLocalDate(),
                        rs.getBoolean("presente")
                ));
            }

            tablaAsistencias.setItems(listaAsistencias);

        } catch (SQLException e) {
            e.printStackTrace();
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
        }
    }
}
