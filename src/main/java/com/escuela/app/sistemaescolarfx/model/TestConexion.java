package com.escuela.app.sistemaescolarfx.model;

import com.escuela.app.sistemaescolarfx.model.ConexionBD;
import java.sql.Connection;

public class TestConexion {
    public static void main(String[] args) {
        Connection conn = ConexionBD.getConexion();
        if (conn != null) {
            System.out.println("🔥 Conexión totalmente operativa");
        } else {
            System.out.println("⚠️ No se pudo conectar");
        }
        ConexionBD.cerrarSSH();
    }
}
