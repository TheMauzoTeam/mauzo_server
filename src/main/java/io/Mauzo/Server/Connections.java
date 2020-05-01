package io.Mauzo.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Connections {
    private List<Connection> connections = new ArrayList<>();
    private static Connections controller = null;

    /**
     * Inicializa la conexión con la base de datos y añade las conexiones a la lista
     *
     * @throws SQLException Excepción que se envia al encontrar fallas al establecer una conexión
     */
    public Connections() throws SQLException{
        //Cargamos las dependencias del driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se ha encontrado el driver de PostgreSQL: " + e.toString());
        }

        // Inicializamos el objeto correspondiente a la conexión.
        String url = System.getenv("JDBC_DATABASE_URL");

        //Añade las 20 conexiones
        for (int i = 0; i < 20; i++) {
            Connection connection = DriverManager.getConnection(url);
            connections.add(connection);
        }

        //Creamos el cuerpo de la base de datos
        try (Statement st = connections.get(0).createStatement()) {
            // Creamos la estructura de la base de datos
            st.execute("CREATE TABLE IF NOT EXISTS Users(id SERIAL PRIMARY KEY, firstname TEXT NOT NULL, lastname TEXT NOT NULL, username TEXT UNIQUE NOT NULL, email TEXT NOT NULL, password TEXT NOT NULL, isAdmin BOOLEAN NOT NULL, userPic BYTEA);");
            st.execute("CREATE TABLE IF NOT EXISTS Discounts(id SERIAL PRIMARY KEY, codeDisc VARCHAR(10) NOT NULL, descDisc TEXT NOT NULL, pricePerc FLOAT NOT NULL);");
            st.execute("CREATE TABLE IF NOT EXISTS Refunds(id SERIAL PRIMARY KEY, dateRefund TIMESTAMP, userId INTEGER, saleId INTEGER FOREIGN KEY (userId) REFERENCES Users(Id), FOREIGN KEY (saleId) REFERECES Sales(Id));");
            st.execute("CREATE TABLE IF NOT EXISTS Sales(id SERIAL PRIMARY KEY, stampRef TIMESTAMP NOT NULL, prodId INTEGER NOT NULL, discId INTEGER, refundId INTEGER, FOREIGN KEY (refundId) REFERENCES Refunds(Id), FOREIGN KEY (discId) REFERENCES Discounts(Id));");
            st.execute("CREATE TABLE IF NOT EXISTS Products(id SERIAL PRIMARY KEY, prodName VARCHAR(50) NOT NULL, prodDesc TEXT NOT NULL, prodPrice FLOAT NOT NULL, prodPic BYTEA);");
            st.execute("CREATE TABLE IF NOT EXISTS Sales_Products(salesId INTEGER NOT NULL, productId INTEGER NOT NULL, PRIMARY KEY (salesId, productId), FOREIGN KEY (salesId) REFERENCES Sales(Id), FOREIGN KEY (productId) REFERENCES Products(Id));");

            // Agregamos el usuario administrador
            st.executeQuery("INSERT INTO public.Users(id, firstname, lastname, username, email, password, isAdmin, userPic) VALUES (1, 'Super', 'Administrador', 'admin', 'admin@localhost', '21232f297a57a5a743894a0e4a801fc3', true, null) ON CONFLICT DO NOTHING;");
        }
    }

    /**
     * Obtiene una conexión de la lista para uso exclusivo
     *
     * @return la conexión
     */
    public Connection acquireConnection() {
        synchronized (connections) {
            if (connections.size() > 0) {
                Connection connection = connections.get(0);
                connections.remove(connection);
                return connection;
            }
        }

        return null;
    }

    /**
     * Añade una conexión a la lista
     *
     * @param connection La conexión a añadir a la lista
     */
    public void releaseConnection(Connection connection) {
        synchronized (connections) {
            if (connection != null) {
                connections.add(connection);
            }
        }
    }

    /**
     * Método para recuperar el controlador de la clase Connections.
     *
     * @return El controlador de la clase Connections.
     */
    public static Connections getController() throws SQLException{
        if (controller == null)
            controller = new Connections();

        return controller;
    }
}
