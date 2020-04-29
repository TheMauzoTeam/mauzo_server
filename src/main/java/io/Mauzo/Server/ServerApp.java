package io.Mauzo.Server;

// Core basico para inicializar las conexiones SQL.
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.logging.Logger;

// Paquetes relativos a la inicialización del servidor
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Clases controladoras de las interfaces web expuestas.
import io.Mauzo.Server.Controllers.DiscountsCtrl;
import io.Mauzo.Server.Controllers.InformsCtrl;
import io.Mauzo.Server.Controllers.LoginCtrl;
import io.Mauzo.Server.Controllers.ProductsCtrl;
import io.Mauzo.Server.Controllers.SalesCtrl;
import io.Mauzo.Server.Controllers.UsersCtrl;
import io.Mauzo.Server.Controllers.RefundsCtrl;

@Configuration
@SpringBootApplication
// Esto que voy a decir, ..., es un poco pro, ..., pero en fin, ... Aquí tienes una limitación muy severa,
// y es que sólo permites una conxión al mismo tiempo. Para permitir muchas, necesitas algunas cosas que me
// habréis de preguntar en clase
public class ServerApp {
    private static Connection connection = null;
    private static Logger loggerSystem = Logger.getLogger("MauzoServer");

    /**
     * Método principal que inicializa el servidor Spring Boot,
     * el cual luego invocará a los métodos y clases que se han
     * ido desarrollando a lo largo y ancho del proyecto.
     * 
     * @param args  Los argumentos que recibe el servidor.
     */
    public static void main(String[] args) {
        SpringApplication.run(ServerApp.class, args);
    }

    /**
     * Método dedicado a la configuración y mapeo de las vistas usadas en el
     * servidor.
     * 
     * @return Configuración del servidor.
     */
    @Bean
    public ResourceConfig resourceConfig() {
        ResourceConfig config = new ResourceConfig();

        // Clases mapeadas en el server/api
        config.register(UsersCtrl.class);
        config.register(LoginCtrl.class);
        config.register(SalesCtrl.class);

        // Clases deshabilitadas
        // config.register(RefundsCtrl.class);
        // config.register(ProductsCtrl.class);
        // config.register(InformsCtrl.class);
        // config.register(DiscountsCtrl.class);

        return config;
    }

    /**
     * Getter para devolver el objeto que se está utilizando para registrar los
     * eventos del servidor por la consola del sistema.
     * 
     * @return Devuelve el objeto usado como Logger.
     */
    public static Logger getLoggerSystem() {
        return loggerSystem;
    }

    /**
     * Getter para obtener el objeto que se está utilizando para registrar la
     * conexion con la base de datos.
     * 
     * @return Devuelve el objeto usado como Conexion.
     */
    public static Connection getConnection() throws SQLException {
        // Validamos la conexión con la base de datos.
        if (connection == null)
            setConnection();

        return connection;
    }

    /**
     * Método para inicializar la conexión con la base de datos usado por el
     * servidor.
     * 
     * Este obtendrá una variable de entorno llamada DATABASE_URL, procedente de la
     * infraestructura de Heroku usado para albergar el servidor, este tendrá la URL
     * con los parametros de conexión.
     * 
     * @throws SQLException Execepcion en caso de no poder conectar con la BBDD.
     */
    private static void setConnection() throws SQLException {
        // Formamos la URL de conexion correspondiente.
        // URL de Información: https://devcenter.heroku.com/articles/connecting-to-relational-databases-on-heroku-with-java#using-the-jdbc_database_url
        String url = System.getenv("JDBC_DATABASE_URL");

        // Cargamos las dependencias del driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se ha encontrado el driver de PostgreSQL: " + e.toString());
        }

        // Inicializamos el objeto correspondiente a la conexión.
        connection = DriverManager.getConnection(url);

        // Creamos la estructura de datos de la bbdd, en caso de ser necesario.
        try (Statement st = connection.createStatement()) {
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
}