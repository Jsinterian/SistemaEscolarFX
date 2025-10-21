package com.escuela.app.sistemaescolarfx.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL = "jdbc:mariadb://localhost:3306/escuela_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Jsinterian1";
    private static Connection conexion;

    public static Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Conexión exitosa a MariaDB.");
        }
        return conexion;
    }
}
