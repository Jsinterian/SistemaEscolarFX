Sistema Escolar FX

Aplicación JavaFX + MariaDB para gestión escolar con CRUD completo.

🧩 Descripción

Este proyecto implementa un sistema de gestión escolar desarrollado en Java utilizando JavaFX para la interfaz gráfica y MariaDB como base de datos relacional.
El sistema permite administrar personas, materias, inscripciones y asistencias de alumnos de manera sencilla e intuitiva.

🧠 Características principales

✅ Interfaz moderna y responsiva con JavaFX y CSS
✅ Conexión a MariaDB mediante JDBC
✅ Estructura modular (MVC)
✅ CRUD completo para cada entidad
✅ Relaciones entre tablas con claves foráneas
✅ Registro y marcación de asistencias con CheckBoxTableCell
✅ Proyecto administrado con Gradle

🧱 Módulos del sistema
Módulo	Funcionalidad principal
👤 Personas	Registro de alumnos, docentes y personal (CRUD completo)
📘 Materias	Gestión de asignaturas (CRUD completo)
🧾 Inscripciones	Asociación alumno ↔ materia
🕐 Asistencias	Registro de asistencia con opción presente/ausente
🛠️ Tecnologías utilizadas

Java 23.0.1

JavaFX 23.0.2

Gradle 8.13

MariaDB (conector JDBC)

Scene Builder para diseño FXML

IntelliJ IDEA como entorno de desarrollo

⚙️ Requisitos previos

Tener instalado Java 23+

Instalar JavaFX SDK

Configurar MariaDB y crear la base de datos escuela_db

Ejecutar los siguientes scripts SQL:

CREATE DATABASE escuela_db;
USE escuela_db;

CREATE TABLE persona_escuela (
id_persona INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(100),
apellido VARCHAR(100),
sexo VARCHAR(10),
fecha_nacimiento DATE,
rol VARCHAR(50)
);

CREATE TABLE materias (
id_materia INT AUTO_INCREMENT PRIMARY KEY,
nombre_materia VARCHAR(100),
descripcion TEXT
);

CREATE TABLE inscripciones (
id_inscripcion INT AUTO_INCREMENT PRIMARY KEY,
id_persona INT,
id_materia INT,
fecha_inscripcion DATE,
FOREIGN KEY (id_persona) REFERENCES persona_escuela(id_persona),
FOREIGN KEY (id_materia) REFERENCES materias(id_materia)
);

CREATE TABLE asistencias (
id_asistencia INT AUTO_INCREMENT PRIMARY KEY,
id_inscripcion INT,
fecha_asistencia DATE,
presente BOOLEAN,
FOREIGN KEY (id_inscripcion) REFERENCES inscripciones(id_inscripcion)
);

🚀 Ejecución del proyecto

Clona este repositorio:

git clone https://github.com/Jsinterian/SistemaEscolarFX.git
cd SistemaEscolarFX


Abre el proyecto en IntelliJ IDEA

Asegúrate de tener configurado el JavaFX SDK

Ejecuta la clase principal:

com.escuela.app.sistemaescolarfx.MainApp

🎨 Estilo visual

El sistema utiliza un diseño tipo Material Design definido en style.css, con botones animados, tablas estilizadas y colores suaves para mejorar la experiencia del usuario.

👨‍💻 Autor

Jose Manuel Interian Chan

📜 Licencia

Este proyecto es de uso educativo y libre, siempre que se otorgue crédito al autor.

⭐ Capturas (opcional)

(Conexión segura SSH (JSch)

El sistema implementa una conexión SSH segura mediante la librería JSch (com.jcraft:jsch:0.1.55).
Esta capa de seguridad permite crear un túnel cifrado entre la aplicación local y el servidor remoto de base de datos, utilizando las credenciales:

Host: fi.jcaguilar.dev
Usuario SSH: patito
Contraseña SSH: cuack
Usuario BD: becario
Contraseña BD: FdI-its-5a


Durante la ejecución, el sistema establece automáticamente el túnel localhost:3307 → 127.0.0.1:3306 y realiza todas las operaciones sobre MariaDB a través de este canal protegido.
De esta manera, se asegura la comunicación entre cliente y servidor sin exponer credenciales ni puertos del motor de base de datos.Hub)

📍 Desarrollado con pasión por la tecnología, la educación y la ingeniería.