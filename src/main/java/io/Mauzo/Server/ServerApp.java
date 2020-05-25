/*
 * MIT License
 *
 * Copyright (c) 2020 The Mauzo Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.Mauzo.Server;

// Paquetes relativos a la conexion con la base de datos.
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

// Paquetes relacionados con el registro en la consola de salida.
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

// Paquetes relativos a la inicialización del servidor.
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Clases controladoras de las interfaces web expuestas.
import io.Mauzo.Server.Controllers.DiscountsCtrl;
import io.Mauzo.Server.Controllers.InformsCtrl;
import io.Mauzo.Server.Controllers.LoginCtrl;
import io.Mauzo.Server.Controllers.ProductsCtrl;
import io.Mauzo.Server.Controllers.SalesCtrl;
import io.Mauzo.Server.Controllers.UsersCtrl;
import io.Mauzo.Server.Managers.Connections;
import io.Mauzo.Server.Controllers.RefundsCtrl;

/**
 * Clase principal del servidor.
 * 
 * Aqui tiene configuraciones basicas para el framework de Spring Boot y su
 * servidor Glassfish, el esquema de la base de datos a importar y tambien
 * variables en relacion a la cadena de conexión, una conexión generica para
 * tareas menores o una variable con el puntero de conexión.
 * 
 * @author Neirth Sergio Martinez
 */
@Configuration
@SpringBootApplication
public class ServerApp {
    private static Connection connection = null;
    private static Logger loggerSystem = LogManager.getLogger(ServerApp.class);
    private static String url = System.getenv("JDBC_DATABASE_URL");

    /**
     * Método principal que inicializa el servidor Spring Boot, el cual luego
     * invocará a los métodos y clases que se han ido desarrollando a lo largo y
     * ancho del proyecto.
     * 
     * @param args Los argumentos que recibe el servidor.
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ServerApp.class);

        /*
         * Aqui cargamos las conexiones de nuestra base de datos antes de que sea
         * requerido por ningún cliente, dado que está clase durante su instaciación
         * hace muchas operaciones relacionadas con base de datos y multithread y tarda
         * muchos segundos en completarse.
         */
        app.addInitializers((context) -> {
            try {
                loggerSystem.info("Loading the database connections...");

                Connections.getController();
                ServerApp.getConnection();
            } catch (Exception e) {
                loggerSystem.error("The server couldn't be loaded, please check the debug info...");
                ServerUtils.writeServerException(e);

                System.exit(-1);
            }
        });

        /*
         * Ya despues de haber integrado los inicializadores de la base de datos,
         * arrancamos la aplicacion de spring boot, empezando al principio por estos
         * incializadores de las lineas anteriores que dejarán preparas las conexiones
         * con la base de datos.
         */
        app.run(args);
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
        config.register(RefundsCtrl.class);
        config.register(ProductsCtrl.class);
        config.register(InformsCtrl.class);
        config.register(DiscountsCtrl.class);

        return config;
    }

    /**
     * Setter para indicar una JDBC URL modificado, util sobretodo para el caso de
     * los test.
     * 
     * @param url La URL JDBC respecto a la BBDD.
     */
    public static void setUrl(String url) {
        ServerApp.url = url;
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
            connection = setConnection();

        return connection;
    }

    /**
     * Método para inicializar la conexión con la base de datos usado por el
     * servidor.
     * 
     * Este obtendrá una variable de entorno llamada JDBC_DATABASE_URL, procedente
     * de la infraestructura de Heroku usado para albergar el servidor, este tendrá
     * la URL con los parametros de conexión.
     * 
     * @throws SQLException Execepcion en caso de no poder conectar con la BBDD.
     * @return Devuelve un objeto de conexión.
     */
    public static Connection setConnection() throws SQLException {
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
            st.execute("CREATE TABLE IF NOT EXISTS Discounts (id SERIAL, codeDisc VARCHAR(10) NOT NULL, descDisc TEXT NOT NULL, pricePerc FLOAT NOT NULL, PRIMARY KEY (id), UNIQUE (codeDisc));");
            st.execute("CREATE TABLE IF NOT EXISTS Products (id SERIAL, prodName VARCHAR(45) NOT NULL, prodCode VARCHAR(45) NOT NULL, prodPrice FLOAT NOT NULL, prodDesc TEXT NULL, prodPic BYTEA NULL, PRIMARY KEY (id), UNIQUE(prodCode));");
            st.execute("CREATE TABLE IF NOT EXISTS Users (id SERIAL, firstname VARCHAR(45) NOT NULL, lastname VARCHAR(45) NOT NULL, username VARCHAR(45) NOT NULL, password TEXT NOT NULL, email TEXT NOT NULL, isAdmin BOOLEAN NOT NULL, userPic BYTEA NULL, PRIMARY KEY (id), UNIQUE (username));");
            st.execute("CREATE TABLE IF NOT EXISTS Sales (id SERIAL, stampRef DATE NOT NULL, userId INT NOT NULL, prodId INT NOT NULL, discId INT NULL, PRIMARY KEY (id), FOREIGN KEY (discId) REFERENCES Discounts(id), FOREIGN KEY (prodId) REFERENCES Products(id), FOREIGN KEY (userId) REFERENCES Users(id));");
            st.execute("CREATE TABLE IF NOT EXISTS Refunds (id SERIAL, dateRefund DATE NOT NULL, userId INT NOT NULL, saleId INT NOT NULL, PRIMARY KEY (id), FOREIGN KEY (userId) REFERENCES Users(id), FOREIGN KEY (saleId) REFERENCES Sales(id) ON DELETE SET NULL ON UPDATE NO ACTION);");

            // Agregamos el usuario administrador
            st.execute("INSERT INTO public.Users(firstname, lastname, username, email, password, isAdmin, userPic) VALUES ('Super', 'Administrador', 'admin', 'admin@localhost', '21232f297a57a5a743894a0e4a801fc3', true, null) ON CONFLICT DO NOTHING;");
        }

        return connection;
    }
}