package io.Mauzo.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.Test;
import org.junit.Assert;
import org.junit.BeforeClass;

public class ConnectionTest {
    /**
     * Preparación de la URL de JDBC con la base de datos
     * de pruebas donde se ejecutarán todos los test de 
     * validación respecto a los métodos a probar.
     */
    @BeforeClass
    public static void prepareDatabase() throws Exception {
        String url = "jdbc:postgresql://ec2-46-137-84-173.eu-west-1.compute.amazonaws.com:5432/dctftmid36ou55?user=rmhzzizdqrajgj&password=b97cce549242c156238562ef5850dbd35f5cb77a4779129cf2d59ed3f2054528";
        ServerApp.setUrl(url);
        
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se ha encontrado el driver de PostgreSQL: " + e.toString());
        }

        Connection connection = DriverManager.getConnection(url);

        connection.createStatement().execute("DROP TABLE IF EXISTS Discounts; DROP TABLE IF EXISTS Products; DROP TABLE IF EXISTS Refunds; DROP TABLE IF EXISTS Sales; DROP TABLE IF EXISTS Users;");
    }

    /**
     * Test que comprueba el momento de conectarse a la base de datos
     * con una URL null lance una excepción esperada para nosotros.
     * 
     * @throws Exception    Puede arrojar cualquier tipo de Excepción.
     */
    @Test(expected = SQLException.class)
    public void checkErrorConnection() throws Exception {
        // Establecemos la URL a nulo, el cual deberia de provocar errores.
        ServerApp.setUrl(null);
       
        // Establecemos conexion, el cual no deberia de poder.
        Connection conn = ServerApp.setConnection();
    }

    /**
     * Test que comprueba que las operaciones de la conexión individual
     * ifuncionen de manera correcta sobre una base de datos que opere,
     * de manera muy semejante a la base de datos que se utiliza en 
     * producción.
     * 
     * @throws Exception    Puede arrojar cualquier tipo de Excepción.
     */
    @Test
    public void checkIndividualConnection() throws Exception {
        // Obtenemos una conexion indivual.
        Connection conn = ServerApp.getConnection();

        // Si no es nulo, significa que ha pasado la prueba.
        Assert.assertNotNull(conn);
    }

    /**
     * Test que comprueba que las operaciones del grupo de conexiones
     * funcionen de manera correcta sobre una base de datos que opere,
     * de manera muy semejante a la base de datos que se utiliza en 
     * producción.
     * 
     * @throws Exception    Puede arrojar cualquier tipo de Excepción.
     */
    @Test
    public void checkPoolsConnection() throws Exception {
        // Variable comprobante del test.
        boolean testPassed = true;

        // Obtenemos una conexion del pool de conexiones.
        if(ServerPools.getController().acquireUsers() == null)
            testPassed = false;
        
        if(ServerPools.getController().acquireSales() == null)
            testPassed = false;

        if(ServerPools.getController().acquireProducts() == null)
            testPassed = false;
            
        if(ServerPools.getController().acquireRefunds() == null)
            testPassed = false;
            
        // if(ServerPools.getController().acquireInforms() == null)
        //     testPassed = false;
            
        // if(ServerPools.getController().acquireDiscounts() == null)
        //     testPassed = false;
            
        // Si no es falso, significa que ha pasado la prueba
        Assert.assertTrue(testPassed);
    }
}