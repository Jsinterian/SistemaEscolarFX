package com.escuela.app.sistemaescolarfx.model;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    // Datos del servidor remoto
    private static final String HOST_SSH = "fi.jcaguilar.dev";
    private static final String USUARIO_SSH = "patito";
    private static final String PASSWORD_SSH = "cuack";

    // Datos de la base de datos
    private static final String USUARIO_DB = "becario";
    private static final String PASSWORD_DB = "FdI-its-5a";
    private static final String NOMBRE_DB = "its5a";

    // Variables de conexión
    private static Session sesionSSH;
    private static int puertoLocal;
    private static Connection conexion;

    // Establece el túnel SSH (solo si no existe)
    public static synchronized void conectarSSH() throws JSchException {
        if (sesionSSH != null && sesionSSH.isConnected()) {
            System.out.println("✅ SSH ya activa en puerto " + puertoLocal);
            return;
        }

        JSch jsch = new JSch();
        sesionSSH = jsch.getSession(USUARIO_SSH, HOST_SSH, 22);
        sesionSSH.setPassword(PASSWORD_SSH);
        sesionSSH.setConfig("StrictHostKeyChecking", "no");
        sesionSSH.connect(10000);

        // Puerto local dinámico → servidor remoto localhost:3306 (MariaDB)
        puertoLocal = sesionSSH.setPortForwardingL(0, "localhost", 3306);
        System.out.println("🔒 Túnel SSH establecido en puerto local: " + puertoLocal);
    }

    // Obtiene una conexión JDBC hacia MariaDB a través del túnel
    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conectarSSH(); // asegúrate de tener el túnel activo

                String url = "jdbc:mariadb://localhost:" + puertoLocal + "/" + NOMBRE_DB;
                conexion = DriverManager.getConnection(url, USUARIO_DB, PASSWORD_DB);
                System.out.println("✅ Conexión exitosa con MariaDB (BD: " + NOMBRE_DB + ")");
            }
            return conexion;
        } catch (SQLException e) {
            System.err.println("❌ Error SQL: " + e.getMessage());
        } catch (JSchException e) {
            System.err.println("❌ Error SSH: " + e.getMessage());
        }
        return null;
    }

    // Cierra la conexión SSH
    public static void cerrarSSH() {
        if (sesionSSH != null && sesionSSH.isConnected()) {
            sesionSSH.disconnect();
            System.out.println("🔓 Conexión SSH cerrada.");
        }
    }
}
