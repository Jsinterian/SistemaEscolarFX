package com.escuela.app.sistemaescolarfx.controllers;

import com.escuela.app.sistemaescolarfx.model.ConexionBD;
import com.escuela.app.sistemaescolarfx.model.Materia;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class MateriaController {

    @FXML private TextField txtNombreMateria; // Usaremos este como "descripcion" (nombre visible)
    @FXML private TextArea txtDescripcion;    // Opcional: por ahora también lo mapeamos a "descripcion"
    @FXML private TableView<Materia> tablaMaterias;
    @FXML private TableColumn<Materia, Integer> colIdMateria;
    @FXML private TableColumn<Materia, String> colNombreMateria;
    @FXML private TableColumn<Materia, String> colDescripcion;

    private final ObservableList<Materia> listaMaterias = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colIdMateria.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getIdMateria()).asObject());
        colNombreMateria.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNombreMateria()));
        colDescripcion.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDescripcion()));

        // Cargar selección -> inputs
        tablaMaterias.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, m) -> {
            if (m != null) {
                txtNombreMateria.setText(m.getNombreMateria());
                txtDescripcion.setText(m.getDescripcion());
            }
        });

        mostrarMaterias();
    }

    @FXML
    public void insertarMateria() {
        String nombreUi = (txtNombreMateria.getText() != null) ? txtNombreMateria.getText().trim() : "";
        String descUi   = (txtDescripcion.getText()   != null) ? txtDescripcion.getText().trim()   : "";

        if (nombreUi.isEmpty() && descUi.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Ingresa al menos el nombre de la materia.").showAndWait();
            return;
        }

        // En its5a el “nombre” es la columna descripcion
        String valorDescripcion = !descUi.isEmpty() ? descUi : nombreUi;

        // NOTA: si tu tabla requiere creditos/semestre como NOT NULL, agrega esos campos aquí.
        String sql = "INSERT INTO materias (descripcion) VALUES (?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, valorDescripcion);
            ps.executeUpdate();

            limpiarCampos();
            mostrarMaterias();
            new Alert(Alert.AlertType.INFORMATION, "Materia agregada correctamente.").showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al insertar materia: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void editarMateria() {
        Materia sel = tablaMaterias.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione una materia para editar.").showAndWait();
            return;
        }
        String nombreUi = (txtNombreMateria.getText() != null) ? txtNombreMateria.getText().trim() : "";
        String descUi   = (txtDescripcion.getText()   != null) ? txtDescripcion.getText().trim()   : "";

        if (nombreUi.isEmpty() && descUi.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Debe ingresar al menos un nuevo valor.").showAndWait();
            return;
        }

        // Tomamos prioridad del área de texto; si está vacío, usamos el TextField
        String nuevaDescripcion = !descUi.isEmpty() ? descUi : (!nombreUi.isEmpty() ? nombreUi : sel.getDescripcion());

        String sql = "UPDATE materias SET descripcion = ? WHERE id_materia = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevaDescripcion);
            ps.setInt(2, sel.getIdMateria());
            ps.executeUpdate();

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
        Materia sel = tablaMaterias.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione una materia para eliminar.").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar la materia \"" + sel.getNombreMateria() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            String sql = "DELETE FROM materias WHERE id_materia = ?";

            try (Connection conn = ConexionBD.getConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, sel.getIdMateria());
                ps.executeUpdate();

                new Alert(Alert.AlertType.INFORMATION, "Materia eliminada correctamente.").showAndWait();
                mostrarMaterias();

            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Error al eliminar: " + e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    public void mostrarMaterias() {
        listaMaterias.clear();

        // Solo traemos id y descripcion; mapeamos a ambos campos del modelo/tabla
        String sql = "SELECT id_materia, descripcion FROM materias ORDER BY id_materia DESC";

        try (Connection conn = ConexionBD.getConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String descripcion = rs.getString("descripcion");

                // Como tu modelo tiene nombreMateria y descripcion, de momento llenamos ambos con la misma columna.
                // Si luego agregas "creditos"/"semestre" o una "descripcion larga", lo separamos.
                listaMaterias.add(new Materia(
                        rs.getInt("id_materia"),
                        descripcion,  // nombreMateria (UI)
                        descripcion   // descripcion (misma fuente por ahora)
                ));
            }

            tablaMaterias.setItems(listaMaterias);

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al consultar materias: " + e.getMessage()).showAndWait();
        }
    }

    private void limpiarCampos() {
        txtNombreMateria.clear();
        txtDescripcion.clear();
    }
}
