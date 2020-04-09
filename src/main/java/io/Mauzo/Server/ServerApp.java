package io.Mauzo.Server;

// Core basico para inicializar las conexiones SQL.
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.logging.Logger;

// Core basico para inicializar la REST API
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api")
public class ServerApp extends Application {
    private static Connection connection = null;
    private static Logger loggerSystem = Logger.getLogger("MauzoServer");
    
    /**
     * Getter para devolver el objeto que se está utilizando
     * para registrar los eventos del servidor por la consola 
     * del sistema.
     * 
     * @return  Devuelve el objeto usado como Logger.
     */
    public static Logger getLoggerSystem() {
        return loggerSystem;
    }
    
    /**
     * Getter para obtener el objeto que se está utilizando
     * para registrar la conexion con la base de datos.
     * 
     * @return  Devuelve el objeto usado como Conexion.
     */
    public static Connection getConnection() throws SQLException {
        // Validamos la conexión con la base de datos.
        if (connection == null)
            setConnection();

        return connection;
    }

    /**
     * Método para inicializar la conexión con la base de datos
     * usado por el servidor.
     * 
     * Este obtendrá una variable de entorno llamada DATABASE_URL,
     * procedente de la infraestructura de Heroku usado para albergar
     * el servidor, este tendrá la URL con los parametros de conexión.
     * 
     * @throws SQLException Execepcion en caso de no poder conectar con la BBDD.
     */
    private static void setConnection() throws SQLException {
        // Formamos la URL de conexion correspondiente.
        String url = System.getenv("DATABASE_URL");

        try {
            // Cargamos las dependencias del driver
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se ha encontrado el driver de PostgreSQL: " + e.toString());
        }

        // Inicializamos el objeto correspondiente a la conexión.
        connection = DriverManager.getConnection(url);

        // Creamos la estructura de datos de la bbdd, en caso de ser necesario.
        try (Statement st = connection.createStatement()) {
            // Creamos la estructura de la base de datos
            st.execute("CREATE TABLE IF NOT EXISTS Users(id SERIAL PRIMARY KEY, firstname TEXT NOT NULL, lastname TEXT NOT NULL, username TEXT UNIQUE NOT NULL, email TEXT NOT NULL, passwd TEXT NOT NULL, isAdmin BOOLEAN NOT NULL);");
            st.execute("CREATE TABLE IF NOT EXISTS Discounts(id SERIAL PRIMARY KEY, codeDisc VARCHAR(10) NOT NULL, descDisc TEXT NOT NULL, prizePerc FLOAT NOT NULL);");
            st.execute("CREATE TABLE IF NOT EXISTS Refunds(id SERIAL PRIMARY KEY, dateRefund TIMESTAMP, userId INTEGER, FOREIGN KEY (userId) REFERENCES Users(Id));");
            st.execute("CREATE TABLE IF NOT EXISTS Sales(id SERIAL PRIMARY KEY, stampRef TIMESTAMP NOT NULL, prodId INTEGER NOT NULL, discId INTEGER, refundId INTEGER, FOREIGN KEY (refundId) REFERENCES Refunds(Id), FOREIGN KEY (discId) REFERENCES Discounts(Id));");
            st.execute("CREATE TABLE IF NOT EXISTS Products(id SERIAL PRIMARY KEY, prodName VARCHAR(50) NOT NULL, prodDesc TEXT NOT NULL, prodPrize FLOAT NOT NULL, prodImag BYTEA);");
            st.execute("CREATE TABLE IF NOT EXISTS Sales_Products(salesId INTEGER NOT NULL, productId INTEGER NOT NULL, PRIMARY KEY (salesId, productId), FOREIGN KEY (salesId) REFERENCES Sales(Id), FOREIGN KEY (productId) REFERENCES Products(Id));");

            // Agregamos el usuario administrador
            st.executeQuery("INSERT INTO public.Users(id, firstname, lastname, username, email, passwd, isAdmin) VALUES (1, 'Super', 'Administrador' 'admin', 'admin@localhost', '21232f297a57a5a743894a0e4a801fc3', true);");
        }
    }
}