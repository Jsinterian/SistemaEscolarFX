package com.escuela.app.sistemaescolarfx.controllers;
import com.escuela.app.sistemaescolarfx.model.ConexionBD;
import com.escuela.app.sistemaescolarfx.model.Materia;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
public class MateriaController {
    @FXML private TextField txtNombreMateria;
    @FXML private TextArea txtDescripcion;
    @FXML private TableView<Materia> tablaMaterias;
    @FXML private TableColumn<Materia, Integer> colIdMateria;
    @FXML private TableColumn<Materia, String> colNombreMateria;
    @FXML private TableColumn<Materia, String> colDescripcion;
    private ObservableList<Materia> listaMaterias = FXCollections.observableArrayList();
    @FXML
    public void initialize() {
        colIdMateria.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getIdMateria()).asObject());
        colNombreMateria.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNombreMateria()));
        colDescripcion.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescripcion()));
        tablaMaterias.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtNombreMateria.setText(newSel.getNombreMateria());
                txtDescripcion.setText(newSel.getDescripcion());
            }
        });
        mostrarMaterias();
    }
    @FXML
    public void editarMateria() {
        Materia seleccionada = tablaMaterias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione una materia para editar.").showAndWait();
            return;
        }
        if (txtNombreMateria.getText().isEmpty() && txtDescripcion.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Debe ingresar al menos un nuevo valor.").showAndWait();
            return;
        }
        String sql = "UPDATE materias SET nombre_materia = ?, descripcion = ? WHERE id_materia = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String nuevoNombre = txtNombreMateria.getText().isEmpty() ? seleccionada.getNombreMateria() : txtNombreMateria.getText();
            String nuevaDescripcion = txtDescripcion.getText().isEmpty() ? seleccionada.getDescripcion() : txtDescripcion.getText();
            stmt.setString(1, nuevoNombre);
            stmt.setString(2, nuevaDescripcion);
            stmt.setInt(3, seleccionada.getIdMateria());
            stmt.executeUpdate();
            new Alert(Alert.AlertType.INFORMATION, "Materia actualizada correctamente.").showAndWait();
            mostrarMaterias();
            limpiarCampos();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al actualizar: " + e.getMessage()).showAndWait();
        }
    }
    @FXML
    public void eliminarMateria() {
        Materia seleccionada = tablaMaterias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione una materia para eliminar.").showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar la materia \"" + seleccionada.getNombreMateria() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            String sql = "DELETE FROM materias WHERE id_materia = ?";

            try (Connection conn = ConexionBD.getConexion();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, seleccionada.getIdMateria());
                stmt.executeUpdate();
                new Alert(Alert.AlertType.INFORMATION, "Materia eliminada correctamente.").showAndWait();
                mostrarMaterias();
            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Error al eliminar: " + e.getMessage()).showAndWait();
            }
        }
    }
    @FXML
    public void insertarMateria() {
        String sql = "INSERT INTO materias (nombre_materia, descripcion) VALUES (?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, txtNombreMateria.getText());
            stmt.setString(2, txtDescripcion.getText());
            stmt.executeUpdate();
            limpiarCampos();
            mostrarMaterias();
            new Alert(Alert.AlertType.INFORMATION, "Materia agregada correctamente.").showAndWait();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al insertar materia: " + e.getMessage()).showAndWait();
        }
    }
    @FXML
    public void mostrarMaterias() {
        listaMaterias.clear();
        String sql = "SELECT * FROM materias";
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                listaMaterias.add(new Materia(
                        rs.getInt("id_materia"),
                        rs.getString("nombre_materia"),
                        rs.getString("descripcion")
                ));
            }

            tablaMaterias.setItems(listaMaterias);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void limpiarCampos() {
        txtNombreMateria.clear();
        txtDescripcion.clear();
    }
}
