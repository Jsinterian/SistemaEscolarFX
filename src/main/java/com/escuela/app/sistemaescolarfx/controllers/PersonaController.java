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

    private ObservableList<Persona> listaPersonas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Inicializar listas en lugar del FXML
        cbSexo.setItems(FXCollections.observableArrayList("Masculino", "Femenino"));
        cbRol.setItems(FXCollections.observableArrayList("Alumno", "Profesor", "Administrador"));

        // Configurar columnas de la tabla
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getIdPersona()).asObject());
        colNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNombre()));
        colApellido.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getApellido()));
        colSexo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSexo()));
        colFecha.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getFechaNacimiento()));
        colRol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRol()));

        mostrarPersonas();
    }


    @FXML
    public void editarPersona() {
        Persona seleccionada = tablaPersonas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione una persona para editar.").showAndWait();
            return;
        }

        String sql = "UPDATE persona_escuela SET nombre=?, apellido=?, sexo=?, fecha_nacimiento=?, rol=? WHERE id_persona=?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, txtNombre.getText().isEmpty() ? seleccionada.getNombre() : txtNombre.getText());
            stmt.setString(2, txtApellido.getText().isEmpty() ? seleccionada.getApellido() : txtApellido.getText());
            stmt.setString(3, cbSexo.getValue() == null ? seleccionada.getSexo() : cbSexo.getValue());
            stmt.setDate(4, dpFecha.getValue() == null ? java.sql.Date.valueOf(seleccionada.getFechaNacimiento()) : java.sql.Date.valueOf(dpFecha.getValue()));
            stmt.setString(5, cbRol.getValue() == null ? seleccionada.getRol() : cbRol.getValue());
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

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar a " + seleccionada.getNombre() + " " + seleccionada.getApellido() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            String sql = "DELETE FROM persona_escuela WHERE id_persona=?";
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
        String sql = "SELECT * FROM persona_escuela";

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                listaPersonas.add(new Persona(
                        rs.getInt("id_persona"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("sexo"),
                        rs.getDate("fecha_nacimiento").toLocalDate(),
                        rs.getString("rol")
                ));
            }

            tablaPersonas.setItems(listaPersonas);

        } catch (SQLException e) {
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

    public void insertarPersona(ActionEvent actionEvent) {
    }
}
