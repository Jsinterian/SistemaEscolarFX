package com.escuela.app.sistemaescolarfx.controllers;

import com.escuela.app.sistemaescolarfx.model.ConexionBD;
import com.escuela.app.sistemaescolarfx.model.Persona;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.time.LocalDate;

public class PersonaController {

    @FXML private TextField txtNombre, txtApellido;
    @FXML private ChoiceBox<String> cbSexo, cbRol;
    @FXML private DatePicker dpFecha;
    @FXML private TableView<Persona> tablaPersonas;
    @FXML private TableColumn<Persona, Integer> colId;
    @FXML private TableColumn<Persona, String> colNombre, colApellido, colSexo, colRol;
    @FXML private TableColumn<Persona, LocalDate> colFecha;

    private final ObservableList<Persona> listaPersonas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Catálogos visibles en UI
        cbSexo.setItems(FXCollections.observableArrayList("Masculino", "Femenino", "Otro"));
        cbRol.setItems(FXCollections.observableArrayList("Alumno", "Docente", "Admin"));

        // Columnas
        colId.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getIdPersona()).asObject());
        colNombre.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNombre()));
        colApellido.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getApellido()));
        colSexo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getSexo()));
        colFecha.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getFechaNacimiento()));
        colRol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getRol()));

        mostrarPersonas();
    }

    // ---- Mapeos a BD ----
    // its5a guarda sexo como 'h' / 'm' / 'o'
    private String mapSexo(String sexoUI) {
        if (sexoUI == null) return null;
        switch (sexoUI.trim().toLowerCase()) {
            case "masculino": case "m": return "h";
            case "femenino":  case "f": return "m";
            case "otro":      case "o": default: return "o";
        }
    }

    // Rol INT (ajusta si tus IDs son otros)
    private int mapRol(String rolUI) {
        if (rolUI == null) throw new IllegalArgumentException("Rol no puede ser nulo");
        switch (rolUI.trim().toLowerCase()) {
            case "alumno":  return 1;
            case "docente": return 2; // si en UI pones "Profesor", mapea aquí también
            case "admin":
            case "administrador": return 3;
            default: throw new IllegalArgumentException("Rol desconocido: " + rolUI);
        }
    }

    // ---- CRUD ----
    @FXML
    public void insertarPersona(ActionEvent actionEvent) {
        String nombre = txtNombre.getText();
        String apellido = txtApellido.getText();
        String sexoUI = cbSexo.getValue();
        String rolUI  = cbRol.getValue();
        LocalDate fecha = dpFecha.getValue();

        if (nombre == null || nombre.isBlank() ||
                apellido == null || apellido.isBlank() ||
                sexoUI == null || rolUI == null || fecha == null) {
            new Alert(Alert.AlertType.WARNING, "Llena todos los campos.").showAndWait();
            return;
        }

        String sql = "INSERT INTO personas_escuela (nombre, apellido, sexo, fh_nac, id_rol) VALUES (?,?,?,?,?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre.trim());
            stmt.setString(2, apellido.trim());
            stmt.setString(3, mapSexo(sexoUI));                // 'h' / 'm' / 'o'
            stmt.setDate(4, Date.valueOf(fecha));
            stmt.setInt(5, mapRol(rolUI));                     // 1/2/3

            stmt.executeUpdate();

            new Alert(Alert.AlertType.INFORMATION, "Persona registrada correctamente.").showAndWait();
            limpiarCampos();
            mostrarPersonas();

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Error al insertar: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void editarPersona() {
        Persona seleccionada = tablaPersonas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione una persona para editar.").showAndWait();
            return;
        }

        String sql = "UPDATE personas_escuela SET nombre=?, apellido=?, sexo=?, fh_nac=?, id_rol=? WHERE id_persona=?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Si no eliges nada en UI, usa lo que ya tiene la fila
            String sexoUI = (cbSexo.getValue() != null) ? cbSexo.getValue() : seleccionada.getSexo(); // "Masculino"/"Femenino"/"Otro"
            String rolUI  = (cbRol.getValue()  != null) ? cbRol.getValue()  : seleccionada.getRol();  // "Alumno"/"Docente"/"Admin"
            String nombre = (!txtNombre.getText().isEmpty()) ? txtNombre.getText().trim() : seleccionada.getNombre();
            String apellido = (!txtApellido.getText().isEmpty()) ? txtApellido.getText().trim() : seleccionada.getApellido();

            stmt.setString(1, nombre);
            stmt.setString(2, apellido);
            stmt.setString(3, mapSexo(sexoUI));

            LocalDate nuevaFecha = (dpFecha.getValue() != null) ? dpFecha.getValue() : seleccionada.getFechaNacimiento();
            if (nuevaFecha != null) {
                stmt.setDate(4, Date.valueOf(nuevaFecha));
            } else {
                stmt.setNull(4, Types.DATE); // null-safe si en BD se permite NULL
            }

            stmt.setInt(5, mapRol(rolUI));
            stmt.setInt(6, seleccionada.getIdPersona());

            stmt.executeUpdate();
            new Alert(Alert.AlertType.INFORMATION, "Registro actualizado correctamente.").showAndWait();
            mostrarPersonas();
            limpiarCampos();

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Error al actualizar: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void eliminarPersona() {
        Persona seleccionada = tablaPersonas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione una persona para eliminar.").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar a " + seleccionada.getNombre() + " " + seleccionada.getApellido() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            String sql = "DELETE FROM personas_escuela WHERE id_persona=?";

            try (Connection conn = ConexionBD.getConexion();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, seleccionada.getIdPersona());
                stmt.executeUpdate();
                new Alert(Alert.AlertType.INFORMATION, "Registro eliminado correctamente.").showAndWait();
                mostrarPersonas();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "Error al eliminar: " + e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    public void mostrarPersonas() {
        listaPersonas.clear();

        String sql =
                "SELECT id_persona, nombre, apellido, " +
                        "       CASE WHEN sexo='h' THEN 'Masculino' " +
                        "            WHEN sexo='m' THEN 'Femenino' " +
                        "            ELSE 'Otro' END AS sexo, " +
                        "       fh_nac, " +
                        "       CASE WHEN id_rol=1 THEN 'Alumno' " +
                        "            WHEN id_rol=2 THEN 'Docente' " +
                        "            WHEN id_rol=3 THEN 'Admin' " +
                        "            ELSE 'Otro' END AS rol " +
                        "FROM personas_escuela " +
                        "ORDER BY id_persona DESC";

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Date d = rs.getDate("fh_nac");
                LocalDate fn = (d != null) ? d.toLocalDate() : null;

                listaPersonas.add(new Persona(
                        rs.getInt("id_persona"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("sexo"), // texto amigable ya mapeado por CASE
                        fn,
                        rs.getString("rol")   // texto amigable ya mapeado por CASE
                ));
            }

            tablaPersonas.setItems(listaPersonas);

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Error al consultar: " + e.getMessage()).showAndWait();
            e.printStackTrace();
        }
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtApellido.clear();
        cbSexo.setValue(null);
        cbRol.setValue(null);
        dpFecha.setValue(null);
    }
}
