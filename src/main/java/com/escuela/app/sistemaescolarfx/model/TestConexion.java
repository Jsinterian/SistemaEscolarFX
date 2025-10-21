package com.escuela.app.sistemaescolarfx;

import com.escuela.app.sistemaescolarfx.model.ConexionBD;
import java.sql.Connection;

public class TestConexion {
    public static void main(String[] args) {
        try (Connection conn = ConexionBD.getConexion()) {
            System.out.println("Conexión probada correctamente 🚀");
        } catch (Exception e) {
            System.out.println("❌ Error al conectar: " + e.getMessage());
        }
    }
}
